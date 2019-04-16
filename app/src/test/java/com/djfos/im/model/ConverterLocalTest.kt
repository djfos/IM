package com.djfos.im.model

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.djfos.im.filter.FilterThreshold
import com.djfos.im.filter.IFilter
import com.google.gson.Gson
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert
import org.junit.Rule
import org.junit.Test


class ConverterLocalTest {
    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Test
    fun `history can convert to Json then retrieve`() {
        val converter = Converter()
        val history = mutableListOf<IFilter>()
        val a = FilterThreshold()
        a.threshold = 20
        history.add(a)
        val json = Gson().toJson(history)
        val res = converter.stringToHistoryTo(json)
        val actual = Gson().toJson(res)
        MatcherAssert.assertThat(actual, equalTo(json))
    }
}