package com.octanews.infoin.ui.auth

import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.octanews.infoin.R
import com.octanews.infoin.databinding.ActivityEditProfileBinding
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.launch

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var supabase: SupabaseClient

    private var imageUri: Uri? = null

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            imageUri = uri
            Glide.with(this).load(uri).circleCrop().into(binding.ivProfile)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Inisialisasi Supabase (GANTI DENGAN KUNCI & URL-MU)
        supabase = createSupabaseClient(
            supabaseUrl = "URL_PROYEK_SUPABASE_MU",
            supabaseKey = "ANON_PUBLIC_KEY_SUPABASE_MU"
        ) {
            install(Storage)
        }

        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener { finish() }

        loadCurrentUserData()

        binding.ivProfile.setOnClickListener {
            galleryLauncher.launch("image/*")
        }
        binding.tvChangePhoto.setOnClickListener {
            galleryLauncher.launch("image/*")
        }
    }

    private fun loadCurrentUserData() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).get().addOnSuccessListener { doc ->
            if (doc != null) {
                binding.etUsername.setText(doc.getString("username"))
                binding.etFullName.setText(doc.getString("fullName"))
                binding.etBio.setText(doc.getString("bio"))
                binding.etWebsite.setText(doc.getString("website"))
                Glide.with(this).load(doc.getString("profileImageUrl")).placeholder(R.drawable.ic_profile_placeholder).circleCrop().into(binding.ivProfile)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.edit_profile_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_save -> {
                saveProfileChanges()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun saveProfileChanges() {
        val username = binding.etUsername.text.toString().trim()
        val fullName = binding.etFullName.text.toString().trim()
        val bio = binding.etBio.text.toString().trim()
        val website = binding.etWebsite.text.toString().trim()

        if (username.isEmpty() || fullName.isEmpty()) {
            Toast.makeText(this, "Nama dan Username tidak boleh kosong", Toast.LENGTH_SHORT).show()
            return
        }

        if (imageUri != null) {
            // Jika ada gambar baru, upload dulu
            uploadImageAndUpdateProfile(imageUri!!, username, fullName, bio, website)
        } else {
            // Jika tidak ada gambar baru, langsung update data teks
            updateProfileData(username, fullName, bio, website, null)
        }
    }

    private fun uploadImageAndUpdateProfile(uri: Uri, username: String, fullName: String, bio: String, website: String) {
        val currentUser = auth.currentUser ?: return
        Toast.makeText(this, "Mengupload foto...", Toast.LENGTH_SHORT).show()

        currentUser.getIdToken(true).addOnSuccessListener { result ->
            lifecycleScope.launch {
                try {
                    val fileName = "profile_images/${currentUser.uid}.jpg"
                    val fileBytes = contentResolver.openInputStream(uri)?.readBytes()
                    if (fileBytes != null) {
                        supabase.storage.from("profile_images").upload(fileName, fileBytes, upsert = true)
                        val publicUrl = supabase.storage.from("profile_images").publicUrl(fileName)
                        updateProfileData(username, fullName, bio, website, publicUrl)
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@EditProfileActivity, "Upload Gagal: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun updateProfileData(username: String, fullName: String, bio: String, website: String, newImageUrl: String?) {
        val uid = auth.currentUser?.uid ?: return

        val updates = mutableMapOf<String, Any>(
            "username" to username,
            "fullName" to fullName,
            "bio" to bio,
            "website" to website
        )

        if (newImageUrl != null) {
            updates["profileImageUrl"] = newImageUrl
        }

        db.collection("users").document(uid).update(updates)
            .addOnSuccessListener {
                Toast.makeText(this, "Profil berhasil diperbarui", Toast.LENGTH_SHORT).show()
                setResult(RESULT_OK) // Beri sinyal ke ProfileFragment bahwa ada perubahan
                finish() // Kembali ke halaman profil
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal memperbarui profil: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}