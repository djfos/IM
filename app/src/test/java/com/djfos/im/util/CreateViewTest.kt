package com.djfos.im.util

import android.content.Context
import android.widget.SeekBar
import androidx.core.view.get
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.djfos.im.filter.FilterThreshold
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class CreateViewTest {

    lateinit var filter: FilterThreshold

    val context: Context = ApplicationProvider.getApplicationContext()

    @Before
    fun init() {
        filter = FilterThreshold()
        filter.threshold = 30
    }

    @Test
    fun layout_should_have_only_one_child_view() {
        val (layout, liveData) = createView(context, filter)
        assertThat(layout.childCount, equalTo(1))
    }

    @Test
    fun `the child view should be a seek bar`() {
        val (layout, liveData) = createView(context, filter)
        assertThat(layout[0] is SeekBar, equalTo(true))
    }

    @Test
    fun `seek bar should have the same progress as filter`() {
        val (layout, liveData) = createView(context, filter)
        val seekBar = layout[0] as SeekBar
        assertThat(seekBar.progress, equalTo(30))
    }

    @Test
    fun `when seek bar progress change,callback should be called`() {
        val (layout, liveData) = createView(context, filter)
        val seekBar = layout[0] as SeekBar

        var number = 0
        liveData.observeForever { number++ }
        seekBar.progress = 66

        assertThat(number, equalTo(1))
    }

    @Test
    fun `when seek bar progress change,field should be synchronized`() {
        val (layout, liveData) = createView(context, filter)
        val seekBar = layout[0] as SeekBar

        var number = 0
        liveData.observeForever { number++ }
        seekBar.progress = 66

        assertThat(filter.threshold, equalTo(66))
    }
}