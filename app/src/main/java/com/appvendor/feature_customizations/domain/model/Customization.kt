package com.appvendor.feature_customizations.domain.model

enum class SelectionType {
    SINGLE, MULTIPLE
}

data class CustomizationOption(
    val id: String? = null,
    val name: String,
    val additionalPrice: Double
)

data class CustomizationGroup(
    val id: String,
    val name: String,
    val selectionType: SelectionType,
    val required: Boolean,
    val minSelection: Int,
    val maxSelection: Int,
    val options: List<CustomizationOption>
)
