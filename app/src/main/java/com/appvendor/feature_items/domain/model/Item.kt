package com.appvendor.feature_items.domain.model

/**
 * Represents an item belonging to a category.
 */
data class Item(
    val id: String,
    val categoryId: String,
    val name: String,
    val description: String,
    val price: Double,
    val stockQuantity: Int,
    val imageUrl: String?,
    val dynamicFields: Map<String, String> // Maps fieldId to its string representation
)
