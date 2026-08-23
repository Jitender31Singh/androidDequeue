package com.appvendor.feature_dashboard.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey val id: String,
    val customerName: String,
    val customerPhone: String,
    val items: String, // JSON string
    val status: String,
    val totalAmount: Double,
    val createdAt: Long,
    val updatedAt: Long,
    val notes: String?
)
