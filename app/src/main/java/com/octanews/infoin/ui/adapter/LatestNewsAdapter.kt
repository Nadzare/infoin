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

class LatestNewsAdapter(
    private val articles: List<NewsArticle>,
    private val onItemClick: (NewsArticle) -> Unit, // <-- KOMA INI PENTING
    private val onBookmarkClick: (NewsArticle) -> Unit
) : RecyclerView.Adapter<LatestNewsAdapter.ViewHolder>() {

    private val sourceLogoMap = mapOf(
        "detik" to R.drawable.ic_detikcom,
        "liputan6" to R.drawable.ic_liputan6,
        "mediaindonesia" to R.drawable.ic_mediindo,
        "republikain" to R.drawable.ic_republika_semangka_1,
        "rm_id" to R.drawable.ic_rm_id,
        "sindonews" to R.drawable.ic_sindonews,
        "tribunnews" to R.drawable.ic_tribunews
        // Jika tidak ditemukan, gunakan placeholder

        // Contoh: pastikan Anda memiliki file ini di res/drawable
                // Contoh
        // Tambahkan semua mapping source_id dan logo yang Anda butuhkan
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_news_article, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val article = articles[position]

        holder.title.text = article.title
        holder.source.text = article.source_id
        holder.time.text = DateFormatter.getTimeAgo(article.pubDate)

        Glide.with(holder.itemView.context)
            .load(article.image_url)
            .placeholder(R.drawable.ic_image_placeholder)
            .error(R.drawable.ic_image_placeholder)
            .into(holder.image)

        // val logoResId = sourceLogoMap[article.source_id] ?: R.drawable.ic_google // Gunakan placeholder jika tidak ditemukan
        // holder.sourceIcon.setImageResource(logoResId)

        // Aksi saat seluruh item di-klik
        holder.itemView.setOnClickListener {
            onItemClick(article)
        }

        // Aksi saat ikon bookmark di-klik
        holder.bookmarkIcon.setOnClickListener {
            onBookmarkClick(article)
            // Feedback visual, langsung ganti ikonnya
            holder.bookmarkIcon.setImageResource(R.drawable.ic_bookmark)
        }
    }

    override fun getItemCount() = articles.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.iv_article_image)
        val title: TextView = itemView.findViewById(R.id.tv_article_title)
        val source: TextView = itemView.findViewById(R.id.tv_article_source)
        val time: TextView = itemView.findViewById(R.id.tv_article_time)
        // --- VIEW HOLDER PERLU MENGENALI IKON BOOKMARK ---
        val bookmarkIcon: ImageView = itemView.findViewById(R.id.iv_bookmark) // <-- INI YANG KURANG
        // val sourceIcon: ImageView = itemView.findViewById(R.id.iv_article_source_icon) // 3. TAMBAHKAN REFERENSI KE IMAGEVIEW LOGO

    }
}