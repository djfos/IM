package com.djfos.im

import androidx.lifecycle.MutableLiveData
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LiveDataStudy {
    @Test
    fun `set data after observe,callback triggered once`() {
        var number = 0
        val mutableLiveData = MutableLiveData<Int>()
        mutableLiveData.observeForever { number++ }
        mutableLiveData.value = 6
        assertThat(number,equalTo(1))
    }

    @Test
    fun `set data before observe,callback triggered once`() {
        var number = 0
        val mutableLiveData = MutableLiveData<Int>()
        mutableLiveData.observeForever { number++ }
        mutableLiveData.value = 6
        assertThat(number,equalTo(1))
    }

}