package com.djfos.im.model

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.djfos.im.filter.FilterGrayScale
import com.djfos.im.filter.FilterIdentity
import com.djfos.im.filter.FilterThreshold
import com.djfos.im.filter.IFilter
import com.google.gson.Gson
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ConverterLocalTest {

    @Test
    fun `history can convert to Json then retrieve`() {
        val converter = Converter()
        val history = mutableListOf<IFilter>()
        history.add(FilterIdentity())
        history.add(FilterGrayScale())

        val a = FilterThreshold()
        a.threshold = 20
        history.add(a)
        val json = Gson().toJson(history)
        val res = converter.stringToHistoryTo(json)
        val actual = Gson().toJson(res)
        MatcherAssert.assertThat(actual, equalTo(json))
    }
}