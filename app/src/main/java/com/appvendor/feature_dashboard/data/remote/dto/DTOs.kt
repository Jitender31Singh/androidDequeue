package com.appvendor.feature_dashboard.data.remote.dto

data class OrderDto(
    val id: String,
    val customerName: String,
    val customerPhone: String,
    val items: List<OrderItemDto>,
    val status: String,
    val totalAmount: Double,
    val createdAt: Long,
    val updatedAt: Long,
    val notes: String?
)

data class OrderItemDto(
    val id: String,
    val name: String,
    val quantity: Int,
    val price: Double,
    val notes: String?
)

data class UpdateStatusRequest(
    val status: String
)

data class PaginatedResponse<T>(
    val content: List<T>,
    val totalElements: Long,
    val totalPages: Int,
    val currentPage: Int,
    val hasNext: Boolean
)
