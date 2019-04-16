package com.djfos.im.model

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [Draft::class], version = 1)
@TypeConverters(Converter::class)
abstract class AppDataBase : RoomDatabase() {
    abstract fun draftDao(): DraftDao

}