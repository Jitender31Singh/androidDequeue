package com.appvendor.feature_geofence.domain.usecase

import com.appvendor.feature_geofence.domain.model.GeofenceArea
import com.appvendor.feature_geofence.domain.repository.GeofenceRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case to retrieve the current geofence configuration.
 */
class GetGeofenceUseCase @Inject constructor(
    private val repository: GeofenceRepository
) {
    operator fun invoke(): Flow<GeofenceArea?> {
        return repository.getGeofence()
    }
}
