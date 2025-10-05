package com.octanews.infoin.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.octanews.infoin.R

class TopicsAdapter(private val topics: List<String>) : RecyclerView.Adapter<TopicsAdapter.ViewHolder>() {

    val selectedTopics = mutableSetOf<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_topic, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val topic = topics[position]
        holder.topicChip.text = topic
        holder.topicChip.isChecked = selectedTopics.contains(topic)
        holder.topicChip.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                selectedTopics.add(topic)
            } else {
                selectedTopics.remove(topic)
            }
        }
    }

    override fun getItemCount() = topics.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val topicChip: Chip = itemView.findViewById(R.id.chip_topic)
    }
}