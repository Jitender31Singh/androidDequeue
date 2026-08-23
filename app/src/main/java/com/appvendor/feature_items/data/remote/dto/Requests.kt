package com.appvendor.feature_items.data.remote.dto

data class CreateCategoryRequest(
    val name: String,
    val description: String,
    val fields: List<CategoryFieldDto>
)

data class CreateItemRequest(
    val categoryId: String,
    val name: String,
    val description: String,
    val price: Double,
    val stockQuantity: Int,
    val imageUrl: String?,
    val dynamicFields: Map<String, String>
)
