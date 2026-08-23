package com.appvendor.feature_menu_items.data.remote.dto

import com.appvendor.feature_customizations.data.remote.dto.CustomizationGroupDto

data class MenuItemDto(
    val id: String = "",
    val name: String = "",
    val description: String? = null,
    val price: Double = 0.0,
    val image: String? = null,
    val available: Boolean = true,
    val visible: Boolean = true,
    val categoryId: String = "",
    val categoryName: String? = null,
    val preparationTime: Int = 15,
    val sortOrder: Int = 0,
    val customizationGroups: List<CustomizationGroupDto>? = null
)

data class CreateMenuItemRequest(
    val name: String,
    val description: String?,
    val price: Double,
    val categoryId: String,
    val preparationTime: Int,
    val sortOrder: Int,
    val customizationGroupIds: List<String>,
    val image: String? = null,
    val tags: List<String> = emptyList()
)

data class UpdateMenuItemRequest(
    val name: String?,
    val description: String?,
    val price: Double?,
    val categoryId: String?,
    val preparationTime: Int?,
    val image: String? = null,
    val customizationGroupIds: List<String>?
)

data class AvailabilityRequest(
    val available: Boolean
)

data class VisibilityRequest(
    val visible: Boolean
)

data class SortOrderRequest(
    val items: List<SortOrderItem>
)

data class SortOrderItem(
    val id: String,
    val sortOrder: Int
)

data class PageResponseDto<T>(
    val content: List<T> = emptyList(),
    val totalElements: Long = 0,
    val totalPages: Int = 0,
    val number: Int = 0,
    val last: Boolean = true
)
