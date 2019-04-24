package com.djfos.im.model

import androidx.lifecycle.LiveData
import androidx.room.*
import com.djfos.im.filter.AbstractFilter
import com.djfos.im.filter.FilterIdentity
import kotlinx.coroutines.runBlocking
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

class DraftRepository private constructor(private val dao: DraftDao) {

    lateinit var allDrafts: LiveData<List<Draft>>

    init {
        runBlocking {
            allDrafts = dao.getAll()
        }
    }

    fun getDraft(id: Long): LiveData<Draft> = dao.getById(id)

    fun getDraftSimple(id: Long): Draft = dao.getByIdSimple(id)

    fun saveDraft(draft: Draft) = dao.insert(draft)

    fun dropDraft(draft: Draft) {
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


    companion object {
        @Volatile
        private var instance: DraftRepository? = null

        fun getInstance(draftDao: DraftDao) =
                instance ?: synchronized(this) {
                    instance ?: DraftRepository(draftDao).also { instance = it }
                }
    }

}

