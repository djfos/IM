package com.djfos.im.ui;


import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.djfos.im.BR;
import com.djfos.im.R;
import com.djfos.im.databinding.FragmentAdjustPageBinding;
import com.djfos.im.model.Config;
import com.djfos.im.util.GlideApp;
import com.djfos.im.util.Processor;
import com.djfos.im.viewModel.SharedViewModel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;


public class AdjustPageFragment extends Fragment {
    private static final String TAG = "AdjustPageFragment";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FragmentAdjustPageBinding binding = FragmentAdjustPageBinding.inflate(inflater, container, false);
        binding.setLifecycleOwner(this);

        SharedViewModel model = ViewModelProviders.of(requireActivity()).get(SharedViewModel.class);
        binding.setVariable(BR.vm, model);


        BitmapPool pool = GlideApp.get(requireContext()).getBitmapPool();
        Processor processor = new Processor(pool, binding.getRoot().findViewById(R.id.resultView), model);

        model.config.observe(this, (config) -> processor.update());

        model.uri.observe(this, (uri) -> GlideApp.with(this)
                .asBitmap()
                .load(uri)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        model.image.setValue(resource);
                        model.config.setValue(new Config());
                        processor.update();
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {

                    }
                }));

        return binding.getRoot();

    }

}
