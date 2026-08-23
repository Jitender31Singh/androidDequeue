package com.appvendor.feature_categories.domain.model

data class Category(
    val id: String,
    val name: String,
    val description: String?,
    val image: String?,
    val sortOrder: Int,
    val active: Boolean,
    val itemCount: Int
)
