package com.appvendor.feature_dashboard.domain.repository

import com.appvendor.feature_dashboard.domain.model.Order
import com.appvendor.feature_dashboard.domain.model.OrderStatus
import kotlinx.coroutines.flow.Flow

interface OrderRepository {
    fun getActiveOrder(vendorId: String): Flow<Order?>
    fun getPendingOrders(vendorId: String): Flow<List<Order>>
    suspend fun updateOrderStatus(orderId: String, status: OrderStatus): Result<Order>
    suspend fun refreshOrders(vendorId: String): Result<Unit>
}
