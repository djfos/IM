package com.djfos.im.ui;

import android.os.Bundle;
import android.util.Log;

import com.djfos.im.R;

import org.opencv.android.OpenCVLoader;

import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity {
    static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
        }
    }
}
