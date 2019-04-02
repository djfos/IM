package com.djfos.im.ui;


import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.djfos.im.R;
import com.djfos.im.databinding.FragmentHomeBinding;
import com.djfos.im.viewModel.SharedViewModel;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


public class HomeFragment extends Fragment {
    FragmentHomeBinding binding;
    private SharedViewModel model;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        model = ViewModelProviders.of(getActivity()).get(SharedViewModel.class);

        binding.getRoot().<Button>findViewById(R.id.button_choose_image).setOnClickListener((view) -> {
            ((MainActivity) getActivity()).takePhoto();

//            Navigation.findNavController(view).navigate(R.id.action_homeFragment_to_adjustPage);
        });

        binding.getRoot().<Button>findViewById(R.id.buttonPick).setOnClickListener((view) -> {
            ((MainActivity) getActivity()).selectPhoto();

//            Navigation.findNavController(view).navigate(R.id.action_homeFragment_to_adjustPage);
        });


        return binding.getRoot();
    }




}
