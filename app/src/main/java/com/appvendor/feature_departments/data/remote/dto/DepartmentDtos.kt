package com.appvendor.feature_departments.data.remote.dto

data class DepartmentDto(
    val id: String = "",
    val name: String = "",
    val description: String? = null,
    val staffCount: Int = 0,
    val active: Boolean = true,
    val createdAt: String? = null
)

data class CreateDepartmentRequest(
    val name: String,
    val description: String?
)

data class UpdateDepartmentRequest(
    val name: String?,
    val description: String?
)
