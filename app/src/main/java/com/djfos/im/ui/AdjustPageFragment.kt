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
import com.djfos.im.util.createView
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

        // decode image to mat
        val fragment = this
        viewModel.draft.observe({ lifecycle }) { draft ->
            Log.d(TAG, "onCreateView: viewModel.draft.observe called")
            // unsubscribe here to prevent the null pointer when drop the draft
            // also,the draft is no need to change
            viewModel.draft.removeObservers(fragment)

            GlobalScope.launch {
                val bitmap = GlideApp.with(fragment)
                        .asBitmap()
                        .load(draft.sourceImageUriString)
                        .submit()
                        .get()
                val origin = Mat()
                Utils.bitmapToMat(bitmap, origin)
                pool.put(bitmap)

                withContext(Dispatchers.Main) {
                    viewModel.origin = origin
                    viewModel.previousResult = origin
                    draft.history.let {
                        viewModel.history = it

                        if (it.isEmpty()) {
                            draw(origin)
                        } else {
                            fallback(it.lastIndex)
                        }
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

        setHasOptionsMenu(true)

        return binding.root
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
                save(viewModel.draft.value!!)
                true
            }
            R.id.drop -> {
                Log.d(TAG, "onOptionsItemSelected: drop")
                drop(viewModel.draft.value!!)
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
        viewModel.previousResult = viewModel.currentResutl
        viewModel.history.add(filter)
        apply(filter)
    }


    fun apply(filter: IFilter) {
        val (layout, mediator) = createView(requireContext(), filter)

        control_panel.removeAllViews()
        control_panel.addView(layout)

        viewModel.mediator.value = mediator
        mediator.value = filter
    }


    private fun draw(mat: Mat) {
        viewModel.currentResutl = mat

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

    private fun save(draft: Draft) {
        GlobalScope.launch {
            viewModel.save(draft)
            gotHome()
        }
    }

    private fun drop(draft: Draft) {
        GlobalScope.launch {
            viewModel.drop(draft)
            gotHome()
        }
    }

    private fun gotHome() {
        findNavController().navigate(HomeFragmentDirections.actionGlobalHomeFragment())
    }

    companion object {
        private const val TAG = "AdjustPageFragment"
    }
}



