package com.appvendor.core.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object DateUtils {
    fun formatToIstReadableTime(isoString: String): String {
        if (isoString.isBlank()) return "--:--"
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            parser.timeZone = TimeZone.getTimeZone("UTC")
            var date = parser.parse(isoString)
            if (date == null) {
                val parser2 = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
                parser2.timeZone = TimeZone.getTimeZone("UTC")
                date = parser2.parse(isoString)
            }
            if (date != null) {
                val formatter = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
                formatter.timeZone = TimeZone.getTimeZone("Asia/Kolkata")
                formatter.format(date)
            } else {
                isoString
            }
        } catch (e: Exception) {
            isoString
        }
    }

    fun formatMillisToIstReadableTime(millis: Long): String {
        return try {
            val date = Date(millis)
            val formatter = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
            formatter.timeZone = TimeZone.getTimeZone("Asia/Kolkata")
            formatter.format(date)
        } catch (e: Exception) {
            "--:--"
        }
    }
}
