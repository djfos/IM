package com.djfos.im.ui


import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.FrameLayout
import android.widget.ImageView
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
import com.djfos.im.filter.AbstractFilter
import com.djfos.im.filter.FilterTypeValues
import com.djfos.im.filter.buildFilterControl
import com.djfos.im.filter.filterInfos
import com.djfos.im.util.GlideApp
import com.djfos.im.util.Injector
import com.djfos.im.viewModel.AdjustPageViewModel
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.opencv.android.Utils
import org.opencv.core.Mat
import java.util.concurrent.TimeUnit


class AdjustPageFragment : Fragment() {
    private lateinit var viewModel: AdjustPageViewModel
    private lateinit var pool: BitmapPool
    private lateinit var resultView: ImageView
    private var previous: Bitmap? = null
    private lateinit var controlPanel: FrameLayout
    private lateinit var historyAdapter: HistoryAdapter
    private var disposable: Disposable? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentAdjustPageBinding.inflate(inflater, container, false)

        // set viewModel
        val factory = Injector.provideAViewModelFactory(requireContext())
        viewModel = ViewModelProviders.of(this, factory).get(AdjustPageViewModel::class.java)
        // set class properties
        resultView = binding.resultView
        Log.d(TAG, "onCreateView: resultView ${resultView.width} x ${resultView.height}")
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

        // set control
        val subject = PublishSubject.create<AbstractFilter>()

        disposable = subject.throttleLatest(10, TimeUnit.MILLISECONDS).subscribe { filter ->
            draw(filter.apply(viewModel.previousResult))
            Log.d("observe", "filter: $filter")
            viewModel.currentFilter = filter
        }

        viewModel.mediator.observe({ lifecycle }) { filterHolder ->
            // subscribe to  change of  filter control
            filterHolder.observe({ lifecycle }) { filter -> subject.onNext(filter) }
        }

        val args = AdjustPageFragmentArgs.fromBundle(requireArguments())
        GlobalScope.launch(IO) {
            init(requireContext(), args.id)
            withContext(Main) {
                syncHistory()
                fallback(viewModel.history.lastIndex)
            }
        }

        // save when back button pressed
        requireActivity().onBackPressedDispatcher.addCallback({ lifecycle }) {
            Log.d(TAG, "onCreateView: onBackPressedDispatcher")
            viewModel.save(pool)
            false
        }


        setHasOptionsMenu(true)

        return binding.root
    }

    /**
     * provide necessary data asynchronously
     */
    private suspend fun init(context: Context, id: Long) {
        val draft = viewModel.getDraft(id)
        val width = resources.displayMetrics.widthPixels
        val height = resources.displayMetrics.heightPixels
        val origin = withContext(IO) {
            GlideApp.with(context)
                    .asBitmap()
                    .load(draft.image)
                    .override(width, height)
                    .submit()
                    .get().let { bitmap ->
                        val mat = Mat()
                        Utils.bitmapToMat(bitmap, mat)
                        pool.put(bitmap)
                        mat
                    }
        }

        viewModel.draft = draft
        viewModel.origin = origin
        viewModel.previousResult = origin
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
                    viewModel.save(pool)
                    goHome()
                    true
                }
                R.id.drop -> {
                    Log.d(TAG, "onOptionsItemSelected: drop")
                    viewModel.drop()
                    goHome()
                    true
                }
                R.id.save -> {
                    viewModel.saveToFile(requireContext(), pool)
                    goHome()
                    true
                }
                R.id.share -> {
                    viewModel.share(requireActivity(), pool)
                    true
                }
                else -> super.onOptionsItemSelected(item)
            }
            R.id.group_filter_menu -> {
                val type = FilterTypeValues[item.itemId]
                val filter = filterInfos.getValue(type).createInstance()
                viewModel.apply(filter) // sync view model
                apply(filter) // sync UI
                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    }

    /**
     * push history list to the  adapter
     */
    private fun syncHistory() {
        historyAdapter.setData(viewModel.history)
    }

    /**
     * fallback somewhere in the history
     */
    private fun fallback(index: Int) {
        val filter = viewModel.fallback(index) //view model
        apply(filter) // UI
        Log.d(TAG, "fallback: history ${viewModel.history}")
    }

    /**
     * update the UI with the given filter
     */
    fun apply(filter: AbstractFilter) {
        val mediator = buildFilterControl(filter)(controlPanel)
        viewModel.mediator.value = mediator
        mediator.value = filter
        requireActivity().invalidateOptionsMenu() // sync menu
        syncHistory() // sync history panel
    }


    /**
     * draw the given mat to  the result view
     */
    private fun draw(mat: Mat) {
        GlobalScope.launch {
            Log.d("draw", "draw: $mat")

            viewModel.currentResult = mat

            if (previous != null) {
                //clean up
                pool.put(previous)
            }
            val bitmap = pool.getDirty(mat.width(), mat.height(), Bitmap.Config.ARGB_8888)
            Utils.matToBitmap(mat, bitmap)
            withContext(Main) {
                resultView.setImageBitmap(bitmap)
            }
        }
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



