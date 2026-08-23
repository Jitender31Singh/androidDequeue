package com.appvendor.feature_settings.di

import com.appvendor.feature_settings.data.remote.SettingsApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SettingsModule {
    
    @Provides
    @Singleton
    fun provideSettingsApiService(retrofit: Retrofit): SettingsApiService {
        return retrofit.create(SettingsApiService::class.java)
    }
}
