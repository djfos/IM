package com.djfos.im.model

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Parcel
import android.os.Parcelable

import com.bumptech.glide.request.target.CustomTarget
import com.djfos.im.util.GlideApp

import androidx.fragment.app.Fragment
import com.bumptech.glide.request.transition.Transition

class Draft() : Parcelable {
    var sourceImageUri: Uri? = null
    var config: Config? = null
    private lateinit var sourceImage: Bitmap
    var onReady: (image:Bitmap) -> Unit = {}

    constructor(parcel: Parcel) : this() {
        sourceImageUri = parcel.readParcelable(Uri::class.java.classLoader)
        config = parcel.readParcelable(Config::class.java.classLoader)

    }

    fun load(fragment: Fragment) {
        GlideApp.with(fragment)
                .asBitmap()
                .load(sourceImageUri)
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        sourceImage = resource
                        onReady(sourceImage)
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {

                    }
                })
    }


    fun save() {

    }

    fun drop() {

    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(sourceImageUri, flags)
        parcel.writeParcelable(config, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Draft> {
        override fun createFromParcel(parcel: Parcel): Draft {
            return Draft(parcel)
        }

        override fun newArray(size: Int): Array<Draft?> {
            return arrayOfNulls(size)
        }
    }
}
