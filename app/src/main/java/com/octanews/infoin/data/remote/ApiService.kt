package com.octanews.infoin.data.remote

import com.octanews.infoin.data.model.NewsDataResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("news")
    fun getNews(
        @Query("apikey") apiKey: String,
        @Query("country") country: String?, // Jadikan nullable
        @Query("language") language: String?, // Jadikan nullable
        @Query("category") category: String?,
        @Query("q") query: String? // <-- TAMBAHKAN INI
    ): Call<NewsDataResponse>
}