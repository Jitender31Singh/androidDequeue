package com.appvendor.feature_auth.data.remote.dto

import com.appvendor.feature_auth.domain.model.User

// ── Requests ──────────────────────────────────────────────────────────────────

data class RegisterRequest(
    val email: String,
    val password: String,
    val name: String,
    val shopName: String,
    val phone: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class RefreshTokenRequest(
    val refreshToken: String
)

// ── Responses ─────────────────────────────────────────────────────────────────

data class AuthData(
    val accessToken: String? = null,
    val refreshToken: String? = null,
    val user: UserDto? = null
)

data class UserDto(
    val id: String,
    val name: String,
    val email: String,
    val shopName: String,
    val phone: String? = null,
    val vendorId: String,
    val roleIds: List<String>? = null,
    val roleNames: List<String>? = null,
    val effectivePermissions: List<String>? = null,
    val orderVisibilityStatuses: List<String>? = null
)

fun UserDto.toDomain(): User {
    return User(
        id = id,
        name = name,
        email = email,
        shopName = shopName,
        phone = phone,
        vendorId = vendorId,
        roles = roleNames ?: roleIds ?: emptyList(),
        permissions = effectivePermissions ?: emptyList(),
        orderVisibilityStatuses = orderVisibilityStatuses ?: emptyList()
    )
}

data class PermissionsResponseData(
    val userId: String,
    val roles: List<String>?,
    val permissions: List<String>?,
    val orderVisibilityStatuses: List<String>?
)
