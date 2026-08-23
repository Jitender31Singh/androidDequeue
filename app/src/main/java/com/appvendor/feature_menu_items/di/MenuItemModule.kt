package com.appvendor.feature_menu_items.di

import com.appvendor.feature_menu_items.data.remote.MenuItemApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MenuItemModule {

    @Provides
    @Singleton
    fun provideMenuItemApiService(retrofit: Retrofit): MenuItemApiService =
        retrofit.create(MenuItemApiService::class.java)
}
