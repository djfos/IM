package com.djfos.im.viewModel

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import android.widget.Toast
import androidx.core.app.ShareCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.djfos.im.filter.AbstractFilter
import com.djfos.im.model.Draft
import com.djfos.im.model.DraftRepository
import com.djfos.im.util.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.opencv.android.Utils
import org.opencv.core.Mat
import java.io.File
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
            saveToFileThumb(pool)
            saveDraft()
        }
    }

    fun saveToFile(context: Context, pool: BitmapPool) {
        val file = createImageFile(savePath)
        GlobalScope.launch {
            saveToFileThumb(pool)
            saveToFileFullSize(context, pool, file)
            saveDraft()
        }
    }

    private fun saveToFileThumb(pool: BitmapPool) {
        val mat = currentResult!!

        val bitmap = pool.getDirty(mat.width(), mat.height(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(mat, bitmap)

        FileOutputStream(draft.thumb).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, out)
            pool.put(bitmap)
        }
    }

    private suspend fun saveToFileFullSize(context: Context, pool: BitmapPool, file: File) {
        // load full size
        var mat = withContext(IO) {
            GlideApp.with(context)
                    .asBitmap()
                    .load(draft.image)
                    .submit()
                    .get().let { bitmap ->
                        val mat = Mat()
                        Utils.bitmapToMat(bitmap, mat)
                        pool.put(bitmap) // clean up
                        mat
                    }
        }
        // do all the processes
        for (filter in history) {
            mat = filter.apply(mat)
        }

        val bitmap = pool.getDirty(mat.width(), mat.height(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(mat, bitmap)

        file.outputStream().use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
        }
    }


    /**
     * save to database
     */
    private suspend fun saveDraft() {
        withContext(IO) {
            draft.latestModifyTime = System.currentTimeMillis()
            draftRepository.saveDraft(draft)
        }
    }

    /**
     * delete the dart
     */
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

    /**
     * query draft by id
     */
    suspend fun getDraft(id: Long): Draft {
        return withContext(IO) { draftRepository.getDraftSimple(id) }
    }

    fun share(activity: Activity, pool: BitmapPool) {
        val file = createImageFile(savePath)
        GlobalScope.launch {
            saveToFileFullSize(activity, pool, file)
            saveToFileThumb(pool)
            saveDraft()
        }

        val uri = FileProvider.getUriForFile(activity, FILE_PROVIDER, file)

        val shareIntent = ShareCompat.IntentBuilder
                .from(activity)
                .setType(SHARE_IMAGE_MIME_TYPE)
                .setStream(uri)
                .intent

        if (shareIntent.resolveActivity(activity.packageManager) != null) {
            activity.startActivity(shareIntent)
        } else {
            Toast.makeText(activity, "can not share this file", Toast.LENGTH_SHORT).show()
        }
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
