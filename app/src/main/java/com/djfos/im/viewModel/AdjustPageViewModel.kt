package com.djfos.im.viewModel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.djfos.im.filter.IFilter
import com.djfos.im.model.Draft
import com.djfos.im.model.DraftRepository
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.opencv.core.Mat

private const val TAG = "AdjustPageViewModel"

class AdjustPageViewModel(
        private val draftRepository: DraftRepository,
        private val draft: Draft,
        private val origin: Mat
) : ViewModel() {

    var previousResult: Mat = origin
    var currentResult: Mat? = null

    var history: MutableList<IFilter> = draft.history


    val mediator = MutableLiveData<MutableLiveData<IFilter>>()

    fun save() {
        GlobalScope.launch {
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
    fun fallback(index: Int): IFilter {
        Log.d(TAG, "fallback() called with: index = [$index]")
        require(index >= 0 && index < history.size) { "index $index out of bound, array size ${history.size}" }

        var i = 0
        var mat = origin

        while (i < index) {
            mat = history[i].apply(mat)
            i++
        }

        previousResult = mat

        return history[i]
    }
}


class AViewModelFactory(
        private val draftRepository: DraftRepository,
        private val draft: Draft,
        private val origin: Mat
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AdjustPageViewModel(draftRepository, draft, origin) as T
    }
}
