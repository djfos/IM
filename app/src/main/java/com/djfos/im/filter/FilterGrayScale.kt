package com.djfos.im.filter


import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc


class FilterGrayScale : AbstractFilter() {
    override val type = FilterType.Gray
    override fun apply(input: Mat): Mat {
        val mat = Mat()
        Imgproc.cvtColor(input, mat, Imgproc.COLOR_RGBA2GRAY)
        return mat
    }
}


