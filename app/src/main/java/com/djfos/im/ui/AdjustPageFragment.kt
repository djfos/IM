package com.djfos.im.ui


import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.ViewSwitcher
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.djfos.im.R
import com.djfos.im.databinding.FragmentAdjustPageBinding
import com.djfos.im.filter.FilterType
import com.djfos.im.filter.IFilter
import com.djfos.im.filter.newInstanceFromType
import com.djfos.im.filter.typeMap
import com.djfos.im.model.Draft
import com.djfos.im.util.GlideApp
import com.djfos.im.util.Injector
import com.djfos.im.util.createControlPanel
import com.djfos.im.viewModel.AdjustPageViewModel
import kotlinx.android.synthetic.main.fragment_adjust_page.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.opencv.android.Utils
import org.opencv.core.Mat


class AdjustPageFragment : Fragment() {

    private lateinit var viewModel: AdjustPageViewModel
    private lateinit var pool: BitmapPool
    private lateinit var viewSwitcher: ViewSwitcher
    private var previous: Bitmap? = null

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentAdjustPageBinding.inflate(inflater, container, false)

        viewSwitcher = binding.resultView
        pool = GlideApp.get(requireContext()).bitmapPool
        val args = AdjustPageFragmentArgs.fromBundle(arguments!!)
        val factory = Injector.provideAViewModelFactory(requireContext(), args.id)

        viewModel = ViewModelProviders.of(this, factory).get(AdjustPageViewModel::class.java)

        viewModel.draft.observe({ lifecycle }) { draft ->
            draft?.let { initFragment(draft) }
        }



        setHasOptionsMenu(true)

        return binding.root
    }

    private fun initFragment(draft: Draft) {
        val fragment: Fragment = this
        // decode image to mat
        GlobalScope.launch {
            val bitmap = GlideApp.with(fragment)
                    .asBitmap()
                    .load(draft.image)
                    .submit()
                    .get()
            Log.d("loadImage", draft.image)
            val origin = Mat()
            Utils.bitmapToMat(bitmap, origin)
            pool.put(bitmap)

            // switch to main thread
            withContext(Dispatchers.Main) {
                viewModel.origin = origin
                viewModel.previousResult = origin

                restoreHistory(newDraft = draft).let { history ->
                    Log.d(TAG, "initFragment: old:${viewModel.history.size},new:${history.size}")
                    viewModel.history = history
                    if (history.isEmpty()) {
                        apply(FilterType.Identity)
                    } else {
                        fallback(history.lastIndex)
                    }
                }
            }
        }

        //when filter applied change, build the new subscription
        viewModel.mediator.observe({ lifecycle }) { filterHolder ->
            // subscribe to  change of  filter control
            filterHolder.observe({ lifecycle }) { filter ->
                draw(process(filter))
            }
        }
    }

    private fun restoreHistory(newDraft: Draft): MutableList<IFilter> {
        val oldDraft = viewModel.draft.value ?: return newDraft.history

        if (oldDraft.id != newDraft.id) return newDraft.history

        return oldDraft.history
    }

    private fun process(filter: IFilter) = filter.apply(viewModel.previousResult)

    override fun onCreateOptionsMenu(
            menu: Menu,
            inflater: MenuInflater
    ) {
        inflater.inflate(R.menu.adjust_page_menu, menu)
        val a = menu.findItem(R.id.filter_menu)
        Log.d(TAG, "onCreateOptionsMenu: ${a.hasSubMenu()}")
        typeMap.keys.forEach {
            a.subMenu.add(Menu.NONE, it.ordinal, Menu.NONE, it.toString())
        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.d(TAG, "onOptionsItemSelected id:${item.itemId} title:${item.title}")
        return when (item.itemId) {
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
            R.id.filter_menu -> {
                true
            }
            else -> {
                val type = FilterType.valueOf(item.title.toString())
                apply(type)
                true
            }
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

        control_panel.removeAllViews()
        control_panel.addView(layout)

        viewModel.mediator.value = mediator
        mediator.value = filter
    }


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



