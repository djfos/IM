package com.djfos.im.viewModel

import androidx.lifecycle.*
import com.djfos.im.filter.IFilter
import com.djfos.im.model.Draft
import com.djfos.im.model.DraftRepository
import org.opencv.core.Mat
import java.util.logging.Filter

class AdjustPageViewModel(
        private val draftRepository: DraftRepository,
        val draft: LiveData<Draft>
) : ViewModel() {

    lateinit var origin: Mat
    lateinit var previousResult: Mat
    lateinit var currentResutl: Mat
    lateinit var history: MutableList<IFilter>

    val mediator = MutableLiveData<MutableLiveData<IFilter>>()

    fun save(draft: Draft) {
        draftRepository.saveDraft(draft)
    }

    fun drop(draft: Draft) {
        draftRepository.dropDraft(draft)
    }

    /**
     * fallback somewhere in the history
     */
    fun fallback(index: Int): IFilter {
        check(history.isEmpty()) { "history,isEmpty" }
        require(index >= 0 && index < history.size) { "index $index out of bound, array size ${history.size}" }

        if (index != 0) {
            var mat = origin
            var i = 0
            while (i != index - 1) {
                mat = history[i].apply(mat)
                i++
            }

            previousResult = mat
        }
        return history[index]
    }
}


class AViewModelFactory(
        private val draftRepository: DraftRepository,
        private val draft: LiveData<Draft>
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AdjustPageViewModel(draftRepository, draft) as T
    }
}
