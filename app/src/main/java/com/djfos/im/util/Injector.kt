package com.djfos.im.util

import android.app.Application
import android.content.Context
import com.djfos.im.model.AppDataBase
import com.djfos.im.model.Draft
import com.djfos.im.model.DraftRepository
import com.djfos.im.viewModel.AViewModelFactory
import com.djfos.im.viewModel.HomePageViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.opencv.android.Utils
import org.opencv.core.Mat

object Injector {
    private fun getDraftRepository(context: Context): DraftRepository {
        return DraftRepository.getInstance(AppDataBase
                .getInstance(context.applicationContext).draftDao())
    }

    fun provideHomePageViewModelFactory(context: Context): HomePageViewModelFactory {
        return HomePageViewModelFactory(getDraftRepository(context))
    }

    fun provideAViewModelFactory(application: Application, id: Long): AViewModelFactory {

        val repo = getDraftRepository(application)
        val draft: Draft = runBlocking(Dispatchers.IO) { repo.getDraftSimple(id) }
        val width = application.resources.displayMetrics.widthPixels
        val height = application.resources.displayMetrics.heightPixels
        val origin: Mat = runBlocking(Dispatchers.IO) {
            GlideApp.with(application)
                    .asBitmap()
                    .load(draft.image)
                    .override(width, height)
                    .submit()
                    .get().let { bitmap ->
                        val mat = Mat()
                        Utils.bitmapToMat(bitmap, mat)
                        GlideApp.get(application).bitmapPool.put(bitmap)
                        mat
                    }
        }

        return AViewModelFactory(repo, draft, origin)
    }

}