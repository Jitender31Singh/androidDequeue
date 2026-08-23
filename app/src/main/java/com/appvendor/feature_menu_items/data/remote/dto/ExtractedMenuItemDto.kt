package com.appvendor.feature_menu_items.data.remote.dto

data class ExtractedMenuItemDto(
    val name: String,
    val price: Double,
    val categoryName: String?
)

data class ExtractionResultDto(
    val detectedCategories: List<String>?,
    val items: List<ExtractedMenuItemDto>,
    val extractionSessionId: String?,
    val totalItems: Int?,
    val summary: String?
)
