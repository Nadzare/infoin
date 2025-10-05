package com.octanews.infoin.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.octanews.infoin.R
import com.octanews.infoin.databinding.ActivitySelectNewsSourceBinding
import com.octanews.infoin.ui.adapter.NewsSourceAdapter

// Data class simpel untuk menyimpan data sumber berita
data class NewsSource(val name: String, val logoName: String)

class SelectNewsSourceActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySelectNewsSourceBinding
    private lateinit var adapter: NewsSourceAdapter
    private val sourceList = mutableListOf<NewsSource>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelectNewsSourceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()

        binding.btnNext.setOnClickListener {
            val selectedSources = adapter.selectedSources.toList()
            if (selectedSources.isEmpty()) {
                Toast.makeText(this, "Pilih minimal satu sumber berita", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            saveSourcesAndProceed(selectedSources)
        }
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupRecyclerView() {
        val sourceNames = resources.getStringArray(R.array.news_sources_name_list)
        val sourceLogos = resources.getStringArray(R.array.news_sources_logo_list)
        for (i in sourceNames.indices) {
            sourceList.add(NewsSource(sourceNames[i], sourceLogos[i]))
        }

        adapter = NewsSourceAdapter(sourceList)
        // Gunakan GridLayoutManager untuk tampilan 3 kolom
        binding.rvNewsSources.layoutManager = GridLayoutManager(this, 3)
        binding.rvNewsSources.adapter = adapter
    }

    private fun saveSourcesAndProceed(sources: List<String>) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()
        val userRef = db.collection("users").document(uid)

        userRef.update("followedSources", sources)
            .addOnSuccessListener {
                // Lanjut ke halaman berikutnya
                val intent = Intent(this, FillProfileActivity::class.java)
                startActivity(intent)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal menyimpan: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}