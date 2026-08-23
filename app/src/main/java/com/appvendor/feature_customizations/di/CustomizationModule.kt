package com.appvendor.feature_customizations.di

import com.appvendor.feature_customizations.data.remote.CustomizationApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CustomizationModule {

    @Provides
    @Singleton
    fun provideCustomizationApiService(retrofit: Retrofit): CustomizationApiService =
        retrofit.create(CustomizationApiService::class.java)
}
