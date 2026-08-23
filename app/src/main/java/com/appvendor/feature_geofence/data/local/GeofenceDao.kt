package com.appvendor.feature_geofence.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Geofence operations.
 */
@Dao
interface GeofenceDao {
    @Query("SELECT * FROM geofence_table WHERE id = 'default_geofence' LIMIT 1")
    fun getGeofence(): Flow<GeofenceEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGeofence(geofence: GeofenceEntity)

    @Query("UPDATE geofence_table SET isEnabled = :isEnabled WHERE id = 'default_geofence'")
    suspend fun updateGeofenceStatus(isEnabled: Boolean)
}
