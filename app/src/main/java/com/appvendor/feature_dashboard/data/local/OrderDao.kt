package com.appvendor.feature_dashboard.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderDao {
    @Query("SELECT * FROM orders WHERE status != 'COMPLETED' AND status != 'CANCELLED' ORDER BY createdAt DESC")
    fun getActiveOrders(): Flow<List<OrderEntity>>
    
    @Query("SELECT * FROM orders WHERE status = 'PENDING' ORDER BY createdAt ASC")
    fun getPendingOrders(): Flow<List<OrderEntity>>
    
    @Query("SELECT * FROM orders WHERE id = :orderId")
    suspend fun getOrderById(orderId: String): OrderEntity?
    
    @Upsert
    suspend fun upsertOrders(orders: List<OrderEntity>)
    
    @Upsert
    suspend fun upsertOrder(order: OrderEntity)
    
    @Query("DELETE FROM orders WHERE status = 'COMPLETED' AND updatedAt < :threshold")
    suspend fun deleteOldCompletedOrders(threshold: Long)
}
