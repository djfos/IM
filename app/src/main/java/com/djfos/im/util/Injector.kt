package com.djfos.im.util

import android.content.Context
import com.djfos.im.model.AppDataBase
import com.djfos.im.model.DraftRepository
import com.djfos.im.viewModel.AViewModelFactory
import com.djfos.im.viewModel.HomePageViewModelFactory

object Injector {
    fun getDraftRepository(
            context: Context
    ): DraftRepository {
        return DraftRepository.getInstance(AppDataBase
                .getInstance(context.applicationContext).draftDao())
    }

    fun provideHomePageViewModelFactory(
            context: Context
    ): HomePageViewModelFactory {
        return HomePageViewModelFactory(getDraftRepository(context))
    }

    fun provideAViewModelFactory(
            context: Context,
            id: Long
    ): AViewModelFactory {
        val draftRepository = getDraftRepository(context)
        val draft = draftRepository.getDraft(id)
        return AViewModelFactory(draftRepository,draft)
    }


}