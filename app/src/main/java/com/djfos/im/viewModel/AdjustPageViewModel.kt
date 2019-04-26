package com.djfos.im.viewModel

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.djfos.im.filter.AbstractFilter
import com.djfos.im.model.Draft
import com.djfos.im.model.DraftRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.opencv.android.Utils
import org.opencv.core.Mat
import java.io.FileOutputStream

private const val TAG = "AdjustPageViewModel"

class AdjustPageViewModel(
        private val draftRepository: DraftRepository
) : ViewModel() {
    lateinit var draft: Draft
    lateinit var origin: Mat

    lateinit var previousResult: Mat
    var currentResult: Mat? = null

    var history: MutableList<AbstractFilter>
        get() = draft.history
        set(value) {
            draft.history = value
        }

    lateinit var currentFilter: AbstractFilter

    val mediator = MutableLiveData<MutableLiveData<AbstractFilter>>()

    fun save(pool: BitmapPool) {
        GlobalScope.launch {
            currentResult?.let { mat ->
                val bitmap = pool.getDirty(mat.width(), mat.height(), Bitmap.Config.ARGB_8888)
                Utils.matToBitmap(mat, bitmap)
                FileOutputStream(draft.thumb).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 50, out)
                    pool.put(bitmap)
                }
            }

            draft.latestModifyTime = System.currentTimeMillis()
            draftRepository.saveDraft(draft)
        }
    }

    fun drop() {
        GlobalScope.launch {
            draftRepository.dropDraft(draft)
        }
    }

    /**
     * fallback somewhere in the history,return the proper filter
     */
    fun fallback(index: Int): AbstractFilter {
        Log.d(TAG, "fallback() called with: index = [$index]")
        require(index >= 0 && index < history.size) { "index $index out of bound, array size ${history.size}" }

        var i = 0
        var mat = origin

        while (i < index) {
            mat = history[i].apply(mat)
            i++
        }

        previousResult = mat

        Log.d(TAG, "fallback: origin $origin")
        Log.d(TAG, "fallback: previousResult $previousResult")
        Log.d(TAG, "fallback: currentResult $currentResult")
        return history[i]
    }

    /**
     * remove filters behind current filter, add given filter to the end of history,
     */
    fun apply(filter: AbstractFilter) {
        currentResult?.let { previousResult = it }

        val currentIndex = history.indexOf(currentFilter)

        history = history.filterIndexed { index, _ -> index <= currentIndex }
                .toMutableList()
                .apply { add(filter) }

    }

    suspend fun getDraft(id: Long): Draft {
        return withContext(Dispatchers.IO) { draftRepository.getDraftSimple(id) }
    }
}


class AViewModelFactory(
        private val draftRepository: DraftRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AdjustPageViewModel(draftRepository) as T
    }
}
