package com.appvendor.feature_categories.data.remote.dto

data class CategoryDto(
    val id: String = "",
    val name: String = "",
    val description: String? = null,
    val image: String? = null,
    val sortOrder: Int = 0,
    val active: Boolean = true,
    val itemCount: Int = 0
)

data class CreateCategoryRequest(
    val name: String,
    val description: String?,
    val sortOrder: Int
)

data class UpdateCategoryRequest(
    val name: String,
    val description: String?
)

data class SortOrderItem(
    val id: String,
    val sortOrder: Int
)

data class SortOrderRequest(
    val items: List<SortOrderItem>
)
