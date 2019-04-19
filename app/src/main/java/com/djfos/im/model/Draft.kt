package com.djfos.im.model

import androidx.lifecycle.LiveData
import androidx.room.*
import com.djfos.im.filter.IFilter
import kotlinx.coroutines.runBlocking

@Entity
data class Draft(val sourceImageUriString: String) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
    var history: MutableList<IFilter> = mutableListOf()
}


@Dao
interface DraftDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(draft: Draft): Long

    @Delete
    fun delete(draft: Draft)

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

    fun dropDraft(draft: Draft) = dao.delete(draft)

    companion object {
        @Volatile
        private var instance: DraftRepository? = null

        fun getInstance(draftDao: DraftDao) =
                instance ?: synchronized(this) {
                    instance ?: DraftRepository(draftDao).also { instance = it }
                }
    }

}

