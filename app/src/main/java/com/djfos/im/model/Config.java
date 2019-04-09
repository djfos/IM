package com.djfos.im.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.djfos.im.BR;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;


public class Config extends BaseObservable implements Parcelable {

    private int threshold;
    public Config(){

    }

    protected Config(Parcel in) {
        threshold = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(threshold);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Config> CREATOR = new Creator<Config>() {
        @Override
        public Config createFromParcel(Parcel in) {
            return new Config(in);
        }

        @Override
        public Config[] newArray(int size) {
            return new Config[size];
        }
    };

    @Bindable
    public int getThreshold() {
        return threshold;
    }

    public void setThreshold(int threshold) {
        this.threshold = threshold;
        notifyPropertyChanged(BR.threshold);
    }
}


