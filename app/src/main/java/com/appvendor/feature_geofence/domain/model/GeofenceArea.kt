package com.appvendor.feature_geofence.domain.model

/**
 * Domain model representing a Geofence area.
 */
data class GeofenceArea(
    val id: String = "default_geofence",
    val latitude: Double,
    val longitude: Double,
    val radiusInMeters: Float,
    val isEnabled: Boolean
)
