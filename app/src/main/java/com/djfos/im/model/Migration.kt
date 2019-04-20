package com.djfos.im.model

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("alter table `draft` add column `thumb` text not null default '' ")
        database.execSQL("alter table `draft` add column `latestModifyTime` integer  not null default 0")
    }
}
