package com.appvendor.feature_orders.domain.model

data class Order(
    val id: String,
    val queueNumber: String,
    val status: OrderStatus,
    val totalAmount: Double,
    val customerNote: String?,
    val items: List<OrderItem>,
    val metadata: Map<String, String>?,
    val customFields: Map<String, String>?,
    val couponCode: String?,
    val couponDiscount: Double?,
    val taxName: String?,
    val taxAmount: Double?,
    val serviceChargeName: String?,
    val serviceChargeAmount: Double?,
    val createdAt: String,
    val updatedAt: String
)

data class SelectedOption(
    val id: String,
    val name: String,
    val additionalPrice: Double
)

data class SelectedCustomization(
    val customizationId: String,
    val name: String,
    val selectedOptions: List<SelectedOption>
)

data class OrderItem(
    val menuItemId: String,
    val menuItemName: String,
    val quantity: Int,
    val unitPrice: Double,
    val totalPrice: Double,
    val specialInstructions: String?,
    val selectedCustomizations: List<SelectedCustomization> = emptyList()
)

data class OrderSummary(
    val id: String,
    val queueNumber: String,
    val totalAmount: Double,
    val status: OrderStatus,
    val itemCount: Int,
    val metadata: Map<String, String>?,
    val customFields: Map<String, String>?,
    val createdAt: String
)

enum class OrderStatus {
    PENDING, ACCEPTED, PREPARING, READY, COMPLETED, CANCELLED;

    fun nextStatus(): OrderStatus? = when (this) {
        PENDING -> ACCEPTED
        ACCEPTED -> PREPARING
        PREPARING -> READY
        READY -> COMPLETED
        else -> null
    }

    fun actionLabel(): String = when (this) {
        PENDING -> "Accept"
        ACCEPTED -> "Start Preparing"
        PREPARING -> "Mark Ready"
        READY -> "Mark Completed"
        else -> ""
    }

    fun displayLabel(): String = when (this) {
        PENDING -> "Pending"
        ACCEPTED -> "Accepted"
        PREPARING -> "Preparing"
        READY -> "Ready"
        COMPLETED -> "Completed"
        CANCELLED -> "Cancelled"
    }
}
