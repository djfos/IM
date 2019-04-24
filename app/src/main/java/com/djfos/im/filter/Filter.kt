package com.djfos.im.filter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.lifecycle.MutableLiveData
import com.djfos.im.R
import com.djfos.im.databinding.ControlSeekbarBinding
import org.opencv.core.Mat
import kotlin.reflect.KClass

/**
 * only hold data for filtering
 */
abstract class AbstractFilter {
    abstract val type: FilterType
    abstract fun apply(input: Mat): Mat

    override fun toString(): String {
        return type.toString()
    }
}

/**
 * represent the key of filters
 */
enum class FilterType {
    Gray, Thresh, Identity
}


/**
 * lazy cached array of all filter type enums,used to get enum from its ordinal
 */
val FilterTypeValues by lazy { FilterType.values() }


data class FilterInfo(
        val cls: KClass<out AbstractFilter>,
        val name: String,
        val createInstance: () -> AbstractFilter,
        val createControlPanel: (controlPanel: ViewGroup, filter: AbstractFilter) -> Unit = { _, _ -> },
        val showInMenu: Boolean = true,
        val valid: (input: Mat) -> Boolean = { true }
)

/**
 * filter metadata
 */
val filterInfos: Map<FilterType, FilterInfo> = mapOf(
        FilterType.Identity to FilterInfo(
                cls = FilterIdentity::class,
                createInstance = { FilterIdentity() },
                name = "Identity",
                showInMenu = true
        ),
        FilterType.Gray to FilterInfo(
                cls = FilterGrayScale::class,
                createInstance = { FilterGrayScale() },
                name = "GrayScale",
                valid = { input -> input.channels() >= 3 }
        ),
        FilterType.Thresh to FilterInfo(
                cls = FilterThreshold::class,
                createInstance = { FilterThreshold() },
                name = "Threshold"

        )
)

/**
 * create filter control,always return a LiveData that emits new filter state.
 */
fun buildFilterControl(filter: AbstractFilter): (controlPanel: ViewGroup) -> MutableLiveData<AbstractFilter> {
    return when (filter.type) {
        FilterType.Thresh -> {
            { controlPanel ->
                val mediator = MutableLiveData<AbstractFilter>()
                if (filter is FilterThreshold) {
                    controlPanel.removeAllViews()
                    val binding = ControlSeekbarBinding.inflate(
                            LayoutInflater.from(controlPanel.context),
                            controlPanel,
                            true
                    )

                    binding.textTitle.setText(R.string.threshold)
                    binding.value = filter.threshold
                    binding.seekBar.apply {
                        max = 257 // -1 ~ 256
                        progress = filter.threshold
                    }


                    binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                            Log.d("onProgressChanged", "onProgressChanged: $progress")
                            filter.threshold = progress - 1
                            binding.value = progress - 1
                            mediator.value = filter
                        }

                        override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                        override fun onStopTrackingTouch(seekBar: SeekBar?) {}
                    })
                }

                mediator
            }
        }
        else -> {
            { controlPanel ->
                controlPanel.removeAllViews() // no need for control
                MutableLiveData()
            }
        }
    }
}






