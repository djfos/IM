package com.djfos.im.model

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.room.*
import com.djfos.im.filter.AbstractFilter
import com.djfos.im.filter.FilterIdentity
import java.io.File

@Entity
data class Draft(
        @ColumnInfo(name = "sourceImageUriString")
        val image: String,
        val thumb: String,
        var latestModifyTime: Long
) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
    var history: MutableList<AbstractFilter> = mutableListOf(FilterIdentity())
}


@Dao
interface DraftDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(draft: Draft): Long


    @Query("delete from draft where id in (:ids)")
    fun delete(ids: List<Long>)

    @Query("select * from draft where id =:id")
    fun getById(id: Long): LiveData<Draft>

    @Query("select * from draft where id =:id")
    fun getByIdSimple(id: Long): Draft

    @Query("select * from draft")
    fun getAll(): LiveData<List<Draft>>
}

class DraftRepository(private val dao: DraftDao) {

    val allDrafts: LiveData<List<Draft>> = dao.getAll()

    fun getDraft(id: Long): LiveData<Draft> = dao.getById(id)

    fun getDraftSimple(id: Long): Draft = dao.getByIdSimple(id)

    fun saveDraft(draft: Draft): Long {
        Log.d("DraftRepository", "saveDraft: $")
        return dao.insert(draft)
    }

    fun dropDraft(draft: Draft) {
        Log.d("DraftRepository", "dropDraft: $")
        deleteThumb(draft)
        dao.delete(listOf(draft.id))
    }

    fun dropDrafts(drafts: List<Draft>) {
        drafts.forEach { deleteThumb(it) }
        dao.delete(drafts.map { it.id })
    }

    private fun deleteThumb(draft: Draft) {
        File(draft.thumb).apply { if (exists()) delete() }
    }
}

