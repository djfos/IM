package com.djfos.im.ui


import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.FrameLayout
import android.widget.ImageView
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
import com.djfos.im.filter.FilterType
import com.djfos.im.filter.FilterTypeValues
import com.djfos.im.filter.filterInfos
import com.djfos.im.util.GlideApp
import com.djfos.im.util.Injector
import com.djfos.im.util.createControlPanel
import com.djfos.im.viewModel.AdjustPageViewModel
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
            binding.drawerLayout.closeDrawer(GravityCompat.START)
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
                Log.d("observe", "filter: $filter")
                viewModel.currentFilter = filter
            }
        }

        fallback(viewModel.history.lastIndex)
        setHasOptionsMenu(true)

        return binding.root
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.adjust_page_menu, menu)
        val filterMenu = menu.findItem(R.id.filter_menu)
        filterInfos.entries.forEach { (type, info) ->
            if (info.showInMenu) {
                filterMenu.subMenu.add(
                        R.id.group_filter_menu,
                        type.ordinal,
                        Menu.NONE,
                        info.name
                ).apply {
                    isEnabled = viewModel.currentResult?.let { info.valid(it) } ?: true
                    Log.d(TAG, "onCreateOptionsMenu: ${info.name} isEnabled $isEnabled")
                }
            }
        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.d(TAG, "onOptionsItemSelected ${item.groupId}  ${item.itemId}")

        return when (item.groupId) {
            R.id.group_adjust_page_action -> when (item.itemId) {
                R.id.done -> {
                    Log.d(TAG, "onOptionsItemSelected: save")
//                    viewModel.save(pool)
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
                val type = FilterTypeValues[item.itemId]
                apply(type)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    }

    override fun onPause() {
        super.onPause()
        viewModel.save(pool)
    }



    /**
     * push history list to the adapter
     */
    private fun syncHistory() {
        historyAdapter.setData(viewModel.history)
    }

    /**
     * fallback somewhere in the history
     */
    private fun fallback(index: Int) {
        val filter = viewModel.fallback(index)
        val (layout, mediator) = createControlPanel(requireContext(), filter)
        controlPanel.removeAllViews()
        controlPanel.addView(layout)
        viewModel.mediator.value = mediator
        mediator.value = filter
 syncHistory()
        requireActivity().invalidateOptionsMenu()
        Log.d(TAG, "fallback: history ${viewModel.history}")
    }

    /**
     * apply a new filter according to the given filter type.
     */
    private fun apply(type: FilterType) {
        val filter = filterInfos.getValue(type).createInstance()
        viewModel.apply(filter)
        val (layout, mediator) = createControlPanel(requireContext(), filter)
        controlPanel.removeAllViews()
        controlPanel.addView(layout)
        viewModel.mediator.value = mediator
        mediator.value = filter
 syncHistory()
        requireActivity().invalidateOptionsMenu()
    }


    /**
     * draw the given mat to result view
     */
    private fun draw(mat: Mat) {
        Log.d("draw", "draw: $mat")
        // todo use extension function
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

    /**
     * navigate to home page
     */
    private fun goHome() {
        findNavController().popBackStack()
    }

    companion object {
        private const val TAG = "AdjustPageFragment"
    }
}



