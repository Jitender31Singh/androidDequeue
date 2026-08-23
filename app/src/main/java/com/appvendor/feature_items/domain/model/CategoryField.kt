package com.appvendor.feature_items.domain.model

/**
 * Represents a dynamic field for a specific category.
 */
data class CategoryField(
    val id: String,
    val name: String,
    val type: FieldType,
    val isRequired: Boolean,
    val options: List<String>? = null // Used when type is SELECT
)

enum class FieldType {
    TEXT, NUMBER, BOOLEAN, SELECT
}
