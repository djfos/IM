package com.djfos.im.util;

import android.graphics.Bitmap;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ViewSwitcher;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.djfos.im.model.Config;
import com.djfos.im.viewModel.AdjustPageViewModel;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.util.concurrent.TimeUnit;

import androidx.lifecycle.LifecycleObserver;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;

public class Processor implements LifecycleObserver {
    private static final String TAG = "Processor";
    private final ViewSwitcher viewSwitcher;
    private AdjustPageViewModel model;
    private BitmapPool pool;
    private PublishSubject<Integer> subject = PublishSubject.create();
    private Bitmap lastResult;

    public Processor(BitmapPool pool, ViewSwitcher vs, AdjustPageViewModel viewModel) {
        this.model = viewModel;
        this.pool = pool;
        viewSwitcher = vs;
        subject.throttleFirst(20, TimeUnit.MILLISECONDS)
                .subscribe(new Observer<Integer>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        Log.d(TAG, "onSubscribe() called with: d = [" + d + "]");
                    }

                    @Override
                    public void onNext(Integer integer) {
                        Log.d(TAG, "onNext() called with: integer = [" + integer + "]");
                        ImageView next = (ImageView) viewSwitcher.getNextView();
                        next.setImageBitmap(process());
                        viewSwitcher.showNext();
                        Log.d(TAG, "onNext: done");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "onError() called with: e = [" + e + "]");
                    }

                    @Override
                    public void onComplete() {
                        Log.d(TAG, "onComplete() called");
                    }
                });
    }

    public void update() {
        subject.onNext(0);
        Log.d(TAG, "update: ");
    }

    private Bitmap process() {
        if (lastResult != null) {
            pool.put(lastResult);
        }

        Bitmap image = model.getImage().getValue();
        Config config = model.getConfig().getValue();
        //
        Mat origin = new Mat();
        Utils.bitmapToMat(image, origin);
        //
        Mat gray = new Mat();
        Imgproc.cvtColor(origin, gray, Imgproc.COLOR_RGBA2GRAY);
        //
        Mat threshold = new Mat();
        Imgproc.threshold(gray, threshold, config.getThreshold(), 255, Imgproc.THRESH_BINARY);
        //
        Bitmap result = pool.getDirty(image.getWidth(), image.getHeight(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(threshold, result);

        lastResult = result;
        return result;
    }
}
