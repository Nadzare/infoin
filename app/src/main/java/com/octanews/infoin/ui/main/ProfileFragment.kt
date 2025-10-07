package com.octanews.infoin.ui.main

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.octanews.infoin.R
import com.octanews.infoin.databinding.FragmentProfileBinding
import com.octanews.infoin.ui.adapter.ProfileTabsAdapter
import com.octanews.infoin.ui.auth.EditProfileActivity
import com.octanews.infoin.ui.auth.SettingsActivity

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val auth by lazy { FirebaseAuth.getInstance() }
    private val db by lazy { FirebaseFirestore.getInstance() }

    // --- PERUBAHAN 1: Tambahkan ActivityResultLauncher ---
    private val editProfileLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // Jika hasil dari EditProfileActivity adalah OK, muat ulang data profil
            loadUserProfile()
            Toast.makeText(context, "Profil berhasil di-update", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewPagerAndTabs()
        loadUserProfile()

        // Settings icon click
        binding.ivSettings.setOnClickListener {
            startActivity(Intent(activity, SettingsActivity::class.java))
        }

        // --- PERUBAHAN 2: Ganti OnClickListener ---
        binding.btnEditProfile.setOnClickListener {
            val intent = Intent(activity, EditProfileActivity::class.java)
            editProfileLauncher.launch(intent) // Gunakan launcher, bukan startActivity
        }
    }

    private fun setupViewPagerAndTabs() {
        val adapter = ProfileTabsAdapter(this)
        binding.viewPagerProfile.adapter = adapter

        TabLayoutMediator(binding.tabLayoutProfile, binding.viewPagerProfile) { tab, position ->
            when (position) {
                0 -> tab.text = "News"
                1 -> tab.text = "Recent"
            }
        }.attach()
    }

    private fun loadUserProfile() {
        val uid = auth.currentUser?.uid ?: return

        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val fullName = document.getString("fullName")
                    val bio = document.getString("bio")
                    val profileImageUrl = document.getString("profileImageUrl")
                    val username = document.getString("username")
                    val website = document.getString("website")

                    binding.tvFullName.text = fullName
                    binding.tvUsername.text = if (!username.isNullOrEmpty()) "@$username" else ""
                    binding.tvBio.text = bio ?: "No bio yet. Tap edit to add one."

                    Glide.with(this)
                        .load(profileImageUrl)
                        .placeholder(R.drawable.ic_profile_placeholder)
                        .error(R.drawable.ic_profile_placeholder)
                        .circleCrop()
                        .into(binding.ivProfile)

                    // Website button logic
                    if (!website.isNullOrEmpty()) {
                        binding.btnWebsite.isEnabled = true
                        binding.btnWebsite.alpha = 1.0f
                        binding.btnWebsite.setOnClickListener {
                            val intent = Intent(Intent.ACTION_VIEW)
                            intent.data = android.net.Uri.parse(if (website.startsWith("http")) website else "https://$website")
                            startActivity(intent)
                        }
                    } else {
                        binding.btnWebsite.isEnabled = false
                        binding.btnWebsite.alpha = 0.5f
                        binding.btnWebsite.setOnClickListener(null)
                    }
                }
            }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}