package com.appvendor.feature_geofence.domain.usecase

import com.appvendor.feature_geofence.domain.repository.GeofenceRepository
import javax.inject.Inject

/**
 * Use case to toggle the enablement status of the geofence.
 */
class ToggleGeofenceUseCase @Inject constructor(
    private val repository: GeofenceRepository
) {
    suspend operator fun invoke(isEnabled: Boolean) {
        repository.toggleGeofence(isEnabled)
    }
}
