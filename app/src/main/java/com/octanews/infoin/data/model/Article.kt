//package com.octanews.infoin.data.model
//
//data class Article(
//    val source: Source?,
//    val author: String?,
//    val title: String?,
//    val description: String?,
//    val url: String?,
//    val urlToImage: String?,
//    val publishedAt: String?,
//    val content: String?
//)
//
//data class Source(
//    val id: String?,
//    val name: String?
//)


// Article.kt
//package com.octanews.infoin.data.model
//
//
//data class Article(
//    val title: String?,
//    val description: String?,
//    val content: String?,
//    val url: String?,
//    val image: String?,
//    val publishedAt: String?,
//    val source: Source?
//)
//
//data class Source(
//    val name: String?,
//    val url: String?
//)


package com.octanews.infoin.data.model

data class NewsArticle( // Nama diubah menjadi NewsArticle
    val title: String? = null,
    val link: String? = null,
    val creator: List<String>? = null,
    val description: String? = null,
    val content: String? = null,
    val pubDate: String? = null,
    val image_url: String? = null,
    val source_id: String? = null
)

