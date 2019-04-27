package com.djfos.im

import android.app.Application
import androidx.room.Room
import com.djfos.im.model.AppDataBase
import com.djfos.im.model.DraftRepository
import com.djfos.im.model.MIGRATION_1_2
import com.djfos.im.util.DATABASE_NAME
import com.djfos.im.viewModel.AdjustPageViewModel
import com.djfos.im.viewModel.HomePageViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module


class App : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()

            androidContext(this@App)

            modules(appModule)
        }
    }
}

val appModule = module {

    single {
        Room.databaseBuilder(androidContext(), AppDataBase::class.java, DATABASE_NAME)
                .addMigrations(MIGRATION_1_2)
                .build()
    }

    single {
        get<AppDataBase>().draftDao()
    }


    single {
        DraftRepository(get())
    }

    viewModel { HomePageViewModel(get()) }

    viewModel { AdjustPageViewModel(get()) }

}

