package com.djfos.im.model


import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.IOException


class DraftDaoTest {
    private lateinit var draftDao: DraftDao
    private lateinit var db: AppDataBase

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDataBase::class.java).build()
        draftDao = db.draftDao()
    }


    @After
    @Throws(IOException::class)
    fun tearDown() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun insert() {
        val draft = Draft("http://example.com")
        val insertId = draftDao.insert(draft)
        val byId = draftDao.find(insertId)
        assertThat(byId, equalTo(draft))
    }
}