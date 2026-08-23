package com.appvendor.feature_shop_profile.di

import com.appvendor.feature_shop_profile.data.remote.ShopProfileApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ShopProfileModule {
    
    @Provides
    @Singleton
    fun provideShopProfileApiService(retrofit: Retrofit): ShopProfileApiService {
        return retrofit.create(ShopProfileApiService::class.java)
    }
}
