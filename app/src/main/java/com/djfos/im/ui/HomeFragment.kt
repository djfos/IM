package com.djfos.im.ui


import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.LinearLayoutManager
import com.djfos.im.R
import com.djfos.im.adapter.DraftAdapter
import com.djfos.im.adapter.DraftLookup
import com.djfos.im.adapter.RecyclerViewIdKeyProvider
import com.djfos.im.databinding.FragmentHomeBinding
import com.djfos.im.util.*
import com.djfos.im.util.Injector.provideHomePageViewModelFactory
import com.djfos.im.viewModel.HomePageViewModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException


class HomeFragment : Fragment() {
    private lateinit var mTracker: SelectionTracker<Long>
    private var image: File? = null
    private var actionMode: ActionMode? = null
    private lateinit var viewModel: HomePageViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentHomeBinding.inflate(inflater, container, false)

        viewModel = ViewModelProviders
                .of(this, provideHomePageViewModelFactory(requireContext()))
                .get(HomePageViewModel::class.java)
        // set menu
        (requireActivity() as AppCompatActivity).apply {
            setSupportActionBar(binding.toolbar)
            supportActionBar!!.setDisplayShowTitleEnabled(false)
        }

        val draftAdapter = DraftAdapter()
        val linearLayoutManager = LinearLayoutManager(requireContext())

        viewModel.allDrafts.observe({ lifecycle }) { drafts -> draftAdapter.setDraft(drafts) }

        binding.draftList.apply {
            setHasFixedSize(true)
            layoutManager = linearLayoutManager
            adapter = draftAdapter

            // add multi select support
            mTracker = SelectionTracker.Builder<Long>(
                    "draft-list-selection",
                    this,
                    RecyclerViewIdKeyProvider(this),
                    DraftLookup(this),
                    StorageStrategy.createLongStorage()
            ).withSelectionPredicate(
                    SelectionPredicates.createSelectAnything()
            ).build()
        }

        // report the change to viewModel
        mTracker.addObserver(selectionObserver)


        // synchronize multiSelection state between SelectionTracker and actionMode
        viewModel.multiSelection.observe(this, Observer { hasSelection ->
            if (hasSelection) {
                if (actionMode == null) {
                    actionMode = requireActivity().startActionMode(actionModeCallback)
                    Log.d(TAG, "multiSelection start")
                }
            } else {
                actionMode?.apply {
                    finish()
                    mTracker.clearSelection()
                    actionMode = null
                    Log.d(TAG, "multiSelection finish")
                }
            }
        })

        draftAdapter.tracker = mTracker

        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (savedInstanceState != null) {
            mTracker.onRestoreInstanceState(savedInstanceState)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mTracker.onSaveInstanceState(outState)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
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
        requestPermission(requireActivity())
        if (!hasPermission(requireActivity()))
            return

        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        if (intent.resolveActivity(requireActivity().packageManager) != null) {
            startActivityForResult(intent, REQUEST_IMAGE_PICK)
        }
    }


    private val selectionObserver = object : SelectionTracker.SelectionObserver<Long>() {
        override fun onSelectionRestored() {
            viewModel.multiSelection.value = mTracker.hasSelection()
        }

        override fun onSelectionChanged() {
            viewModel.multiSelection.value = mTracker.hasSelection()
        }
    }

    private val actionModeCallback = object : ActionMode.Callback {
        // Called when the action mode is created; startActionMode() was called
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            // Inflate a menu resource providing context menu items
            val inflater: MenuInflater = mode.menuInflater
            inflater.inflate(R.menu.home_page_action_mode, menu)
            return true
        }

        // Called each time the action mode is shown. Always called after onCreateActionMode, but
        // may be called multiple times if the mode is invalidated.
        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            return false // Return false if nothing is done
        }

        // Called when the user selects a contextual menu item
        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            return when (item.itemId) {
                R.id.delete -> {
                    viewModel.deleteDrafts(mTracker.selection.toList())
                    viewModel.multiSelection.value = false
                    true
                }
                else -> false
            }
        }

        // Called when the user exits the action mode
        override fun onDestroyActionMode(mode: ActionMode) {

        }
    }


    companion object {
        private const val TAG = "HomeFragment"
        private const val REQUEST_IMAGE_CAPTURE = 1
        private const val REQUEST_IMAGE_PICK = 2
    }
}


