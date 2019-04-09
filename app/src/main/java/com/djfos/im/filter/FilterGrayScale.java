package com.djfos.im.filter;


import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

public class FilterGrayScale extends BaseFilter {
    FilterGrayScale() {
        type = FilterType.GARY_SCALE;
    }

    @Override
    Mat apply(Mat in) {
        Mat mat = new Mat();
        Imgproc.cvtColor(in, mat, Imgproc.COLOR_RGBA2GRAY);
        return mat;
    }
}
