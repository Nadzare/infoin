package com.octanews.infoin.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.octanews.infoin.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        // Atur listener untuk tombol kembali di toolbar
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // --- INI BAGIAN YANG DISESUAIKAN ---
        // Listener sekarang dipasang di 'item_logout' (LinearLayout)
        binding.itemLogout.setOnClickListener {
            // Proses Logout dari Firebase
            auth.signOut()

            Toast.makeText(this, "Logout successful", Toast.LENGTH_SHORT).show()

            // Arahkan kembali ke halaman Login
            val intent = Intent(this, LoginActivity::class.java)
            // Hapus semua activity sebelumnya agar user tidak bisa kembali (back)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        // Kamu bisa tambahkan listener untuk item lain di sini
        binding.itemNotification.setOnClickListener {
            Toast.makeText(this, "Fitur Notifikasi belum dibuat", Toast.LENGTH_SHORT).show()
        }

        binding.itemSecurity.setOnClickListener {
            Toast.makeText(this, "Fitur Keamanan belum dibuat", Toast.LENGTH_SHORT).show()
        }

        // ... dan seterusnya
    }
}