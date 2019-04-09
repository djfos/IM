package com.djfos.im.filter;

import org.opencv.core.Mat;

abstract class BaseFilter {
    FilterType type = FilterType.UNDEFIND;

    Mat apply(Mat in) {
        return in;
    }
}
