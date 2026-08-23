package com.appvendor.feature_geofence.domain.usecase

import com.appvendor.feature_geofence.domain.model.GeofenceArea
import com.appvendor.feature_geofence.domain.repository.GeofenceRepository
import javax.inject.Inject

/**
 * Use case to update the geofence configuration.
 */
class UpdateGeofenceUseCase @Inject constructor(
    private val repository: GeofenceRepository
) {
    suspend operator fun invoke(area: GeofenceArea) {
        require(area.radiusInMeters in 100f..10000f) { "Radius must be between 100 and 10000 meters" }
        repository.updateGeofence(area)
    }
}
