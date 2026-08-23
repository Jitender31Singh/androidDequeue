package com.appvendor.core.database

import androidx.room.TypeConverter
import java.util.Date

/**
 * Type converters for Room database to handle custom data types.
 */
class Converters {

    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
    
    // Add more converters (e.g., for JSON/Lists) as needed using Gson or Moshi
}
