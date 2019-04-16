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
import com.djfos.im.R
import com.djfos.im.databinding.FragmentAdjustPageBinding
import com.djfos.im.filter.FilterType
import com.djfos.im.filter.typeMap
import com.djfos.im.model.Draft
import com.djfos.im.util.GlideApp
import com.djfos.im.util.createView
import kotlinx.android.synthetic.main.fragment_adjust_page.*
import org.opencv.android.Utils
import org.opencv.core.Mat
import kotlin.reflect.full.createInstance

private const val TAG = "AdjustPageFragment"

class AdjustPageFragment : Fragment() {
    private val db = (requireActivity() as MainActivity).db
    private val draftDao = db.draftDao()
    private lateinit var viewModel: AdjustPageViewModel
    private val pool = GlideApp.get(requireContext()).bitmapPool
    private lateinit var viewSwitcher: ViewSwitcher
    private var previous: Bitmap? = null
    private lateinit var draft: Draft


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentAdjustPageBinding.inflate(inflater, container, false)
        viewSwitcher = binding.resultView

        val id = AdjustPageFragmentArgs.fromBundle(arguments!!).draftId
        draft = draftDao.find(id)

        val bitmap = GlideApp.with(requireContext())
                .asBitmap()
                .load(draft.sourceImageUriString)
                .submit()
                .get()

        val origin = Mat()
        Utils.bitmapToMat(bitmap, origin)
        pool.put(bitmap)

        viewModel = ViewModelProviders
                .of(this, Factory(draft, origin))
                .get<AdjustPageViewModel>(AdjustPageViewModel::class.java)

        setHasOptionsMenu(true)

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.adjust_page_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.done -> {
                save(draft)
                Log.d(TAG, "onOptionsItemSelected: save")
                findNavController().navigate(R.id.action_global_homeFragment)
                true
            }
            R.id.drop -> {
                drop(draft)
                Log.d(TAG, "onOptionsItemSelected: drop")
                findNavController().navigate(R.id.action_global_homeFragment)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    }

    /**
     * get the current result
     */
    private fun process(): Mat {
        return viewModel.process()
    }


    fun apply(type: FilterType) {
        val cls = typeMap[type]
        if (cls == null) {
            Log.e(TAG, "apply: no class found that matches type $type", null)
            return
        }
        val filter = cls.createInstance()
        val (layout, liveData) = createView(requireContext(), filter)

        liveData.observe({ lifecycle }) {
            draw(process())
        }

        control_panel.removeAllViews()
        control_panel.addView(layout)
    }

    private fun draw(mat: Mat) {
        val bitmap = pool.getDirty(mat.width(), mat.height(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(mat, bitmap)
        if (previous != null) pool.put(previous)
        previous = bitmap

        val next = viewSwitcher.nextView as ImageView
        next.setImageBitmap(bitmap)
        viewSwitcher.showNext()
    }

    private fun save(draft: Draft) {
        draftDao.update(draft)
        gotHome()
    }

    private fun drop(draft: Draft) {
        draftDao.delete(draft)
        gotHome()
    }

    private fun gotHome() {
        findNavController().navigate(HomeFragmentDirections.actionGlobalHomeFragment())
    }
}



