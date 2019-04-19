package com.djfos.im.ui


import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.*
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.djfos.im.R
import com.djfos.im.databinding.FragmentHomeBinding
import com.djfos.im.model.DraftAdapter
import com.djfos.im.util.*
import com.djfos.im.util.Injector.provideHomePageViewModelFactory
import com.djfos.im.viewModel.HomePageViewModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException


class HomeFragment : Fragment() {
    private var image: File? = null
    private lateinit var viewModel: HomePageViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentHomeBinding.inflate(inflater, container, false)

        viewModel = ViewModelProviders
                .of(this, provideHomePageViewModelFactory(requireContext()))
                .get(HomePageViewModel::class.java)

        val draftAdapter = DraftAdapter()
        val linearLayoutManager = LinearLayoutManager(requireContext())

        viewModel.allDrafts.observe({ lifecycle }) { drafts -> draftAdapter.setDraft(drafts) }
        binding.draftList.apply {
            setHasFixedSize(true)
            layoutManager = linearLayoutManager
            adapter = draftAdapter
        }

        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onCreateOptionsMenu(
            menu: Menu,
            inflater: MenuInflater
    ) {
        Log.d(TAG, "onCreateOptionsMenu: ")
        inflater.inflate(R.menu.home_page_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_take_photo -> {
                takePhoto()
                true
            }
            R.id.action_pick_from_gallery -> {
                selectPhoto()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(
            requestCode: Int,
            resultCode: Int,
            data: Intent?
    ) {
        when (requestCode) {
            REQUEST_IMAGE_CAPTURE -> if (resultCode == RESULT_OK) {
                val uri = Uri.fromFile(image)
                Log.d(TAG, "onActivityResult: uri $uri")
                image = null
                toAdjustPage(uri)
            }
            REQUEST_IMAGE_PICK -> if (resultCode == RESULT_OK) {
                if (data == null) return
                val uri = data.data ?: return
                Log.d(TAG, "onActivityResult: uri $uri")
                toAdjustPage(uri)
            }
        }
    }

    private fun toAdjustPage(uri: Uri) {
        GlobalScope.launch {
            val id = viewModel.createDraft(uri.toString())
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToAdjustPage(id))
        }
    }


    private fun takePhoto() {
        requestPermission(requireActivity())
        if (!hasPermission(requireActivity()))
            return

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(requireActivity().packageManager) != null) {
            try {
                val photoFile = createImageFile(photoPath)
                val photoURI = FileProvider.getUriForFile(requireActivity(), FILE_PROVIDER, photoFile)
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                image = photoFile
                startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
            } catch (ex: IOException) {
                Log.e(TAG, "capturePhoto: ", ex)
            }
        }
    }


    private fun selectPhoto() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        if (intent.resolveActivity(requireActivity().packageManager) != null) {
            startActivityForResult(intent, REQUEST_IMAGE_PICK)
        }
    }


    companion object {
        private const val TAG = "HomeFragment"
        private const val REQUEST_IMAGE_CAPTURE = 1
        private const val REQUEST_IMAGE_PICK = 2
    }
}