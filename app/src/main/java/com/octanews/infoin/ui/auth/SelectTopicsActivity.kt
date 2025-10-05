package com.octanews.infoin.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.octanews.infoin.R
import com.octanews.infoin.databinding.ActivitySelectTopicsBinding
import com.octanews.infoin.ui.adapter.TopicsAdapter

class SelectTopicsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySelectTopicsBinding
    private lateinit var adapter: TopicsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelectTopicsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()

        binding.btnNext.setOnClickListener {
            val selectedTopics = adapter.selectedTopics.toList()

            if (selectedTopics.isEmpty()) {
                Toast.makeText(this, "Pilih minimal satu topik", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            saveTopicsAndProceed(selectedTopics)
        }
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupRecyclerView() {
        val topics = resources.getStringArray(R.array.topics_list).toList()
        adapter = TopicsAdapter(topics)

        // Gunakan FlexboxLayoutManager agar rapi seperti di desain
        binding.rvTopics.layoutManager = FlexboxLayoutManager(this)
        binding.rvTopics.adapter = adapter
    }

    private fun saveTopicsAndProceed(topics: List<String>) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()
        val userRef = db.collection("users").document(uid)

        userRef.update("topics", topics)
            .addOnSuccessListener {
                // Lanjut ke halaman berikutnya
                val intent = Intent(this, SelectNewsSourceActivity::class.java)
                startActivity(intent)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal menyimpan topik: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}