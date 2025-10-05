package com.octanews.infoin.data.model

data class NewsDataResponse( // Nama diubah menjadi NewsDataResponse
    val status: String,
    val totalResults: Int,
    val results: List<NewsArticle> // Menggunakan 'results' dan 'NewsArticle'
)