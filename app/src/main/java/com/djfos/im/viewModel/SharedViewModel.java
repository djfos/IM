package com.djfos.im.viewModel;


import android.graphics.Bitmap;
import android.net.Uri;

import com.djfos.im.model.Config;
import com.djfos.im.util.CustomMutableLiveData;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SharedViewModel extends ViewModel {
    public CustomMutableLiveData<Config> config = new CustomMutableLiveData<>();

    public MutableLiveData<Bitmap> image = new MutableLiveData<>();

    public MutableLiveData<Uri> uri = new MutableLiveData<>();

    public void setThreshold(int progress) {
        config.getValue().setThreshold(progress);
    }


    public void increase() {
        config.getValue().setThreshold(config.getValue().getThreshold() + 1);
    }

}
