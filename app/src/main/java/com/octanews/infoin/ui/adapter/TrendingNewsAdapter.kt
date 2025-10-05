package com.octanews.infoin.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.octanews.infoin.R
import com.octanews.infoin.data.model.NewsArticle
import com.octanews.infoin.utils.DateFormatter

// --- PERUBAHAN 1: Tambahkan parameter 'onBookmarkClick' ---
class TrendingNewsAdapter(
    private val articles: List<NewsArticle>,
    private val onItemClick: (NewsArticle) -> Unit,
    private val onBookmarkClick: (NewsArticle) -> Unit
) : RecyclerView.Adapter<TrendingNewsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_news_article_trending, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val article = articles[position]
        holder.bind(article, onItemClick, onBookmarkClick)
    }

    override fun getItemCount() = articles.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val image: ImageView = itemView.findViewById(R.id.iv_trending_article_image)
        private val title: TextView = itemView.findViewById(R.id.tv_trending_article_title)
        private val sourceName: TextView = itemView.findViewById(R.id.tv_trending_article_source_name)
        private val time: TextView = itemView.findViewById(R.id.tv_trending_article_time)
        private val bookmarkIcon: ImageView? = itemView.findViewById(R.id.iv_bookmark)

        fun bind(
            article: NewsArticle,
            onItemClick: (NewsArticle) -> Unit,
            onBookmarkClick: (NewsArticle) -> Unit
        ) {
            title.text = article.title

            Glide.with(itemView.context)
                .load(article.image_url)
                .placeholder(R.drawable.ic_image_placeholder)
                .error(R.drawable.ic_image_placeholder)
                .into(image)

            sourceName.text = article.source_id
            time.text = DateFormatter.getTimeAgo(article.pubDate)

            itemView.setOnClickListener {
                onItemClick(article)
            }

            bookmarkIcon?.let { icon ->
                icon.setOnClickListener {
                    onBookmarkClick(article)
                    icon.setImageResource(R.drawable.ic_bookmark)
                }
            }
        }
    }
}