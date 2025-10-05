package com.octanews.infoin.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.octanews.infoin.DetailActivity
import com.octanews.infoin.data.model.NewsArticle
import com.octanews.infoin.data.model.NewsDataResponse
import com.octanews.infoin.data.remote.RetrofitClient
import com.octanews.infoin.databinding.ActivityNewsListBinding
import com.octanews.infoin.ui.adapter.LatestNewsAdapter
import com.octanews.infoin.utils.Constants
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NewsListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNewsListBinding
    private lateinit var newsAdapter: LatestNewsAdapter
    private val articles = mutableListOf<NewsArticle>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewsListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Ambil data yang dikirim dari HomeFragment
        val title = intent.getStringExtra("TITLE")
        val country = intent.getStringExtra("COUNTRY")
        val language = intent.getStringExtra("LANGUAGE")
        val category = intent.getStringExtra("CATEGORY")

        setupToolbar(title)
        setupRecyclerView()
        fetchNews(country, language, category)
    }

    private fun setupToolbar(title: String?) {
        binding.toolbar.title = title ?: "News List"
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupRecyclerView() {
        newsAdapter = LatestNewsAdapter(
            articles,
            // Ini adalah parameter onItemClick
            onItemClick = { article ->
                val intent = Intent(this, DetailActivity::class.java).apply {
                    putExtra("ARTICLE_URL", article.link)
                }
                startActivity(intent)
            },
            // Ini adalah parameter onBookmarkClick yang kurang
            onBookmarkClick = { article ->
                // Untuk halaman ini, kita bisa kosongkan atau beri Toast
                // karena fungsi utama bookmark ada di HomeFragment
                Toast.makeText(this, "Bookmark dari sini belum diimplementasikan", Toast.LENGTH_SHORT).show()
            }
        )
        binding.rvNewsList.apply {
            layoutManager = LinearLayoutManager(this@NewsListActivity)
            adapter = newsAdapter
        }
    }

    private fun fetchNews(country: String?, lang: String?, category: String?) {
        binding.progressBar.visibility = View.VISIBLE

        val finalCategory = if (category.isNullOrBlank() || category.equals("top", ignoreCase = true)) null else category

        RetrofitClient.instance.getNews(Constants.API_KEY, country, lang, finalCategory, null)
            .enqueue(object : Callback<NewsDataResponse> {
                override fun onResponse(call: Call<NewsDataResponse>, response: Response<NewsDataResponse>) {
                    binding.progressBar.visibility = View.GONE
                    if (response.isSuccessful) {
                        response.body()?.results?.let {
                            articles.addAll(it)
                            newsAdapter.notifyDataSetChanged()
                        }
                    } else {
                        Toast.makeText(this@NewsListActivity, "Gagal: ${response.message()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<NewsDataResponse>, t: Throwable) {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this@NewsListActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
}