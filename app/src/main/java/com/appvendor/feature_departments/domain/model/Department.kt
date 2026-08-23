package com.appvendor.feature_departments.domain.model

data class Department(
    val id: String,
    val name: String,
    val description: String?,
    val staffCount: Int,
    val active: Boolean
)
