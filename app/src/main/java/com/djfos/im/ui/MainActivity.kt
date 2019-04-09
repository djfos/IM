package com.djfos.im.ui

import android.os.Bundle
import android.util.Log
import android.view.View

import com.djfos.im.R

import org.opencv.android.OpenCVLoader

import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
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
        internal val TAG = "MainActivity"
    }
}
