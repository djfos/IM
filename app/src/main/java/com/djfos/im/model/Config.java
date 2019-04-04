package com.djfos.im.model;

import com.djfos.im.BR;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;


public class Config extends BaseObservable {

    private int threshold;
    @Bindable
    public int getThreshold() {
        return threshold;
    }

    public void setThreshold(int threshold) {
        this.threshold = threshold;
        notifyPropertyChanged(BR.threshold);
    }
}


