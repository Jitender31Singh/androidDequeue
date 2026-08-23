package com.appvendor.feature_items.presentation

import com.appvendor.feature_items.domain.model.Category
import com.appvendor.feature_items.domain.model.Item

data class ItemsState(
    val categories: List<Category> = emptyList(),
    val items: List<Item> = emptyList(),
    val selectedCategoryId: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isRefreshing: Boolean = false
)
