package com.djfos.im.ui


import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ViewSwitcher
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.djfos.im.R
import com.djfos.im.adapter.HistoryAdapter
import com.djfos.im.databinding.FragmentAdjustPageBinding
import com.djfos.im.databinding.HistoryPanelBinding
import com.djfos.im.databinding.ListItemHistoryBinding
import com.djfos.im.filter.FilterType
import com.djfos.im.filter.IFilter
import com.djfos.im.filter.newInstanceFromType
import com.djfos.im.filter.typeMap
import com.djfos.im.util.GlideApp
import com.djfos.im.util.Injector
import com.djfos.im.util.createControlPanel
import com.djfos.im.viewModel.AdjustPageViewModel
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.fragment_adjust_page.*
import org.opencv.android.Utils
import org.opencv.core.Mat


class AdjustPageFragment : Fragment() {
    private lateinit var viewModel: AdjustPageViewModel
    private lateinit var pool: BitmapPool
    private lateinit var viewSwitcher: ViewSwitcher
    private var previous: Bitmap? = null
    private lateinit var controlPanel: FrameLayout
    private lateinit var historyAdapter: HistoryAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentAdjustPageBinding.inflate(inflater, container, false)

        val args = AdjustPageFragmentArgs.fromBundle(requireArguments())
        val factory = Injector.provideAViewModelFactory(requireActivity().application, args.id)
        viewModel = ViewModelProviders.of(this, factory).get(AdjustPageViewModel::class.java)

        // set class properties
        viewSwitcher = binding.resultView
        controlPanel = binding.controlPanel
        pool = GlideApp.get(requireContext()).bitmapPool

        // set toolbar
        (requireActivity() as AppCompatActivity).apply {
            setSupportActionBar(binding.toolbar)
            supportActionBar!!.setDisplayShowTitleEnabled(false)
        }

        // set history button
        binding.buttonHistory.setOnClickListener { binding.drawerLayout.openDrawer(GravityCompat.START) }


        // set history panel
        historyAdapter = HistoryAdapter(callback = { index ->
            fallback(index)
        })
        binding.historyPanel.historyList.let {
            it.layoutManager = LinearLayoutManager(requireContext())
            it.adapter = historyAdapter
        }
        syncHistory()


        viewModel.mediator.observe({ lifecycle }) { filterHolder ->
            // subscribe to  change of  filter control
            filterHolder.observe({ lifecycle }) { filter ->
                draw(filter.apply(viewModel.previousResult))
            }
        }

        fallback(viewModel.history.lastIndex)
        setHasOptionsMenu(true)

        return binding.root
    }

    private fun syncHistory() {
        historyAdapter.setData(viewModel.history)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.adjust_page_menu, menu)
        val a = menu.findItem(R.id.filter_menu)
        typeMap.keys.forEach {
            a.subMenu.add(R.id.group_filter_menu, it.ordinal, Menu.NONE, it.toString())
        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.d(TAG, "onOptionsItemSelected ${item.groupId}  ${item.itemId}")

        return when (item.groupId) {
            R.id.group_adjust_page_action -> when (item.itemId) {
                R.id.done -> {
                    Log.d(TAG, "onOptionsItemSelected: save")
                    viewModel.save()
                    goHome()
                    true
                }
                R.id.drop -> {
                    Log.d(TAG, "onOptionsItemSelected: drop")
                    viewModel.drop()
                    goHome()
                    true
                }
                else -> super.onOptionsItemSelected(item)
            }
            R.id.group_filter_menu -> {
                val type = FilterType.valueOf(item.title.toString())
                apply(type)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    }

    /**
     * fallback somewhere in the history
     */
    private fun fallback(index: Int) {
        val filter = viewModel.fallback(index)
        apply(filter)
    }

    fun apply(type: FilterType) {
        val filter = newInstanceFromType(type)
        viewModel.currentResult?.let { viewModel.previousResult = it }
        viewModel.history.add(filter)
        apply(filter)
    }


    fun apply(filter: IFilter) {
        val (layout, mediator) = createControlPanel(requireContext(), filter)

        controlPanel.removeAllViews()
        controlPanel.addView(layout)

        viewModel.mediator.value = mediator
        mediator.value = filter

        syncHistory()
    }

    // todo use extension function
    private fun draw(mat: Mat) {
        viewModel.currentResult = mat

        if (previous != null) {
            //clean up
            pool.put(previous)
        }
        val bitmap = pool.getDirty(mat.width(), mat.height(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(mat, bitmap)
        val next = viewSwitcher.nextView as ImageView
        next.setImageBitmap(bitmap)
        viewSwitcher.showNext()

    }

    private fun goHome() {
        findNavController().navigate(HomeFragmentDirections.actionGlobalHomeFragment())
    }

    companion object {
        private const val TAG = "AdjustPageFragment"
    }
}



