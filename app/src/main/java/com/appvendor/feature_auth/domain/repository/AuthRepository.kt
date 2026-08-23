package com.appvendor.feature_auth.domain.repository

import com.appvendor.core.network.ApiResult
import com.appvendor.feature_auth.domain.model.AuthResult

/**
 * Repository interface for authentication operations.
 * Matches the actual backend: /api/v1/auth/register, /api/v1/auth/login, /api/v1/auth/refresh
 */
interface AuthRepository {
    suspend fun register(name: String, shopName: String, phone: String, email: String, password: String): ApiResult<AuthResult>
    suspend fun login(email: String, password: String): ApiResult<AuthResult>
    suspend fun refreshToken(refreshToken: String): ApiResult<AuthResult>
    suspend fun fetchPermissions(): ApiResult<com.appvendor.feature_auth.data.remote.dto.PermissionsResponseData>
}
