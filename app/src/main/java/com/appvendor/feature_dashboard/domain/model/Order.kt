package com.appvendor.feature_dashboard.domain.model

data class Order(
    val id: String,
    val customerName: String,
    val customerPhone: String,
    val items: List<OrderItem>,
    val status: OrderStatus,
    val totalAmount: Double,
    val createdAt: Long,
    val updatedAt: Long,
    val notes: String?
)

data class OrderItem(
    val id: String,
    val name: String,
    val quantity: Int,
    val price: Double,
    val notes: String?
)
