package com.appvendor.feature_staff.data.remote.dto

data class StaffDto(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String? = null,
    val departmentId: String? = null,
    val departmentName: String? = null,
    val role: String = "KITCHEN_STAFF",
    val permissions: List<String>? = emptyList(),
    val status: String = "ACTIVE",
    val avatar: String? = null,
    val lastLoginAt: String? = null
)

data class CreateStaffRequest(
    val name: String,
    val email: String,
    val password: String?,
    val phone: String?,
    val departmentId: String?,
    val role: String,
    val permissions: List<String>
)

data class UpdateStaffRequest(
    val name: String?,
    val phone: String?,
    val departmentId: String?,
    val role: String?,
    val permissions: List<String>?
)

data class StaffStatusRequest(
    val status: String
)

data class PageResponseDto<T>(
    val content: List<T> = emptyList(),
    val totalElements: Int = 0,
    val totalPages: Int = 0,
    val page: Int = 0,
    val size: Int = 0,
    val last: Boolean = true
)
