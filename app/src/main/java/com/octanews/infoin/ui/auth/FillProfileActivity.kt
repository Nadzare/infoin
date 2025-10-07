package com.octanews.infoin.ui.auth

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.octanews.infoin.MainActivity
import com.octanews.infoin.databinding.ActivityFillProfileBinding
import java.util.UUID

class FillProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFillProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    // Variabel untuk menyimpan URI gambar yang akan di-upload
    private var imageUri: Uri? = null

    // Launcher untuk memilih gambar dari galeri
    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            imageUri = uri
            // Tampilkan gambar yang dipilih menggunakan Glide
            Glide.with(this).load(imageUri).circleCrop().into(binding.ivProfile)
        }
    }

    // Launcher untuk meminta izin kamera
    private val requestCameraPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            Toast.makeText(this, "Fitur kamera belum siap, silakan pilih dari galeri untuk saat ini", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "Izin kamera ditolak", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFillProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        loadInitialData()

        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.ivProfile.setOnClickListener {
            showImageSourceDialog()
        }

        binding.btnNext.setOnClickListener {
            saveProfileAndFinish()
        }
    }

    private fun showImageSourceDialog() {
        val options = arrayOf("Pilih dari Galeri", "Ambil dari Kamera")
        AlertDialog.Builder(this)
            .setTitle("Ganti Foto Profil")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> galleryLauncher.launch("image/*") // Buka galeri
                    1 -> checkCameraPermissionAndLaunch() // Buka kamera
                }
            }
            .show()
    }

    private fun checkCameraPermissionAndLaunch() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Fitur kamera belum siap, silakan pilih dari galeri untuk saat ini", Toast.LENGTH_LONG).show()
        } else {
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun loadInitialData() {
        val user = auth.currentUser
        if (user != null) {
            binding.etEmail.setText(user.email)
            val dbUserRef = db.collection("users").document(user.uid)
            dbUserRef.get().addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val fullName = document.getString("fullName")
                    if (!fullName.isNullOrEmpty()) {
                        binding.etFullName.setText(fullName)
                    }
                }
            }
        }
    }

    private fun saveProfileAndFinish() {
        val username = binding.etUsername.text.toString().trim()
        val fullName = binding.etFullName.text.toString().trim()
        val phoneNumber = binding.etPhone.text.toString().trim()

        if (username.isEmpty() || fullName.isEmpty() || phoneNumber.isEmpty()) {
            Toast.makeText(this, "Semua kolom wajib diisi", Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnNext.isEnabled = false
        Toast.makeText(this, "Menyimpan profil...", Toast.LENGTH_SHORT).show()

        if (imageUri != null) {
            // Jika ada gambar baru, upload dulu
            uploadImageAndSaveProfile(imageUri!!, username, fullName, phoneNumber)
        } else {
            // Jika tidak ada gambar baru, langsung simpan data teks
            saveProfileData(username, fullName, phoneNumber, null)
        }
    }

    private fun uploadImageAndSaveProfile(uri: Uri, username: String, fullName: String, phoneNumber: String) {
        val uid = auth.currentUser?.uid ?: return
        val fileName = "profile_images/${uid}.jpg"
        val imageRef = storage.reference.child(fileName)

        imageRef.putFile(uri)
            .addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                    saveProfileData(username, fullName, phoneNumber, downloadUrl.toString())
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal mengupload foto: ${e.message}", Toast.LENGTH_SHORT).show()
                binding.btnNext.isEnabled = true
            }
    }

    private fun saveProfileData(username: String, fullName: String, phoneNumber: String, profileImageUrl: String?) {
        val uid = auth.currentUser?.uid ?: return
        val userRef = db.collection("users").document(uid)

        val profileData = mutableMapOf<String, Any>(
            "username" to username,
            "fullName" to fullName,
            "phoneNumber" to phoneNumber,
            "isSetupComplete" to true
        )

        if (profileImageUrl != null) {
            profileData["profileImageUrl"] = profileImageUrl
        }

        userRef.update(profileData)
            .addOnSuccessListener {
                Toast.makeText(this, "Profil berhasil disimpan!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal menyimpan profil: ${e.message}", Toast.LENGTH_SHORT).show()
                binding.btnNext.isEnabled = true
            }
    }
}