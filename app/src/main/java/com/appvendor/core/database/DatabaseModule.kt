package com.appvendor.core.database

import android.content.Context
import androidx.room.Room
import com.appvendor.feature_dashboard.data.local.OrderDao
import com.appvendor.feature_geofence.data.local.GeofenceDao
import com.appvendor.feature_items.data.local.ItemDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
        .fallbackToDestructiveMigration() // In production, provide proper migrations
        .build()
    }

    // ── DAOs ──────────────────────────────────────────────────────────────────

    @Provides
    @Singleton
    fun provideOrderDao(database: AppDatabase): OrderDao =
        database.orderDao()

    @Provides
    @Singleton
    fun provideItemDao(database: AppDatabase): ItemDao =
        database.itemDao()

    @Provides
    @Singleton
    fun provideGeofenceDao(database: AppDatabase): GeofenceDao =
        database.geofenceDao()
}
