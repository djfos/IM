package com.djfos.im.viewModel


import android.graphics.Bitmap
import android.net.Uri

import com.djfos.im.model.Config
import com.djfos.im.util.CustomMutableLiveData

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AdjustPageViewModel : ViewModel() {
    var config = CustomMutableLiveData<Config>()

    var image = MutableLiveData<Bitmap>()

    var uri = MutableLiveData<Uri>()

    fun setThreshold(progress: Int) {
        config.value!!.threshold = progress
    }


    fun increase() {
        config.value!!.threshold = config.value!!.threshold + 1
    }

}
