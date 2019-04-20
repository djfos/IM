package com.djfos.im.viewModel

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.djfos.im.model.Draft
import com.djfos.im.model.DraftRepository
import com.djfos.im.util.createImageFile
import com.djfos.im.util.thumbPath
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class HomePageViewModel(private val draftRepository: DraftRepository) : ViewModel() {
    val allDrafts: LiveData<List<Draft>> = draftRepository.allDrafts

    val multiSelection = MutableLiveData<Boolean>(false)

    @WorkerThread
    fun createDraft(uri: String): Long {
        val path = createImageFile(thumbPath)
        val draft = Draft(uri, path.absolutePath, System.currentTimeMillis())
        return draftRepository.saveDraft(draft)
    }

    fun deleteDrafts(ids: List<Long>) {
        GlobalScope.launch {
            draftRepository.dropDrafts(ids)
        }
    }
}

class HomePageViewModelFactory(private val repository: DraftRepository) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return HomePageViewModel(repository) as T
    }
}
