package com.appvendor.feature_staff.di

import com.appvendor.feature_staff.data.remote.StaffApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object StaffModule {
    
    @Provides
    @Singleton
    fun provideStaffApiService(retrofit: Retrofit): StaffApiService {
        return retrofit.create(StaffApiService::class.java)
    }
}
