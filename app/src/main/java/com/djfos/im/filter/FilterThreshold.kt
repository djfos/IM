package com.djfos.im.filter

import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc


class FilterThreshold : AbstractFilter() {
    override val type = FilterType.Thresh

    var threshold: Int = 125

    override fun apply(input: Mat): Mat {
        val mat = Mat()
        Imgproc.threshold(input, mat, threshold.toDouble(), 255.0, Imgproc.THRESH_BINARY)
        return mat
    }
}
