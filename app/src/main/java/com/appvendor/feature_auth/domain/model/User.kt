package com.appvendor.feature_auth.domain.model

/**
 * Domain model representing an authenticated user.
 */
data class User(
    val id: String,
    val name: String,
    val email: String,
    val shopName: String,
    val phone: String? = null,
    val vendorId: String,
    val roles: List<String> = emptyList(),
    val permissions: List<String> = emptyList(),
    val orderVisibilityStatuses: List<String> = emptyList()
)
