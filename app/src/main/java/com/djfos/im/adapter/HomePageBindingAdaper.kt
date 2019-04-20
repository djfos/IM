package com.djfos.im.adapter

import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.djfos.im.R
import com.djfos.im.util.GlideApp
import java.text.DateFormat
import java.util.*


@BindingAdapter("android:text")
fun setText(view: TextView, date: Long) {
    view.text = DateFormat.getDateInstance(DateFormat.LONG).format(Date(date))
}


@BindingAdapter("uri")
fun loadImage(view: ImageView, uri: String) {
    Log.d("loadImage", uri)
    GlideApp.with(view)
            .load(uri)
            .placeholder(R.drawable.ic_launcher_background)
            .error(R.drawable.ic_launcher_background)
            .into(view)
}