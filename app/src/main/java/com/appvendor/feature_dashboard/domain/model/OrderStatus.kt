package com.appvendor.feature_dashboard.domain.model

enum class OrderStatus(val displayName: String) {
    PENDING("Pending"),
    CONFIRMED("Confirmed"),
    PREPARING("Preparing"),
    READY("Ready"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled");
    
    fun nextStatus(): OrderStatus? = when(this) {
        PENDING -> CONFIRMED
        CONFIRMED -> PREPARING
        PREPARING -> READY
        READY -> COMPLETED
        COMPLETED -> null
        CANCELLED -> null
    }
}
