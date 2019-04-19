package com.djfos.im.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.djfos.im.util.DATABASE_NAME

@Database(entities = [Draft::class], version = 1)
@TypeConverters(Converter::class)
abstract class AppDataBase : RoomDatabase() {
    abstract fun draftDao(): DraftDao

    companion object {
        @Volatile
        private var instance: AppDataBase? = null

        fun getInstance(context: Context): AppDataBase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context): AppDataBase {
            return Room.databaseBuilder(context, AppDataBase::class.java, DATABASE_NAME).build()
        }
    }
}