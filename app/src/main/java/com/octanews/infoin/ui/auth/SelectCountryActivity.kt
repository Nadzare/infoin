package com.octanews.infoin.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions // <-- Import yang dibutuhkan
import com.octanews.infoin.databinding.ActivitySelectCountryBinding

class SelectCountryActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySelectCountryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelectCountryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Atur agar pengguna bisa klik komponennya untuk membuka dialog
        binding.countryCodePicker.setOnClickListener {
            binding.countryCodePicker.launchCountrySelectionDialog()
        }

        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.btnNext.setOnClickListener {
            // Ambil nama negara yang dipilih langsung dari komponen CCP
            val selectedCountryName = binding.countryCodePicker.selectedCountryName
            saveCountryAndProceed(selectedCountryName)
        }
    }

    private fun saveCountryAndProceed(countryName: String) {
        // Tampilkan loading atau nonaktifkan tombol
        binding.btnNext.isEnabled = false

        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            Toast.makeText(this, "User tidak ditemukan, silakan login ulang.", Toast.LENGTH_LONG).show()
            binding.btnNext.isEnabled = true
            return
        }

        val db = FirebaseFirestore.getInstance()
        val userRef = db.collection("users").document(uid)

        // --- BAGIAN YANG DIPERBARUI ---

        // Siapkan data yang mau disimpan dalam bentuk Map
        val dataToSave = hashMapOf("country" to countryName)

        // Gunakan .set dengan opsi merge.
        // Ini akan membuat dokumen jika belum ada, atau meng-update jika sudah ada.
        userRef.set(dataToSave, SetOptions.merge())
            .addOnSuccessListener {
                // Sukses! Lanjut ke halaman berikutnya
                val intent = Intent(this, SelectTopicsActivity::class.java)
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { e ->
                // Jika gagal, tampilkan pesan dan aktifkan lagi tombolnya
                Toast.makeText(this, "Gagal menyimpan pilihan: ${e.message}", Toast.LENGTH_SHORT).show()
                binding.btnNext.isEnabled = true
            }
        // --- AKHIR PERBARUAN ---
    }
}