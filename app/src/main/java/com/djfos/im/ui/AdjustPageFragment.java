package com.djfos.im.ui;


import android.os.Bundle;

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
    static final String TAG = "AdjustPageFragment";

    private static final String ARG_PARAM1 = "uri";


    private String uri;

    private SharedViewModel model;
    private FragmentAdjustPageBinding binding;

    public static AdjustPageFragment newInstance(String uri) {
        AdjustPageFragment fragment = new AdjustPageFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, uri);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            uri = getArguments().getString(ARG_PARAM1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        binding = FragmentAdjustPageBinding.inflate(inflater, container, false);
        binding.setLifecycleOwner(this);
        model = ViewModelProviders.of( getActivity()).get(SharedViewModel.class);
        binding.setVariable(BR.vm, model);

        Processor processor = new Processor(this,binding.getRoot().<ViewSwitcher>findViewById(R.id.resultView), model.config.getValue());
        Log.i(TAG, "onCreateView: ");
        model.config.observe(this, (config) -> {
            processor.update();
        });

        return binding.getRoot();

    }


}
