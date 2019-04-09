package com.djfos.im.filter;

import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

public class FilterThreshold extends BaseFilter{
    public int threshold;
    FilterThreshold(){
        type = FilterType.THRESHOLD;
    }
    @Override
    Mat apply(Mat in) {
        Mat mat = new Mat();
        Imgproc.threshold(in, mat, threshold, 255, Imgproc.THRESH_BINARY);
        return mat;
    }
}
