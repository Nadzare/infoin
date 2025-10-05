package com.octanews.infoin

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.octanews.infoin.databinding.ActivityMainBinding
import com.octanews.infoin.ui.main.BookmarkFragment
import com.octanews.infoin.ui.main.ExploreFragment
import com.octanews.infoin.ui.main.HomeFragment
import com.octanews.infoin.ui.main.ProfileFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set halaman default saat pertama kali dibuka
        setCurrentFragment(HomeFragment())

        // Atur listener untuk Bottom Navigation Bar
        binding.bottomNavigation.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.navigation_home -> {
                    setCurrentFragment(HomeFragment())
                    binding.fabAddNews.hide()
                }
                R.id.navigation_explore -> {
                    setCurrentFragment(ExploreFragment())
                    binding.fabAddNews.hide()
                }
                R.id.navigation_bookmark -> {
                    setCurrentFragment(BookmarkFragment())
                    binding.fabAddNews.hide()
                }
                R.id.navigation_profile -> {
                    setCurrentFragment(ProfileFragment())
                    binding.fabAddNews.show()
                }
            }
            true
        }

        // Listener untuk FAB Add News
        binding.fabAddNews.setOnClickListener {
            // TODO: Implement add news functionality
            Toast.makeText(this, "Add News feature coming soon!", Toast.LENGTH_SHORT).show()
        }
    }

    // Fungsi untuk mengganti fragment yang sedang tampil
    private fun setCurrentFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragment_container, fragment)
            commit()
        }
    }
}