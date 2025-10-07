package com.octanews.infoin

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.octanews.infoin.databinding.ActivityDetailBinding

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding

    companion object {
        const val EXTRA_URL = "ARTICLE_URL"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val url = intent.getStringExtra(EXTRA_URL)


        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }


        binding.webView.webViewClient = object : WebViewClient() {
            // Tampilkan progress bar saat halaman mulai dimuat
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                binding.progressBar.visibility = View.VISIBLE
            }

            // Sembunyikan progress bar saat halaman selesai dimuat
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                binding.progressBar.visibility = View.GONE
            }
        }

        // Pastikan URL tidak kosong, lalu muat ke WebView
        if (url != null) {
            binding.webView.loadUrl(url)
        }
    }
}