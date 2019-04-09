package com.djfos.im.ui


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ViewSwitcher

import com.djfos.im.BR
import com.djfos.im.R
import com.djfos.im.databinding.FragmentAdjustPageBinding
import com.djfos.im.model.Draft
import com.djfos.im.util.GlideApp
import com.djfos.im.util.Processor
import com.djfos.im.viewModel.AdjustPageViewModel
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController


class AdjustPageFragment : Fragment() {
    lateinit var draft: Draft

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)

        val binding = FragmentAdjustPageBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this

        val model = ViewModelProviders.of(this).get<AdjustPageViewModel>(AdjustPageViewModel::class.java)
        binding.setVariable(BR.vm, model)

        val pool = GlideApp.get(requireContext()).bitmapPool
        val viewSwitcher = binding.root.findViewById(R.id.resultView) as ViewSwitcher
        val processor = Processor(pool, viewSwitcher, model)

        draft = AdjustPageFragmentArgs.fromBundle(arguments!!).draft
        draft.onReady = { image ->
            model.config.value = draft.config
            model.image.value = image
            model.config.observe(this, Observer { processor.update() })
        }
        draft.load(this)


        return binding.root
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        Log.d(TAG, "onCreateOptionsMenu: inflate menu")
        inflater.inflate(R.menu.adjust_page_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.done -> {
                draft.save()
                Log.d(TAG, "onOptionsItemSelected: save")
                findNavController().navigate(R.id.action_global_homeFragment)
                true
            }

            R.id.drop -> {
                draft.drop()
                Log.d(TAG, "onOptionsItemSelected: drop")
                findNavController().navigate(R.id.action_global_homeFragment)
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    companion object {
        private val TAG = "AdjustPageFragment"
    }
}
