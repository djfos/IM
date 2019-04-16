package com.djfos.im.filter

import io.github.classgraph.ClassGraph
import org.opencv.core.Mat
import kotlin.reflect.KClass

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

val typeMap: MutableMap<FilterType, KClass<out IFilter>> by lazy {

    val pkg = "com.djfos.im.filter"
    val interfaceClass = IFilter::class.java.name
    val scanResult = ClassGraph()
            .enableAllInfo()
            .whitelistPackages(pkg)
            .scan()

    val list = scanResult.getClassesImplementing(interfaceClass)
    val map = mutableMapOf<FilterType, KClass<out IFilter>>()
    for (it in list) {
        val iFilter = it.loadClass(IFilter::class.java).newInstance()
        val type = iFilter.type
        map[type] = iFilter::class
    }

    scanResult.close()
    map
}