package com.appvendor.feature_geofence.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity representing a geofence area in the local database.
 */
@Entity(tableName = "geofence_table")
data class GeofenceEntity(
    @PrimaryKey val id: String = "default_geofence",
    val latitude: Double,
    val longitude: Double,
    val radiusInMeters: Float,
    val isEnabled: Boolean
)
