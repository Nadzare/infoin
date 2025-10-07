package com.octanews.infoin.ui.auth

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.octanews.infoin.databinding.ActivitySecurityBinding

class SecurityActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySecurityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySecurityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup toolbar with back button
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }
}
