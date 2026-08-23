package com.appvendor.feature_customizations.presentation

import com.appvendor.feature_customizations.domain.model.CustomizationGroup

data class CustomizationsState(
    val customizations: List<CustomizationGroup> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isUpdating: Boolean = false,
    val error: String? = null,
    
    // Dialog state
    val isFormOpen: Boolean = false,
    val editingCustomization: CustomizationGroup? = null
)
