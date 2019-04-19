package com.djfos.im.model

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class DraftAdapter : RecyclerView.Adapter<DraftAdapter.WordViewHolder>() {
    private var draftList = emptyList<Draft>()

    inner class WordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WordViewHolder {
        return WordViewHolder(TextView(parent.context))
    }

    override fun onBindViewHolder(holder: WordViewHolder, position: Int) {
        val current = draftList[position]
        (holder.itemView as TextView).text = current.id.toString()
    }

    internal fun setDraft(draftList: List<Draft>) {
        this.draftList = draftList
        notifyDataSetChanged()
    }

    override fun getItemCount() = draftList.size
}


