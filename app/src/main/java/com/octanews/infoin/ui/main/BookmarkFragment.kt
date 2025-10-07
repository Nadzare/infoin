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
import com.octanews.infoin.databinding.FragmentBookmarkBinding
import com.octanews.infoin.ui.adapter.LatestNewsAdapter

class BookmarkFragment : Fragment() {

    private var _binding: FragmentBookmarkBinding? = null
    private val binding get() = _binding!!

    private lateinit var bookmarkAdapter: LatestNewsAdapter
    private val bookmarkedArticles = mutableListOf<NewsArticle>()

    private val auth by lazy { FirebaseAuth.getInstance() }
    private val db by lazy { FirebaseFirestore.getInstance() }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentBookmarkBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        loadBookmarks()
    }

    private fun setupRecyclerView() {
        // Kita bisa pakai ulang LatestNewsAdapter
        bookmarkAdapter = LatestNewsAdapter(bookmarkedArticles,
            onItemClick = { article ->
                val intent = Intent(activity, DetailActivity::class.java).apply {
                    putExtra("ARTICLE_URL", article.link)
                }
                startActivity(intent)
            },
            onBookmarkClick = { article ->
                // Di halaman bookmark, klik ikon bookmark artinya menghapus
                if (article.link != null) {
                    removeBookmark(article.link.hashCode().toString())
                }
            }
        )
        binding.rvBookmarks.layoutManager = LinearLayoutManager(context)
        binding.rvBookmarks.adapter = bookmarkAdapter
    }

    private fun loadBookmarks() {
        binding.progressBar.visibility = View.VISIBLE
        val uid = auth.currentUser?.uid
        if (uid == null) {
            binding.progressBar.visibility = View.GONE
            binding.tvEmptyBookmark.visibility = View.VISIBLE
            Toast.makeText(context, "Silakan login untuk melihat bookmark", Toast.LENGTH_LONG).show()
            return
        }

        db.collection("users").document(uid).collection("bookmarks")
            .orderBy("pubDate", Query.Direction.DESCENDING) // Urutkan berdasarkan tanggal
            .addSnapshotListener { snapshots, error ->
                binding.progressBar.visibility = View.GONE

                if (error != null) {
                    Toast.makeText(context, "Gagal memuat bookmark: ${error.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                bookmarkedArticles.clear()
                if (snapshots != null && !snapshots.isEmpty) {
                    binding.tvEmptyBookmark.visibility = View.GONE
                    for (document in snapshots) {
                        val article = document.toObject(NewsArticle::class.java)
                        bookmarkedArticles.add(article)
                    }
                } else {
                    binding.tvEmptyBookmark.visibility = View.VISIBLE
                }
                bookmarkAdapter.notifyDataSetChanged()
            }
    }

    private fun removeBookmark(documentId: String) {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).collection("bookmarks").document(documentId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(context, "Bookmark dihapus", Toast.LENGTH_SHORT).show()
                // List akan otomatis ter-update karena kita pakai addSnapshotListener
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