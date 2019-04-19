package com.djfos.im.viewModel

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.djfos.im.model.Draft
import com.djfos.im.model.DraftRepository


class HomePageViewModel(
        private val draftRepository: DraftRepository
) : ViewModel() {
    val allDrafts: LiveData<List<Draft>> = draftRepository.allDrafts

    @WorkerThread
    fun createDraft(uri: String) :Long {
        val draft = Draft(uri)
        return draftRepository.saveDraft(draft)
    }
}

class HomePageViewModelFactory(
        private val repository: DraftRepository
) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return HomePageViewModel(repository) as T
    }
}
