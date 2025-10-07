package com.octanews.infoin.ui.auth

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore // Import Firestore
import com.octanews.infoin.MainActivity
import com.octanews.infoin.R
import com.octanews.infoin.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private var isLoggingIn = false

    // Variabel untuk Google Sign-In
    private lateinit var googleSignInClient: GoogleSignInClient
    private val googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                binding.progressBar.visibility = View.GONE // Sembunyikan progress bar jika gagal
                Toast.makeText(this, "Login Google Gagal: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            binding.progressBar.visibility = View.GONE // Sembunyikan progress bar jika pengguna batal
        }
    }



    override fun onCreate(savedInstanceState: Bundle?) {

        installSplashScreen()

        super.onCreate(savedInstanceState)

        // Inisialisasi binding
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inisialisasi GoogleSignInOptions sebelum digunakan
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // --- Atur semua listener ---
        auth = FirebaseAuth.getInstance()
        setupListeners()
    }

    override fun onStart() {
        super.onStart()
        // Jika user membuka aplikasi dan sudah login, kita cek profilnya
        if (auth.currentUser != null) {
            checkUserProfile()
        }
    }

    private fun setupListeners() {
        binding.btnLogin.setOnClickListener { loginUser() }
        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
        binding.tvForgot.setOnClickListener { showResetPasswordDialog() }
        binding.btnGoogle.setOnClickListener { signInWithGoogle() }
    }

    // --- FUNGSI INTI BARU UNTUK NAVIGASI ---
    private fun checkUserProfile() {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            // Jika tidak ada user, sembunyikan loading dan jangan lakukan apa-apa
            binding.progressBar.visibility = View.GONE
            return
        }

        // Tampilkan loading saat pengecekan
        binding.progressBar.visibility = View.VISIBLE

        val db = FirebaseFirestore.getInstance()
        val userRef = db.collection("users").document(uid)

        userRef.get()
            .addOnSuccessListener { document ->
                // Sembunyikan loading setelah dapat data
                binding.progressBar.visibility = View.GONE

                val intent: Intent
                if (document != null && document.exists()) {
                    val isSetupComplete = document.getBoolean("isSetupComplete") ?: false
                    intent = if (isSetupComplete) {
                        // Jika setup selesai, ke MainActivity
                        Intent(this, MainActivity::class.java)
                    } else {
                        // Jika belum selesai, ke alur personalisasi
                        Intent(this, SelectCountryActivity::class.java)
                    }
                } else {
                    // Jika dokumen tidak ada, anggap user baru (misal login via Google pertama kali)
                    // Kita perlu membuat dokumennya di sini
                    createInitialUserProfile {
                        val setupIntent = Intent(this, SelectCountryActivity::class.java)
                        setupIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(setupIntent)
                        finish()
                    }
                    return@addOnSuccessListener // Hentikan eksekusi di sini, tunggu createInitialUserProfile selesai
                }

                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .addOnFailureListener {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this, "Gagal memuat profil, coba lagi", Toast.LENGTH_SHORT).show()
            }
    }

    // Fungsi baru untuk membuat profil jika belum ada (berguna untuk Google Sign-In pertama kali)
    private fun createInitialUserProfile(onComplete: () -> Unit) {
        val firebaseUser = auth.currentUser ?: return
        val uid = firebaseUser.uid
        val email = firebaseUser.email
        val name = firebaseUser.displayName ?: ""

        val userMap = hashMapOf(
            "uid" to uid, "email" to email, "fullName" to name, "username" to "",
            "country" to "", "isSetupComplete" to false
        )

        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(uid).set(userMap)
            .addOnSuccessListener { onComplete() } // Jalankan onComplete callback setelah sukses
            .addOnFailureListener { e ->
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this, "Gagal membuat profil: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }


    private fun signInWithGoogle() {
        binding.progressBar.visibility = View.VISIBLE
        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // --- DIUBAH: Panggil checkUserProfile ---
                    checkUserProfile()
                } else {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this, task.exception?.localizedMessage ?: "Login Firebase Gagal", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun loginUser() {
        if (isLoggingIn) return
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()

        if (!validateInput(email, password)) return

        isLoggingIn = true
        binding.progressBar.visibility = View.VISIBLE
        binding.btnLogin.isEnabled = false

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                isLoggingIn = false
                binding.btnLogin.isEnabled = true
                // Progress bar akan di-handle oleh checkUserProfile

                if (task.isSuccessful) {
                    // --- DIUBAH: Panggil checkUserProfile ---
                    checkUserProfile()
                } else {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this, task.exception?.localizedMessage ?: "Login gagal", Toast.LENGTH_LONG).show()
                }
            }
    }

    // Fungsi validasi dipisah agar lebih rapi
    private fun validateInput(email: String, pass: String): Boolean {
        binding.tilEmail.error = null
        binding.tilPassword.error = null
        if (email.isEmpty()) {
            binding.tilEmail.error = "Email tidak boleh kosong"
            return false
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = "Format email tidak valid"
            return false
        }
        if (pass.isEmpty()) {
            binding.tilPassword.error = "Password tidak boleh kosong"
            return false
        }
        return true
    }

    // (Fungsi showResetPasswordDialog tidak perlu diubah, biarkan seperti aslinya)
    private fun showResetPasswordDialog() {
        //... isi fungsi reset passwordmu ...
    }
}