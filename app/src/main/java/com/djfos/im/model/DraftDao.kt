package com.djfos.im.model

import androidx.room.*


@Dao
interface DraftDao {
    @Insert
    fun insert(draft: Draft): Long

    @Delete
    fun delete(draft: Draft): Int

    @Update
    fun update(draft: Draft): Int

    @Query("select * from draft where id =:id")
    fun find(id: Long): Draft
}