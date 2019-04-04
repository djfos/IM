package com.djfos.im.viewModel;


import androidx.lifecycle.ViewModel;

import com.djfos.im.model.Config;
import com.djfos.im.util.CustomMutableLiveData;

import java.io.File;

public class SharedViewModel extends ViewModel {
    public CustomMutableLiveData<Config> config = new CustomMutableLiveData<>();

    public File image;

    SharedViewModel() {
        config.setValue(new Config());
    }

    public void setThreshold(int progress) {
        config.getValue().setThreshold(progress);
    }


    public void increase() {
        config.getValue().setThreshold(config.getValue().getThreshold() + 1);
    }


}
