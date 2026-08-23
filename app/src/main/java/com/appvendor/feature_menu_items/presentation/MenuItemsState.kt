package com.appvendor.feature_menu_items.presentation

import com.appvendor.feature_categories.domain.model.Category
import com.appvendor.feature_customizations.domain.model.CustomizationGroup
import com.appvendor.feature_menu_items.domain.model.MenuItem

data class MenuItemsState(
    val items: List<MenuItem> = emptyList(),
    val filteredItems: List<MenuItem> = emptyList(),
    val categories: List<Category> = emptyList(),
    val customizations: List<CustomizationGroup> = emptyList(),
    
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isUpdating: Boolean = false,
    val isUploadingImage: Boolean = false,
    val error: String? = null,
    
    val searchQuery: String = "",
    val filterCategoryId: String? = null, // null means all
    
    // Dialog state
    val isFormOpen: Boolean = false,
    val editingItem: MenuItem? = null
)
