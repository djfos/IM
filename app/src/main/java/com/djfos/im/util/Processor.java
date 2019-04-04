package com.djfos.im.util;

import android.graphics.drawable.Drawable;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.OnLifecycleEvent;

import com.djfos.im.model.Config;

import android.util.Log;
import android.widget.ImageView;
import android.widget.ViewSwitcher;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;

public class Processor implements LifecycleObserver {
    static final String TAG = "Processor";
    private final ViewSwitcher viewSwitcher;
    private RequestOptions options;
    private GlideRequest<Drawable> requestBuilder;
    private boolean first = true;
    private RequestListener<Drawable> ls;
    Lifecycle lifecycle;

    public Processor(Fragment fragment, ViewSwitcher vs, Config config) {
        lifecycle = fragment.getLifecycle();
        viewSwitcher = vs;
        ls = getListener();
        options = new RequestOptions();
        Transformation transformation = new Transformation(config);
        options.transform(transformation);
        requestBuilder = GlideApp.with(fragment)
                .load(config.getUri())
                .skipMemoryCache(true);

    }

    public void update() {
        if (first) {
            ImageView current = (ImageView) viewSwitcher.getCurrentView();
            requestBuilder
                    .apply(options)
                    .into(current);
            first = false;
        } else {
            ImageView current = (ImageView) viewSwitcher.getCurrentView();
            ImageView next = (ImageView) viewSwitcher.getNextView();
            options.override(current.getWidth(), current.getHeight());
            requestBuilder
                    .apply(options)
                    .addListener(ls)
                    .into(next);
        }
    }


    private RequestListener<Drawable> getListener() {
        return new RequestListener<Drawable>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                viewSwitcher.showNext();
                Log.d(TAG, "onResourceReady: switched");

                return false;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                viewSwitcher.showNext();
                Log.d(TAG, "onResourceReady: switched");
                return false;
            }
        };
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    private void reset(){
        first = false;
        Log.d(TAG, "reset: yeah");
    }
}
