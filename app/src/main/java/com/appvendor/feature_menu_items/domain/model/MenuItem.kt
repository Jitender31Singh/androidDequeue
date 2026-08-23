package com.appvendor.feature_menu_items.domain.model

import com.appvendor.feature_customizations.domain.model.CustomizationGroup

data class MenuItem(
    val id: String,
    val name: String,
    val description: String?,
    val price: Double,
    val image: String?,
    val available: Boolean,
    val visible: Boolean,
    val categoryId: String,
    val categoryName: String?,
    val preparationTime: Int,
    val sortOrder: Int,
    val customizationGroups: List<CustomizationGroup>
)
