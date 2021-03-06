package com.djfos.im.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.ItemKeyProvider
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.djfos.im.databinding.ListItemDraftBinding
import com.djfos.im.model.Draft
import com.djfos.im.ui.HomeFragmentDirections


class DraftAdapter : ListAdapter<Draft, DraftAdapter.ViewHolder>(DraftDiffCallback()) {
    private var mDraftList: List<Draft>? = emptyList()
    lateinit var tracker: SelectionTracker<Long>

    init {
        setHasStableIds(true)
    }

    inner class ViewHolder(private val binding: ListItemDraftBinding) : RecyclerView.ViewHolder(binding.root) {
        private lateinit var mdraft: Draft
        fun bind(item: Draft, position: Int, listener: View.OnClickListener) {
            mdraft = item
            binding.apply {
                draft = mdraft
                clickListener = listener
                select = tracker.isSelected(mdraft.id)
                Log.d("DraftAdapter", "bind: position:$position select:$select")
                executePendingBindings()
            }
        }


        fun getItemDetails() = object : ItemDetailsLookup.ItemDetails<Long>() {
            override fun getSelectionKey(): Long = mdraft.id
            override fun getPosition(): Int = adapterPosition
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ListItemDraftBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val draft = getItem(position)
        holder.bind(draft, position, View.OnClickListener {
            it.findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToAdjustPage(draft.id))
        })
    }

    override fun submitList(list: List<Draft>?) {
        Log.d("submitList", "submitList: %list")
        super.submitList(list)
        mDraftList = list
    }

    override fun getItemId(position: Int): Long {
        return getItem(position).id
    }

    fun getSelectedDraft(): List<Draft> {
        return mDraftList?.filter { draft ->
            tracker.selection.contains(draft.id)
        } ?: emptyList()
    }


}

private class DraftDiffCallback : DiffUtil.ItemCallback<Draft>() {

    override fun areItemsTheSame(oldItem: Draft, newItem: Draft): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Draft, newItem: Draft): Boolean {
        return oldItem.latestModifyTime == newItem.latestModifyTime
    }
}

class DraftLookup(private val recyclerView: RecyclerView) : ItemDetailsLookup<Long>() {
    override fun getItemDetails(event: MotionEvent): ItemDetails<Long>? {
        val view = recyclerView.findChildViewUnder(event.x, event.y)
        if (view != null) {
            val holder = recyclerView.getChildViewHolder(view)
            if (holder is DraftAdapter.ViewHolder) {
                return holder.getItemDetails()
            }
        }
        return null
    }
}

class RecyclerViewIdKeyProvider(private val recyclerView: RecyclerView) : ItemKeyProvider<Long>(SCOPE_MAPPED) {

    override fun getKey(position: Int): Long? {
        return recyclerView.adapter?.getItemId(position)
                ?: throw IllegalStateException("RecyclerView adapter is not set!")
    }

    override fun getPosition(key: Long): Int {
        val viewHolder = recyclerView.findViewHolderForItemId(key)
        return viewHolder?.layoutPosition ?: RecyclerView.NO_POSITION
    }
}


