package com.djfos.im.util;

import android.graphics.Bitmap;
import androidx.annotation.NonNull;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;

import java.security.MessageDigest;


import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import com.djfos.im.model.Config;

public class Transformation extends BitmapTransformation {
    static final String ID = "Transformation";
    private final Config config;

    public Transformation(Config config) {
        this.config = config;
    }

    @Override
    protected Bitmap transform(@NonNull BitmapPool pool, @NonNull Bitmap toTransform, int outWidth, int outHeight) {
        Mat origin = new Mat();
        Utils.bitmapToMat(toTransform,origin);
        Mat gray = new Mat();

        Imgproc.cvtColor(origin,gray,Imgproc.COLOR_RGBA2GRAY);
        Mat threshold = new Mat();
        Imgproc.threshold(gray,threshold,config.getThreshold(),255,Imgproc.THRESH_BINARY);
        Utils.matToBitmap(threshold,toTransform);


        return toTransform;
    }

    @Override
    public void updateDiskCacheKey(@NonNull MessageDigest messageDigest) {

    }
}
