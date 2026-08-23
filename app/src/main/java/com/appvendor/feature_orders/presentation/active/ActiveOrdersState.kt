package com.appvendor.feature_orders.presentation.active

import com.appvendor.feature_orders.domain.model.OrderStatus
import com.appvendor.feature_orders.domain.model.OrderSummary

data class ActiveOrdersState(
    val orders: List<OrderSummary> = emptyList(),
    val filteredOrders: List<OrderSummary> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val searchQuery: String = "",
    val filterStatus: OrderStatus? = null, // null means all
    val userPermissions: Set<String> = emptySet(),
    val userRoles: Set<String> = emptySet(),
    val orderVisibilityStatuses: Set<String> = emptySet()
)
