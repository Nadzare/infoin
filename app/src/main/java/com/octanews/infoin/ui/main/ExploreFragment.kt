package com.octanews.infoin.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.octanews.infoin.R
import com.octanews.infoin.databinding.FragmentExploreBinding
import com.octanews.infoin.ui.NewsListActivity
import com.octanews.infoin.ui.adapter.ExploreAdapter
import com.octanews.infoin.utils.Constants

// Data class untuk item di grid
data class CategoryItem(val name: String, @DrawableRes val iconResId: Int)

class ExploreFragment : Fragment() {

    private var _binding: FragmentExploreBinding? = null
    private val binding get() = _binding!!

    // Siapkan data user, akan kita ambil dari Firestore
    private var userCountryCode: String = "us"
    private var userLangCode: String = "en"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentExploreBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fetchUserData() // Ambil data negara & bahasa user

        // Buat daftar kategori dengan ikonnya masing-masing
        val categories = listOf(
            CategoryItem("Top", R.drawable.ic_category_top),
            CategoryItem("Business", R.drawable.ic_category_business),
            CategoryItem("Technology", R.drawable.ic_category_technology),
            CategoryItem("Sports", R.drawable.ic_category_sports),
            CategoryItem("Science", R.drawable.ic_category_science),
            CategoryItem("Health", R.drawable.ic_category_health),
            CategoryItem("Entertainment", R.drawable.ic_category_entertainment),
            CategoryItem("Politics", R.drawable.ic_category_politics), // <-- TAMBAHAN
            CategoryItem("Food", R.drawable.ic_category_food),         // <-- TAMBAHAN
            CategoryItem("Tourism", R.drawable.ic_category_tourism)    // <-- TAMBAHAN
        )

        val adapter = ExploreAdapter(categories) { selectedCategory ->
            // Aksi saat salah satu kategori di-klik
            val intent = Intent(activity, NewsListActivity::class.java).apply {
                putExtra("TITLE", "${selectedCategory.name} News")
                putExtra("COUNTRY", userCountryCode)
                putExtra("LANGUAGE", userLangCode)
                putExtra("CATEGORY", selectedCategory.name.lowercase())
            }
            startActivity(intent)
        }

        binding.rvCategories.layoutManager = GridLayoutManager(context, 2) // 2 kolom grid
        binding.rvCategories.adapter = adapter
    }

    private fun fetchUserData() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseFirestore.getInstance().collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val countryName = document.getString("country") ?: "United States"
                    val codes = convertCountryNameToCodes(countryName)
                    userCountryCode = codes.first
                    userLangCode = codes.second
                }
            }
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