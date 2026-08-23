package com.appvendor.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.appvendor.feature_dashboard.data.local.OrderDao
import com.appvendor.feature_dashboard.data.local.OrderEntity
import com.appvendor.feature_geofence.data.local.GeofenceDao
import com.appvendor.feature_geofence.data.local.GeofenceEntity
import com.appvendor.feature_items.data.local.CategoryEntity
import com.appvendor.feature_items.data.local.ItemDao
import com.appvendor.feature_items.data.local.ItemEntity

/**
 * Main Room Database for the AppVendor application.
 *
 * Entities: OrderEntity, ItemEntity, CategoryEntity, GeofenceEntity
 * Version bump required whenever the schema changes.
 */
@Database(
    entities = [
        OrderEntity::class,
        ItemEntity::class,
        CategoryEntity::class,
        GeofenceEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun orderDao(): OrderDao
    abstract fun itemDao(): ItemDao
    abstract fun geofenceDao(): GeofenceDao

    companion object {
        const val DATABASE_NAME = "appvendor_database"
    }
}
