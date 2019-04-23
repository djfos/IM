package com.djfos.im.util

import android.content.Context
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.SeekBar
import androidx.lifecycle.MutableLiveData
import com.djfos.im.filter.*
import java.lang.reflect.Field

/**
 * build one way binding ,ui -> data
 */
fun createControlPanel(
        context: Context,
        filter: AbstractFilter
): Pair<ViewGroup, MutableLiveData<AbstractFilter>> {
    //todo no reflection
    val layout = LinearLayout(context)
    layout.orientation = LinearLayout.VERTICAL

    val mediator = MutableLiveData<AbstractFilter>()

    for (field in filter::class.java.declaredFields) {
        field.isAccessible = true //make no private shouldStart!!

        field.getAnnotation(FilterControl::class.java)?.let {
            val view = when (it.controlType) {
                ControlType.Slider -> {
                    seekBar(context, filter, field, mediator)
                }
            }

            layout.addView(view)
        }

    }
    return Pair(layout, mediator)
}

private fun seekBar(
        context: Context,
        filter: AbstractFilter,
        field: Field,
        mediator: MutableLiveData<AbstractFilter>
): SeekBar {
    val value = field.get(filter) as Int
    val seekBar = SeekBar(context)
    //should have the same progress as filter
    seekBar.progress = value
    seekBar.max = 256
    seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(
                seekBar: SeekBar?,
                progress: Int,
                fromUser: Boolean
        ) {
            field.set(filter, progress)
            mediator.value = filter      //notify change
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {

        }

        override fun onStopTrackingTouch(seekBar: SeekBar?) {

        }

    })
    return seekBar
}


