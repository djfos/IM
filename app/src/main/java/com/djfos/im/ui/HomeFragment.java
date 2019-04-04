package com.djfos.im.ui;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.djfos.im.R;
import com.djfos.im.databinding.FragmentHomeBinding;
import com.djfos.im.viewModel.SharedViewModel;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;


public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_PICK = 2;
    private static final int PERMISSIONS_WRITE_EXTERNAL_STORAGE = 3;
    private SharedViewModel model;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        FragmentHomeBinding binding = FragmentHomeBinding.inflate(inflater, container, false);
        model = ViewModelProviders.of(requireActivity()).get(SharedViewModel.class);

        binding.getRoot().<Button>findViewById(R.id.button_choose_image).setOnClickListener((view) -> takePhoto());

        binding.getRoot().<Button>findViewById(R.id.buttonPick).setOnClickListener((view) -> selectPhoto());

        return binding.getRoot();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult() called with: requestCode = [" + requestCode + "], resultCode = [" + resultCode + "], data = [" + data + "]");
        switch (requestCode) {
            case REQUEST_IMAGE_CAPTURE:
                if (resultCode == RESULT_OK) {
                    Uri uri = Uri.fromFile(model.image);
                    Log.d(TAG, "onActivityResult: uri " + uri);
                    Objects.requireNonNull(model.config.getValue()).setUri(uri);
                    model.image = null;
                    Navigation.findNavController(requireView()).navigate(R.id.action_homeFragment_to_adjustPage);
                }
                if (resultCode == RESULT_CANCELED) {
                    model.image = null;
                }
                break;
            case REQUEST_IMAGE_PICK:
                if (resultCode == RESULT_OK) {
                    Uri uri = (data.getData());
                    Log.d(TAG, "onActivityResult: uri " + uri);
                    Objects.requireNonNull(model.config.getValue()).setUri(uri);
                    model.image = null;

                    Navigation.findNavController(requireView()).navigate(R.id.action_homeFragment_to_adjustPage);
                    Log.d(TAG, "onActivityResult: nav ed");
                }
                if (resultCode == RESULT_CANCELED) {
                    model.image = null;
                }
                break;
        }
    }

    private void takePhoto() {
        if (!hasPermission()) {
            requestPermission();
        }

        if (hasPermission()) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (intent.resolveActivity(requireActivity().getPackageManager()) != null) {
                File photoFile = null;
                try {
                    photoFile = createImageFile();
                } catch (IOException ex) {
                    // Error occurred while creating the File
                    Log.e(TAG, "capturePhoto: ", ex);
                }
                // Continue only if the File was successfully created
                if (photoFile != null) {
                    Uri photoURI = FileProvider.getUriForFile(requireActivity(), "com.djfos.fileprovider", photoFile);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                    model.image = photoFile;
                    startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
                }
            }
        }
    }

    private File getPhotoPath() {
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File appImagePath = new File(path, "im");
        if (!appImagePath.exists()) {
            boolean mkdirs = appImagePath.mkdirs();
        }
        return appImagePath;
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd", Locale.CHINESE).format(new Date());
        File appImagePath = getPhotoPath();
        return File.createTempFile(timeStamp, ".jpg", appImagePath);
    }

    private void selectPhoto() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        if (intent.resolveActivity(requireActivity().getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_IMAGE_PICK);
        }
    }

    private boolean hasPermission() {
        return ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void doPermissionRequest() {
        this.requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSIONS_WRITE_EXTERNAL_STORAGE);
    }

    private void requestPermission() {
        if (hasPermission()) {
            return;
        }

        if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            new AlertDialog.Builder(requireActivity())
                    .setTitle("permission required!")
                    .setMessage("need storage permission to save the photo you take.")
                    .setPositiveButton("OK", (dialog, which) -> doPermissionRequest())
                    .setNegativeButton("Cancel", null)
                    .create()
                    .show();
        } else {
            doPermissionRequest();
        }
    }
}
