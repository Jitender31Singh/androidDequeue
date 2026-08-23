package com.appvendor.feature_geofence.domain.repository

import com.appvendor.feature_geofence.domain.model.GeofenceArea
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Geofence data operations in the domain layer.
 */
interface GeofenceRepository {
    /**
     * Retrieves the current geofence area.
     */
    fun getGeofence(): Flow<GeofenceArea?>
    
    /**
     * Updates the geofence area properties.
     */
    suspend fun updateGeofence(area: GeofenceArea)
    
    /**
     * Toggles the enablement state of the geofence.
     */
    suspend fun toggleGeofence(isEnabled: Boolean)
}
