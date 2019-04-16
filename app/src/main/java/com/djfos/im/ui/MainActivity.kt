package com.djfos.im.ui

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.room.Room
import com.djfos.im.databinding.ActivityMainBinding
import com.djfos.im.model.AppDataBase
import org.opencv.android.OpenCVLoader


class MainActivity : AppCompatActivity() {
    val db: AppDataBase by lazy {
        Room.databaseBuilder(applicationContext, AppDataBase::class.java, "im-db").build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        val toolbar = binding.toolbar
        setSupportActionBar(toolbar)
        setContentView(binding.root)
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
