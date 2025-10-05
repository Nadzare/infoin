package com.octanews.infoin.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.octanews.infoin.databinding.ActivityOnboardingBinding
import com.octanews.infoin.ui.auth.LoginActivity
import com.octanews.infoin.R

class OnboardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingBinding
    private var currentPage = 0

    // Data untuk 3 halaman onboarding
    private val onboardingData = listOf(
        OnboardingPage(
            R.drawable.ic_onboarding,
            "Berita Terkini di Ujung Jari Anda",
            "Dapatkan berita terbaru dari berbagai sumber terpercaya, langsung di satu aplikasi. Infoin menghadirkan kabar terkini dengan tampilan yang cepat, ringan, dan mudah diakses kapan pun."
        ),
        OnboardingPage(
            R.drawable.ic_onboarding_2,
            "Berita Sesuai Minatmu",
            "Pilih topik favoritmu seperti teknologi, ekonomi, hiburan, hingga olahraga. Infoin menyesuaikan konten agar kamu hanya melihat berita yang relevan dan kamu sukai."
        ),
        OnboardingPage(
            R.drawable.ic_onboarding_3,
            "Selalu Terhubung dengan Dunia",
            "Akses berita kapan saja, di mana saja. Dengan Infoin, kamu tetap tahu perkembangan dunia secara real-time, tanpa batas waktu dan tempat."
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize ViewBinding
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup awal
        setupUI()
        setupListeners()
    }

    private fun setupUI() {
        // Tampilkan halaman pertama
        updateContent()
    }

    private fun setupListeners() {
        // Handle tombol Next
        binding.btnNext.setOnClickListener {
            if (currentPage < onboardingData.size - 1) {
                // Masih ada halaman berikutnya, pindah ke halaman selanjutnya
                currentPage++
                updateContent()
            } else {
                // Sudah halaman terakhir, pindah ke MainActivity
                navigateToMain()
            }
        }
    }

    private fun updateContent() {
        val page = onboardingData[currentPage]

        // Update gambar, judul, dan deskripsi
        binding.ivOnboarding.setImageResource(page.imageRes)
        binding.tvTitle.text = page.title
        binding.tvDescription.text = page.description

        // Update indikator (titik biru/abu-abu)
        updateIndicators()

        // Ubah teks tombol di halaman terakhir
        binding.btnNext.text = if (currentPage == onboardingData.size - 1) {
            "Get Started"
        } else {
            "Next"
        }
    }

    private fun updateIndicators() {
        // List semua indikator
        val indicators = listOf(
            binding.indicator1,
            binding.indicator2,
            binding.indicator3
        )

        // Update warna indikator sesuai halaman aktif
        indicators.forEachIndexed { index, view ->
            view.background = if (index == currentPage) {
                // Halaman aktif = biru
                ContextCompat.getDrawable(this, R.drawable.indicator_active)
            } else {
                // Halaman tidak aktif = abu-abu
                ContextCompat.getDrawable(this, R.drawable.indicator_inactive)
            }
        }
    }

    private fun navigateToMain() {
        // Simpan bahwa user sudah selesai onboarding
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        prefs.edit().putBoolean("onboarding_completed", true).apply()

        // Pindah ke MainActivity (uncomment dan sesuaikan dengan activity Anda)
         val intent = Intent(this, LoginActivity::class.java)
         startActivity(intent)

        finish()
    }

    // Data class untuk menyimpan info tiap halaman
    data class OnboardingPage(
        val imageRes: Int,      // ID resource gambar
        val title: String,      // Judul halaman
        val description: String // Deskripsi halaman
    )
}