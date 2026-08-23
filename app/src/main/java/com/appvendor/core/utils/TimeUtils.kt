package com.appvendor.core.utils

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

object TimeUtils {
    fun getRelativeTime(isoTimestamp: String): String {
        return try {
            val past = Instant.parse(isoTimestamp)
            val now = Instant.now()
            
            val minutes = ChronoUnit.MINUTES.between(past, now)
            val hours = ChronoUnit.HOURS.between(past, now)
            val days = ChronoUnit.DAYS.between(past, now)
            
            when {
                minutes < 1 -> "Just now"
                minutes < 60 -> "$minutes min ago"
                hours < 24 -> "$hours hr ago"
                days == 1L -> "Yesterday"
                else -> "$days days ago"
            }
        } catch (e: Exception) {
            isoTimestamp
        }
    }

    fun getCurrentDateFormatted(): String {
        val now = Instant.now().atZone(ZoneId.systemDefault())
        val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")
        return now.format(formatter)
    }
}
