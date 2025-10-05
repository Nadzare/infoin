package com.octanews.infoin.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object DateFormatter {

    fun getTimeAgo(dateString: String?): String {
        if (dateString.isNullOrEmpty()) return ""

        return try {
            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
            val pastDate: Date = format.parse(dateString) ?: return ""
            val now = Date()

            val seconds = TimeUnit.MILLISECONDS.toSeconds(now.time - pastDate.time)
            val minutes = TimeUnit.MILLISECONDS.toMinutes(now.time - pastDate.time)
            val hours = TimeUnit.MILLISECONDS.toHours(now.time - pastDate.time)
            val days = TimeUnit.MILLISECONDS.toDays(now.time - pastDate.time)

            when {
                seconds < 60 -> "$seconds s ago"
                minutes < 60 -> "$minutes m ago"
                hours < 24 -> "$hours h ago"
                days < 7 -> "$days d ago"
                else -> SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(pastDate)
            }
        } catch (e: Exception) {
            ""
        }
    }
}