package com.djfos.im.ui


import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button

import com.djfos.im.R
import com.djfos.im.databinding.FragmentHomeBinding
import com.djfos.im.model.Config
import com.djfos.im.model.Draft
import com.djfos.im.viewModel.AdjustPageViewModel

import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation

import android.app.Activity.RESULT_OK


class HomeFragment : Fragment() {
    private var model: AdjustPageViewModel? = null
    private var image: File? = null

    private val photoPath: File?
        get() {
            val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val appImagePath = File(path, "im")
            if (!appImagePath.exists()) {
                if (!appImagePath.mkdirs()) {
                    requestPermission()
                    return null
                }
            }
            return appImagePath
        }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val binding = FragmentHomeBinding.inflate(inflater, container, false)
        model = ViewModelProviders.of(requireActivity()).get(AdjustPageViewModel::class.java)

        binding.root.findViewById<Button>(R.id.button_choose_image).setOnClickListener { takePhoto() }

        binding.root.findViewById<Button>(R.id.buttonPick).setOnClickListener { selectPhoto() }

        return binding.root
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.d(TAG, "onActivityResult() called with: requestCode = [$requestCode], resultCode = [$resultCode], data = [$data]")
        when (requestCode) {
            REQUEST_IMAGE_CAPTURE -> if (resultCode == RESULT_OK) {
                val uri = Uri.fromFile(image)
                Log.d(TAG, "onActivityResult: uri $uri")
                //                    model.uri.setValue(uri);
                image = null

                val draft = Draft()
                draft.sourceImageUri = uri
                draft.config = Config()
                val action = HomeFragmentDirections.actionHomeFragmentToAdjustPage(draft)
                Navigation.findNavController(requireView()).navigate(action)
            }
            REQUEST_IMAGE_PICK -> if (resultCode == RESULT_OK) {
                val uri = data!!.data
                Log.d(TAG, "onActivityResult: uri " + uri!!)
                //                    model.uri.setValue(uri);

                val draft = Draft()
                draft.sourceImageUri = uri
                draft.config = Config()
                val action = HomeFragmentDirections.actionHomeFragmentToAdjustPage(draft)
                Navigation.findNavController(requireView()).navigate(action)
            }
        }
    }


    private fun takePhoto() {
        if (!hasPermission()) {
            requestPermission()
        }

        if (hasPermission()) {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (intent.resolveActivity(requireActivity().packageManager) != null) {
                var photoFile: File? = null
                try {
                    photoFile = createImageFile()
                } catch (ex: IOException) {
                    // Error occurred while creating the File
                    Log.e(TAG, "capturePhoto: ", ex)
                }

                // Continue only if the File was successfully created
                if (photoFile != null) {
                    val photoURI = FileProvider.getUriForFile(requireActivity(), "com.djfos.fileprovider", photoFile)
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    image = photoFile
                    startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd", Locale.CHINESE).format(Date())
        val appImagePath = photoPath
        return File.createTempFile(timeStamp, ".jpg", appImagePath)
    }

    private fun selectPhoto() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        if (intent.resolveActivity(requireActivity().packageManager) != null) {
            startActivityForResult(intent, REQUEST_IMAGE_PICK)
        }
    }

    private fun hasPermission(): Boolean {
        return ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }

    private fun doPermissionRequest() {
        this.requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), PERMISSIONS_WRITE_EXTERNAL_STORAGE)
    }

    private fun requestPermission() {
        if (hasPermission()) {
            return
        }

        if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            AlertDialog.Builder(requireActivity())
                    .setTitle("permission required!")
                    .setMessage("need storage permission to save the photo you take.")
                    .setPositiveButton("OK") { _, _ -> doPermissionRequest() }
                    .setNegativeButton("Cancel", null)
                    .create()
                    .show()
        } else {
            doPermissionRequest()
        }
    }

    companion object {
        private const val TAG = "HomeFragment"
        private const val REQUEST_IMAGE_CAPTURE = 1
        private const val REQUEST_IMAGE_PICK = 2
        private const val PERMISSIONS_WRITE_EXTERNAL_STORAGE = 3
    }
}
