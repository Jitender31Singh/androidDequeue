package com.appvendor.feature_geofence.di

import com.appvendor.feature_geofence.data.repository.GeofenceRepositoryImpl
import com.appvendor.feature_geofence.domain.repository.GeofenceRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class GeofenceDataModule {

    @Binds
    @Singleton
    abstract fun bindGeofenceRepository(
        geofenceRepositoryImpl: GeofenceRepositoryImpl
    ): GeofenceRepository
}
