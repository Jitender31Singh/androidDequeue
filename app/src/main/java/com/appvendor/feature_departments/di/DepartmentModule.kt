package com.appvendor.feature_departments.di

import com.appvendor.feature_departments.data.remote.DepartmentApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DepartmentModule {
    
    @Provides
    @Singleton
    fun provideDepartmentApiService(retrofit: Retrofit): DepartmentApiService {
        return retrofit.create(DepartmentApiService::class.java)
    }
}
