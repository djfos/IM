package com.djfos.im.filter

import org.opencv.core.Mat

class FilterIdentity : AbstractFilter() {
    override val type: FilterType = FilterType.Identity
    override fun apply(input: Mat): Mat {
        return input
    }
}