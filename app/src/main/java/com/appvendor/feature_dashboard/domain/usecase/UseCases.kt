package com.appvendor.feature_dashboard.domain.usecase

import com.appvendor.feature_dashboard.domain.model.Order
import com.appvendor.feature_dashboard.domain.model.OrderStatus
import com.appvendor.feature_dashboard.domain.repository.OrderRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetActiveOrderUseCase @Inject constructor(
    private val repository: OrderRepository
) {
    operator fun invoke(vendorId: String): Flow<Order?> {
        return repository.getActiveOrder(vendorId)
    }
}

class GetPendingOrdersUseCase @Inject constructor(
    private val repository: OrderRepository
) {
    operator fun invoke(vendorId: String): Flow<List<Order>> {
        return repository.getPendingOrders(vendorId)
    }
}

class UpdateOrderStatusUseCase @Inject constructor(
    private val repository: OrderRepository
) {
    suspend operator fun invoke(orderId: String, status: OrderStatus): Result<Order> {
        return repository.updateOrderStatus(orderId, status)
    }
}
