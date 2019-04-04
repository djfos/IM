package com.djfos.im.ui;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.djfos.im.R;
import com.djfos.im.viewModel.SharedViewModel;
import com.djfos.im.BR;
import com.djfos.im.util.Processor;
import com.djfos.im.databinding.FragmentAdjustPageBinding;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ViewSwitcher;


public class AdjustPageFragment extends Fragment {
    private static final String TAG = "AdjustPageFragment";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FragmentAdjustPageBinding binding = FragmentAdjustPageBinding.inflate(inflater, container, false);
        binding.setLifecycleOwner(this);
        SharedViewModel model = ViewModelProviders.of(requireActivity()).get(SharedViewModel.class);
        binding.setVariable(BR.vm, model);
        Processor processor = new Processor(this, binding.getRoot().<ViewSwitcher>findViewById(R.id.resultView), model.config.getValue());
        getLifecycle().addObserver(processor);
        model.config.observe(this, (config) -> {
            processor.update();
//            Log.d(TAG, "onCreateView: update" + config.getThreshold());
        });

        return binding.getRoot();

    }

}
