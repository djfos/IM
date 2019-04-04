package com.djfos.im.model;

import android.net.Uri;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import com.djfos.im.BR;


public class Config extends BaseObservable {

    private int threshold;
    private Uri uri;
    @Bindable
    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
        notifyPropertyChanged(BR.uri);

    }

    @Bindable
    public int getThreshold() {
        return threshold;
    }

    public void setThreshold(int threshold) {
        this.threshold = threshold;
        notifyPropertyChanged(BR.threshold);
    }
}


