package com.djfos.im.filter

import com.google.gson.Gson
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc


class FilterThreshold : IFilter {
    override val type = FilterType.Thresh
    @FilterControl(ControlType.Slider)
    var threshold: Int = 125
    override fun apply(input: Mat): Mat {
        val mat = Mat()
        Imgproc.threshold(input, mat, threshold.toDouble(), 255.0, Imgproc.THRESH_BINARY)
        val gson = Gson()
        val filter = FilterThreshold()
        gson.toJson(filter)
        return mat
    }
}
