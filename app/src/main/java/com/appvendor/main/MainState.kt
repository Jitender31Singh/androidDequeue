package com.appvendor.main

data class MainState(
    val shopName: String? = null,
    val userName: String? = null,
    val isBusinessActive: Boolean = false,
    val userEmail: String? = null,
    val logoUrl: String? = null,
    val isLoading: Boolean = false,
    val userPermissions: Set<String> = emptySet(),
    val userRoles: Set<String> = emptySet()
)
