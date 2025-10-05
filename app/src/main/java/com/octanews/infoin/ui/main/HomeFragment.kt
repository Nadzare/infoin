package com.octanews.infoin.ui.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.octanews.infoin.DetailActivity // Pastikan Activity ini sudah dibuat
import com.octanews.infoin.data.model.NewsArticle
import com.octanews.infoin.data.model.NewsDataResponse
import com.octanews.infoin.data.remote.RetrofitClient
import com.octanews.infoin.databinding.FragmentHomeBinding
import com.octanews.infoin.ui.NewsListActivity // Pastikan Activity ini sudah dibuat
import com.octanews.infoin.ui.SearchActivity // Pastikan Activity ini sudah dibuat
import com.octanews.infoin.ui.adapter.LatestNewsAdapter
import com.octanews.infoin.ui.adapter.TrendingNewsAdapter
import com.octanews.infoin.utils.Constants
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var trendingAdapter: TrendingNewsAdapter
    private lateinit var latestAdapter: LatestNewsAdapter
    private val trendingArticles = mutableListOf<NewsArticle>()
    private val latestArticles = mutableListOf<NewsArticle>()

    // Properti untuk menyimpan preferensi user
    private var userCountryCode: String = "us"
    private var userLangCode: String = "en"
    private var userTopics: List<String>? = null

    // Daftar kategori statis yang valid untuk NewsData.io
    private val staticCategories = listOf("Top", "Business", "Technology", "Sports", "Science", "Health", "Entertainment")

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerViews()
        setupClickListeners()
        fetchUserDataAndGetNews()
    }

    private fun setupRecyclerViews() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid

        // Setup adapter Trending dengan DUA aksi klik (item & bookmark)
        trendingAdapter = TrendingNewsAdapter(
            trendingArticles,
            onItemClick = { article ->
                openDetailActivity(article.link)
            },
            onBookmarkClick = { article ->
                if (uid != null) {
                    saveBookmark(uid, article)
                } else {
                    Toast.makeText(context, "Silakan login untuk menyimpan bookmark", Toast.LENGTH_SHORT).show()
                }
            }
        )
        binding.rvTrendingNews.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.rvTrendingNews.adapter = trendingAdapter

        // Setup adapter Latest dengan DUA aksi klik (item & bookmark)
        latestAdapter = LatestNewsAdapter(
            latestArticles,
            onItemClick = { article ->
                openDetailActivity(article.link)
            },
            onBookmarkClick = { article ->
                if (uid != null) {
                    saveBookmark(uid, article)
                } else {
                    Toast.makeText(context, "Silakan login untuk menyimpan bookmark", Toast.LENGTH_SHORT).show()
                }
            }
        )
        binding.rvLatestNews.layoutManager = LinearLayoutManager(context)
        binding.rvLatestNews.adapter = latestAdapter
    }

    private fun setupClickListeners() {
        // Listener untuk Search Bar
        binding.searchCard.setOnClickListener {
            startActivity(Intent(activity, SearchActivity::class.java))
        }

        // Listener untuk "See All" Trending
        binding.tvSeeAllTrending.setOnClickListener {
            openNewsList("Trending News", "top")
        }

        // Listener untuk "See All" Latest
        binding.tvSeeAllLatest.setOnClickListener {
            val selectedTabPosition = binding.tabLayoutCategories.selectedTabPosition
            if (selectedTabPosition >= 0) {
                val selectedCategory = binding.tabLayoutCategories.getTabAt(selectedTabPosition)?.text.toString()
                openNewsList("$selectedCategory News", selectedCategory)
            }
        }

        setupCategoryTabs() // Setup tab dipanggil dari sini agar rapi
    }

    private fun fetchUserDataAndGetNews() {
        binding.progressBar.visibility = View.VISIBLE
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            getNews("us", "en", "top")
            return
        }

        FirebaseFirestore.getInstance().collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val countryName = document.getString("country") ?: "United States"
                    userTopics = document.get("topics") as? List<String>
                    val codes = convertCountryNameToCodes(countryName)
                    userCountryCode = codes.first
                    userLangCode = codes.second
                    val categoriesString = userTopics?.joinToString(separator = ",")
                    getNews(userCountryCode, userLangCode, categoriesString)
                } else {
                    getNews("us", "en", "top")
                }
            }
            .addOnFailureListener { getNews("us", "en", "top") }
    }

    private fun setupCategoryTabs() {
        binding.tabLayoutCategories.clearOnTabSelectedListeners()
        binding.tabLayoutCategories.removeAllTabs()

        binding.tabLayoutCategories.addTab(binding.tabLayoutCategories.newTab().setText("For You"))
        staticCategories.forEach { category ->
            binding.tabLayoutCategories.addTab(binding.tabLayoutCategories.newTab().setText(category))
        }

        binding.tabLayoutCategories.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val selectedCategory = tab?.text.toString().lowercase()

                if (selectedCategory == "for you") {
                    val userCategoriesString = userTopics?.joinToString(separator = ",")
                    getNews(userCountryCode, userLangCode, userCategoriesString)
                } else {
                    getNews(userCountryCode, userLangCode, selectedCategory)
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun getNews(country: String, lang: String, categories: String?) {
        binding.progressBar.visibility = View.VISIBLE
        val finalCategories = if (categories.isNullOrBlank() || categories.equals("top", ignoreCase = true)) null else categories

        RetrofitClient.instance.getNews(Constants.API_KEY, country, lang, finalCategories, null)
            .enqueue(object : Callback<NewsDataResponse> {
                override fun onResponse(call: Call<NewsDataResponse>, response: Response<NewsDataResponse>) {
                    binding.progressBar.visibility = View.GONE
                    if (response.isSuccessful) {
                        response.body()?.results?.let { articlesResult ->
                            trendingArticles.clear()
                            trendingArticles.addAll(articlesResult.take(5))
                            trendingAdapter.notifyDataSetChanged()

                            latestArticles.clear()
                            latestArticles.addAll(articlesResult)
                            latestAdapter.notifyDataSetChanged()
                        }
                    } else {
                        Toast.makeText(context, "Gagal: ${response.code()} ${response.message()}", Toast.LENGTH_LONG).show()
                    }
                }
                override fun onFailure(call: Call<NewsDataResponse>, t: Throwable) {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
    }

    private fun openDetailActivity(url: String?) {
        if (url.isNullOrEmpty()) {
            Toast.makeText(context, "Link berita tidak tersedia", Toast.LENGTH_SHORT).show()
            return
        }
        val intent = Intent(activity, DetailActivity::class.java).apply {
            putExtra("ARTICLE_URL", url)
        }
        startActivity(intent)
    }

    private fun openNewsList(title: String, category: String) {
        val intent = Intent(activity, NewsListActivity::class.java).apply {
            putExtra("TITLE", title)
            putExtra("COUNTRY", userCountryCode)
            putExtra("LANGUAGE", userLangCode)
            if (category.equals("For You", ignoreCase = true)) {
                putExtra("CATEGORY", userTopics?.joinToString(","))
            } else {
                putExtra("CATEGORY", category.lowercase())
            }
        }
        startActivity(intent)
    }

    private fun saveBookmark(uid: String, article: NewsArticle) {
        val db = FirebaseFirestore.getInstance()
        val documentId = article.link.hashCode().toString()

        db.collection("users").document(uid)
            .collection("bookmarks").document(documentId)
            .set(article)
            .addOnSuccessListener { Toast.makeText(context, "Berita disimpan!", Toast.LENGTH_SHORT).show() }
            .addOnFailureListener { e -> Toast.makeText(context, "Gagal menyimpan: ${e.message}", Toast.LENGTH_SHORT).show() }
    }

    private fun convertCountryNameToCodes(countryName: String): Pair<String, String> {
        return when (countryName.lowercase()) {
            "indonesia" -> Pair("id", "id")
            "united states" -> Pair("us", "en")
            else -> Pair("us", "en")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}