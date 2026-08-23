package com.appvendor.feature_orders.data.remote.dto

// ── Responses ─────────────────────────────────────────────────────────────────

data class SelectedOptionDto(
    val id: String = "",
    val name: String = "",
    val additionalPrice: Double = 0.0
)

data class SelectedCustomizationDto(
    val customizationId: String = "",
    val name: String = "",
    val selectedOptions: List<SelectedOptionDto> = emptyList()
)

data class OrderItemDto(
    val menuItemId: String = "",
    val menuItemName: String = "",
    val quantity: Int = 0,
    val unitPrice: Double = 0.0,
    val totalPrice: Double = 0.0,
    val specialInstructions: String? = null,
    val selectedCustomizations: List<SelectedCustomizationDto>? = emptyList()
)

data class OrderResponseDto(
    val id: String = "",
    val queueNumber: String = "",
    val status: String = "",
    val totalAmount: Double = 0.0,
    val customerNote: String? = null,
    val items: List<OrderItemDto> = emptyList(),
    val metadata: Map<String, String>? = null,
    val customFields: Map<String, String>? = null,
    val couponCode: String? = null,
    val couponDiscount: Double? = null,
    val taxName: String? = null,
    val taxAmount: Double? = null,
    val serviceChargeName: String? = null,
    val serviceChargeAmount: Double? = null,
    val createdAt: String = "",
    val updatedAt: String = ""
)

data class OrderSummaryDto(
    val id: String = "",
    val queueNumber: String = "",
    val totalAmount: Double = 0.0,
    val status: String = "",
    val itemCount: Int = 0,
    val items: List<OrderItemDto>? = null,
    val metadata: Map<String, String>? = null,
    val customFields: Map<String, String>? = null,
    val createdAt: String = ""
)

data class PageResponseDto<T>(
    val content: List<T> = emptyList(),
    val totalElements: Long = 0,
    val totalPages: Int = 0,
    val number: Int = 0,
    val last: Boolean = true
)

// ── Requests ──────────────────────────────────────────────────────────────────

data class UpdateOrderStatusRequest(
    val status: String,
    val note: String? = null
)
