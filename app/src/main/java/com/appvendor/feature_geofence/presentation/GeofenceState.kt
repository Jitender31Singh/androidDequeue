package com.appvendor.feature_geofence.presentation

import com.appvendor.feature_geofence.domain.model.GeofenceArea

/**
 * UI State for the Geofence presentation layer.
 */
data class GeofenceState(
    val area: GeofenceArea? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)
