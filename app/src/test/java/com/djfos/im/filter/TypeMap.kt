package com.djfos.im.filter

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

class TypeMap {
    @Test
    fun `get class type from filter type`() {
        val actual = filterInfos
        assertThat(
                (actual[FilterType.Gray] ?: error("null")).cls.java.name,
                equalTo(FilterGrayScale::class.java.name)
        )
    }
}