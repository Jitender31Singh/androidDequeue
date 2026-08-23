package com.appvendor.feature_categories.presentation

import com.appvendor.feature_categories.domain.model.Category

data class CategoriesState(
    val categories: List<Category> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isUpdating: Boolean = false,
    val error: String? = null,
    
    // Dialog states
    val isCreateDialogOpen: Boolean = false,
    val editCategory: Category? = null
)
