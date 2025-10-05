package com.octanews.infoin.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.octanews.infoin.DetailActivity
import com.octanews.infoin.data.model.NewsArticle
import com.octanews.infoin.databinding.FragmentListBinding // Gunakan binding dari layout baru
import com.octanews.infoin.ui.adapter.LatestNewsAdapter

class ProfileNewsFragment : Fragment() {

    private var _binding: FragmentListBinding? = null
    private val binding get() = _binding!!

    private lateinit var newsAdapter: LatestNewsAdapter
    private val articles = mutableListOf<NewsArticle>()

    private val auth by lazy { FirebaseAuth.getInstance() }
    private val db by lazy { FirebaseFirestore.getInstance() }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        loadBookmarkedNews()
    }

    private fun setupRecyclerView() {
        newsAdapter = LatestNewsAdapter(articles,
            onItemClick = { article ->
                val intent = Intent(activity, DetailActivity::class.java).apply {
                    putExtra("ARTICLE_URL", article.link)
                }
                startActivity(intent)
            },
            onBookmarkClick = { article ->
                if (article.link != null) {
                    removeBookmark(article.link.hashCode().toString())
                }
            }
        )
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = newsAdapter
    }

    private fun loadBookmarkedNews() {
        binding.progressBar.visibility = View.VISIBLE
        val uid = auth.currentUser?.uid
        if (uid == null) {
            binding.progressBar.visibility = View.GONE
            binding.tvEmpty.visibility = View.VISIBLE
            return
        }

        db.collection("users").document(uid).collection("bookmarks")
            .orderBy("pubDate", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, error ->
                binding.progressBar.visibility = View.GONE
                if (error != null) {
                    return@addSnapshotListener
                }

                articles.clear()
                if (snapshots != null && !snapshots.isEmpty) {
                    binding.tvEmpty.visibility = View.GONE
                    snapshots.documents.forEach { doc ->
                        articles.add(doc.toObject(NewsArticle::class.java)!!)
                    }
                } else {
                    binding.tvEmpty.visibility = View.VISIBLE
                }
                newsAdapter.notifyDataSetChanged()
            }
    }

    private fun removeBookmark(documentId: String) {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).collection("bookmarks").document(documentId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(context, "Bookmark dihapus", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Gagal menghapus bookmark", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}