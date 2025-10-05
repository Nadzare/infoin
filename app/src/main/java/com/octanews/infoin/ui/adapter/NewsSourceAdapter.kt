package com.octanews.infoin.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.octanews.infoin.R
import com.octanews.infoin.ui.auth.NewsSource

class NewsSourceAdapter(private val sources: List<NewsSource>) : RecyclerView.Adapter<NewsSourceAdapter.ViewHolder>() {

    val selectedSources = mutableSetOf<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_news_source, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val source = sources[position]
        holder.sourceName.text = source.name

        val context = holder.itemView.context
        val imageResId = context.resources.getIdentifier(source.logoName, "drawable", context.packageName)
        if (imageResId != 0) {
            holder.sourceLogo.setImageResource(imageResId)
        }

        holder.cardView.isChecked = selectedSources.contains(source.name)

        holder.cardView.setOnClickListener {
            holder.cardView.isChecked = !holder.cardView.isChecked
            if (holder.cardView.isChecked) {
                selectedSources.add(source.name)
            } else {
                selectedSources.remove(source.name)
            }
        }
    }

    override fun getItemCount() = sources.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardView: MaterialCardView = itemView as MaterialCardView
        val sourceLogo: ImageView = itemView.findViewById(R.id.iv_source_logo)
        val sourceName: TextView = itemView.findViewById(R.id.tv_source_name)
    }
}