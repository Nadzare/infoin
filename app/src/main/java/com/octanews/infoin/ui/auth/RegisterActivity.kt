package com.octanews.infoin.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore // Import Firestore
import com.octanews.infoin.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {

    // ViewBinding
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inisialisasi FirebaseAuth
        auth = FirebaseAuth.getInstance()

        // Tombol Register
        binding.btnRegister.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val confirmPassword = binding.etConfirmPassword.text.toString().trim()

            // Validasi (kodemu sudah bagus, tidak diubah)
            if (name.isEmpty()) {
                binding.tilName.error = "Name required"
                return@setOnClickListener
            } else binding.tilName.error = null

            if (email.isEmpty()) {
                binding.tilEmail.error = "Email required"
                return@setOnClickListener
            } else binding.tilEmail.error = null

            if (password.isEmpty()) {
                binding.tilPassword.error = "Password required"
                return@setOnClickListener
            } else binding.tilPassword.error = null

            if (password.length < 6) {
                binding.tilPassword.error = "Password must be at least 6 characters"
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                binding.tilConfirmPassword.error = "Passwords do not match"
                return@setOnClickListener
            } else binding.tilConfirmPassword.error = null

            // Show ProgressBar
            binding.progressBar.visibility = View.VISIBLE

            // Register Firebase
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    // ProgressBar disembunyikan nanti setelah semua proses selesai

                    if (task.isSuccessful) {
                        // --- BAGIAN YANG DIUBAH ---

                        val firebaseUser = auth.currentUser
                        if (firebaseUser != null) {
                            val uid = firebaseUser.uid

                            // Siapkan data user untuk disimpan ke Firestore
                            val userMap = hashMapOf(
                                "uid" to uid,
                                "email" to email,
                                "fullName" to name, // Langsung simpan nama
                                "username" to "", // Akan diisi di halaman profil
                                "country" to "",  // Akan diisi di halaman pilih negara
                                "isSetupComplete" to false // Penanda user baru
                            )

                            // Simpan ke Firestore
                            val db = FirebaseFirestore.getInstance()
                            db.collection("users").document(uid)
                                .set(userMap)
                                .addOnSuccessListener {
                                    // Jika sukses membuat dokumen, baru pindah halaman
                                    binding.progressBar.visibility = View.GONE
                                    Toast.makeText(this, "Registrasi Berhasil!", Toast.LENGTH_SHORT).show()

                                    val intent = Intent(this, SelectCountryActivity::class.java)
                                    // Hapus semua activity sebelumnya (Login, Register) agar tidak bisa kembali
                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    startActivity(intent)
                                    finish()
                                }
                                .addOnFailureListener { e ->
                                    binding.progressBar.visibility = View.GONE
                                    Toast.makeText(this, "Gagal menyimpan profil: ${e.message}", Toast.LENGTH_LONG).show()
                                }
                        }

                        // --- AKHIR BAGIAN YANG DIUBAH ---

                    } else {
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(this, "Register failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }

        // Link ke Login
        binding.tvLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            // jangan di-finish() agar bisa kembali ke halaman login jika diperlukan
        }
    }
}