package com.djfos.im.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.djfos.im.databinding.ListItemHistoryBinding
import com.djfos.im.filter.AbstractFilter
import com.djfos.im.filter.filterInfos


class HistoryAdapter(val callback: (index: Int) -> Unit) : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {
    private var mDataList = emptyList<AbstractFilter>()

    init {
        setHasStableIds(true)
    }

    inner class ViewHolder(private val binding: ListItemHistoryBinding) : RecyclerView.ViewHolder(binding.root) {
        private lateinit var mItem: AbstractFilter
        fun bind(item: AbstractFilter, listener: View.OnClickListener) {
            mItem = item
            binding.apply {
                name = filterInfos.getValue(mItem.type).name
                clickListener = listener
                executePendingBindings()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ListItemHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mDataList[position]
        holder.bind(item, View.OnClickListener {
            callback(position)
        })
    }

    internal fun setData(dataList: List<AbstractFilter>) {
        this.mDataList = dataList
        notifyDataSetChanged()
    }

    override fun getItemCount() = mDataList.size
}
