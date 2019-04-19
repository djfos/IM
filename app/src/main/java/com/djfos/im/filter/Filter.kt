package com.djfos.im.filter

import android.util.Log
import com.djfos.im.ui.AdjustPageFragment
import org.opencv.core.Mat
import java.lang.Exception
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance

interface IFilter {
    val type: FilterType
    fun apply(input: Mat): Mat
}

enum class FilterType {
    Gray, Thresh, Identity
}


@Target(AnnotationTarget.FIELD)
annotation class FilterControl(val controlType: ControlType)

enum class ControlType {
    Slider,
}

val typeMap: Map<FilterType, KClass<out IFilter>> = mapOf(
        FilterType.Gray to FilterGrayScale::class,
        FilterType.Thresh to FilterThreshold::class,
        FilterType.Identity to FilterIdentity::class
)

fun newInstanceFromType(type: FilterType): IFilter {
    val cls = typeMap[type]
    cls ?: throw Exception("apply: no class found that matches type $type")
    return cls.createInstance()
}