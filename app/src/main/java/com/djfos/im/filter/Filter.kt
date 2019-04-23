package com.djfos.im.filter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.Observable
import androidx.databinding.ObservableInt
import androidx.lifecycle.MutableLiveData
import com.djfos.im.databinding.ControlSeekbarBinding
import org.opencv.core.Mat
import kotlin.reflect.KClass

abstract class AbstractFilter {
    abstract val type: FilterType
    abstract fun apply(input: Mat): Mat

    override fun toString(): String {
        return type.toString()
    }
}

enum class FilterType {
    Gray, Thresh, Identity
}


enum class ControlType {
    Slider,
}

val FilterTypeValues by lazy { FilterType.values() }

@Target(AnnotationTarget.FIELD)
annotation class FilterControl(val controlType: ControlType)

data class FilterInfo(
        val cls: KClass<out AbstractFilter>,
        val name: String,
        val createInstance: () -> AbstractFilter,
        val createControlPanel: (controlPanel: ViewGroup, filter: AbstractFilter) -> Unit = { _, _ -> },
        val showInMenu: Boolean = true,
        val valid: (input: Mat) -> Boolean = { true }
)


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


fun buildFilterControl(filter: AbstractFilter): (controlPanel: ViewGroup) -> MutableLiveData<AbstractFilter> {
    return when (filter.type) {
        FilterType.Thresh -> {
            { controlPanel ->
                val mediator = MutableLiveData<AbstractFilter>()
                if (filter is FilterThreshold) {
                    val binding = ControlSeekbarBinding.inflate(
                            LayoutInflater.from(controlPanel.context),
                            controlPanel,
                            true
                    )

                    val observableInt = ObservableInt()

                    binding.title = "threshold"
                    binding.max = 256
                    binding.value = observableInt

                    observableInt.set(filter.threshold)

                    observableInt.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
                        override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                            filter.threshold = observableInt.get()
                            mediator.value = filter
                        }
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






