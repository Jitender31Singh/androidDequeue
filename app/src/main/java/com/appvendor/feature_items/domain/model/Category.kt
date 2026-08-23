package com.appvendor.feature_items.domain.model

/**
 * Represents a category of items in the vendor app.
 */
data class Category(
    val id: String,
    val name: String,
    val description: String,
    val fields: List<CategoryField>
)
