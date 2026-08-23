package com.appvendor.feature_staff.domain.model

data class Staff(
    val id: String,
    val name: String,
    val email: String,
    val phone: String?,
    val departmentId: String?,
    val departmentName: String?,
    val role: String,
    val permissions: List<String>?,
    val status: String,
    val avatar: String?,
    val lastLoginAt: String?
)
