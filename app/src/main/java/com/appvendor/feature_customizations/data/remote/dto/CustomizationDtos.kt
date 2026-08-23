package com.appvendor.feature_customizations.data.remote.dto

data class CustomizationOptionDto(
    val id: String? = null,
    val name: String,
    val additionalPrice: Double
)

data class CustomizationGroupDto(
    val id: String,
    val name: String,
    val selectionType: String,
    val required: Boolean,
    val minSelection: Int,
    val maxSelection: Int,
    val options: List<CustomizationOptionDto>
)

data class CreateCustomizationGroupRequest(
    val name: String,
    val selectionType: String,
    val required: Boolean,
    val minSelection: Int,
    val maxSelection: Int,
    val options: List<CustomizationOptionDto>
)

data class UpdateCustomizationGroupRequest(
    val name: String,
    val selectionType: String,
    val required: Boolean,
    val minSelection: Int,
    val maxSelection: Int,
    val options: List<CustomizationOptionDto>
)
