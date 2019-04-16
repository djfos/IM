package com.djfos.im.util

import android.content.Context
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.SeekBar
import androidx.lifecycle.MutableLiveData
import com.djfos.im.filter.ControlType
import com.djfos.im.filter.FilterControl
import com.djfos.im.filter.IFilter

/**
 * build one way binding ,ui -> data
 */
fun createView(context: Context, filter: IFilter): Pair<ViewGroup, MutableLiveData<Any>> {
    val layout = LinearLayout(context)
    layout.orientation = LinearLayout.VERTICAL

    val mediator = MutableLiveData<Any>()

    val fields = filter.javaClass.declaredFields
    for (field in fields) {
        field!! // why it could be null ??
        field.isAccessible = true //make no private first!!
        if (!field.isAnnotationPresent(FilterControl::class.java))
            continue

        val annotation = field.getAnnotation(FilterControl::class.java)!!

        val view = when (annotation.controlType) {
            ControlType.Slider -> {
                check(field.get(filter) is Int) { "field expected to be a Int" }
                val seekBar = SeekBar(context)
                //should have the same progress as filter
                seekBar.progress = field.getInt(filter)
                seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                        field.set(filter, progress)
                        mediator.value = 0      //notify change
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar?) {

                    }

                    override fun onStopTrackingTouch(seekBar: SeekBar?) {

                    }

                })
                seekBar
            }
        }


        layout.addView(view)
    }


    return Pair(layout, mediator)
}