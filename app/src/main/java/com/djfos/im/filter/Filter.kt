package com.djfos.im.filter

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
        val showInMenu: Boolean = true,
        val valid: (input: Mat) -> Boolean = { true }
)


val filterInfos: Map<FilterType, FilterInfo> = mapOf(
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
        ),
        FilterType.Identity to FilterInfo(
                cls = FilterIdentity::class,
                createInstance = { FilterIdentity() },
                name = "Identity",
                showInMenu = true
        )
)




