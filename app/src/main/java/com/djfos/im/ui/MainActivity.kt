package com.djfos.im.ui

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.djfos.im.R
import org.opencv.android.OpenCVLoader


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }


    public override fun onResume() {
        super.onResume()
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization")
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!")
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}
