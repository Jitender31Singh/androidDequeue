package com.appvendor.feature_geofence.data.repository

import com.appvendor.feature_geofence.data.local.GeofenceDao
import com.appvendor.feature_geofence.data.local.GeofenceEntity
import com.appvendor.feature_geofence.domain.model.GeofenceArea
import com.appvendor.feature_geofence.domain.repository.GeofenceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Implementation of [GeofenceRepository] using Room DAO.
 */
class GeofenceRepositoryImpl @Inject constructor(
    private val dao: GeofenceDao
) : GeofenceRepository {

    override fun getGeofence(): Flow<GeofenceArea?> {
        return dao.getGeofence().map { it?.toDomainModel() }
    }

    override suspend fun updateGeofence(area: GeofenceArea) {
        dao.insertGeofence(area.toEntity())
    }

    override suspend fun toggleGeofence(isEnabled: Boolean) {
        dao.updateGeofenceStatus(isEnabled)
    }
}

fun GeofenceEntity.toDomainModel() = GeofenceArea(
    id = id,
    latitude = latitude,
    longitude = longitude,
    radiusInMeters = radiusInMeters,
    isEnabled = isEnabled
)

fun GeofenceArea.toEntity() = GeofenceEntity(
    id = id,
    latitude = latitude,
    longitude = longitude,
    radiusInMeters = radiusInMeters,
    isEnabled = isEnabled
)
