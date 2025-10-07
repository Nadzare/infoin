package com.octanews.infoin.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.octanews.infoin.DetailActivity
import com.octanews.infoin.data.model.NewsArticle
import com.octanews.infoin.data.model.NewsDataResponse
import com.octanews.infoin.data.remote.RetrofitClient
import com.octanews.infoin.databinding.ActivitySearchBinding
import com.octanews.infoin.ui.adapter.LatestNewsAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchBinding
    private lateinit var newsAdapter: LatestNewsAdapter
    private val articles = mutableListOf<NewsArticle>()

    // GANTI DENGAN API KEY DARI NEWSDATA.IO
    private val apiKey = "pub_5361dff2fd6a413dbe02325a647d9da2"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupSearchListener()
    }

    private fun setupToolbar() {
        binding.btnBack.setOnClickListener {
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
            // --- INI PARAMETER KETIGA YANG KURANG ---
            onBookmarkClick = { article ->
                // Untuk halaman search, kita bisa beri Toast atau implementasikan
                // fungsi saveBookmark seperti di HomeFragment jika mau.
                Toast.makeText(this, "${article.title} ditambahkan ke bookmark!", Toast.LENGTH_SHORT).show()
                // Optional: Panggil fungsi untuk menyimpan ke Firestore di sini
            }
        )

        binding.rvSearchResults.apply { // Ganti rvNewsList menjadi rvSearchResults
            layoutManager = LinearLayoutManager(this@SearchActivity)
            adapter = newsAdapter
        }
    }

    private fun setupSearchListener() {
        binding.etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = binding.etSearch.text.toString().trim()
                if (query.isNotEmpty()) {
                    searchNews(query)
                }
                // Sembunyikan keyboard
                true
            } else {
                false
            }
        }
    }

    private fun searchNews(query: String) {
        binding.progressBar.visibility = View.VISIBLE
        articles.clear()
        newsAdapter.notifyDataSetChanged()

        // Panggil API dengan parameter 'q' untuk query
        RetrofitClient.instance.getNews(apiKey, null, null, null, query)
            .enqueue(object : Callback<NewsDataResponse> {
                override fun onResponse(call: Call<NewsDataResponse>, response: Response<NewsDataResponse>) {
                    binding.progressBar.visibility = View.GONE
                    if (response.isSuccessful) {
                        response.body()?.results?.let {
                            articles.addAll(it)
                            newsAdapter.notifyDataSetChanged()
                        }
                    } else {
                        Toast.makeText(this@SearchActivity, "Gagal: ${response.message()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<NewsDataResponse>, t: Throwable) {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this@SearchActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
}