package com.appvendor.feature_auth.data.repository

import com.appvendor.core.datastore.UserPreferences
import com.appvendor.core.network.ApiResult
import com.appvendor.core.network.safeApiCall
import com.appvendor.feature_auth.data.remote.AuthApiService
import com.appvendor.feature_auth.data.remote.dto.LoginRequest
import com.appvendor.feature_auth.data.remote.dto.RefreshTokenRequest
import com.appvendor.feature_auth.data.remote.dto.RegisterRequest
import com.appvendor.core.network.ApiResponse
import com.appvendor.feature_auth.data.remote.dto.AuthData
import com.appvendor.feature_auth.data.remote.dto.toDomain
import com.appvendor.feature_auth.domain.model.AuthResult
import com.appvendor.feature_auth.domain.repository.AuthRepository
import javax.inject.Inject

import kotlinx.coroutines.flow.firstOrNull

class AuthRepositoryImpl @Inject constructor(
    private val apiService: AuthApiService,
    private val userPreferences: UserPreferences
) : AuthRepository {

    override suspend fun register(
        name: String,
        shopName: String,
        phone: String,
        email: String,
        password: String
    ): ApiResult<AuthResult> {
        return safeApiCall {
            val response = apiService.register(
                RegisterRequest(
                    email = email,
                    password = password,
                    name = name,
                    shopName = shopName,
                    phone = phone
                )
            )
            handleAuthResponse(response)
        }
    }

    override suspend fun login(email: String, password: String): ApiResult<AuthResult> {
        return safeApiCall {
            val response = apiService.login(LoginRequest(email, password))
            handleAuthResponse(response)
        }
    }

    override suspend fun refreshToken(refreshToken: String): ApiResult<AuthResult> {
        return safeApiCall {
            val response = apiService.refreshToken(RefreshTokenRequest(refreshToken))
            handleAuthResponse(response)
        }
    }

    override suspend fun fetchPermissions(): ApiResult<com.appvendor.feature_auth.data.remote.dto.PermissionsResponseData> {
        return safeApiCall {
            val response = apiService.getPermissions()
            if (response.success && response.data != null) {
                val data = response.data
                val email = userPreferences.userEmail.firstOrNull() ?: ""
                val name = userPreferences.userName.firstOrNull() ?: ""
                val shopName = userPreferences.userShopName.firstOrNull() ?: ""
                val vendorId = userPreferences.userVendorId.firstOrNull() ?: ""
                val logoUrl = userPreferences.logoUrl.firstOrNull()
                userPreferences.saveUserSession(
                    userId = data.userId,
                    name = name,
                    email = email,
                    shopName = shopName,
                    vendorId = vendorId,
                    logoUrl = logoUrl,
                    roles = data.roles?.toSet() ?: emptySet(),
                    permissions = data.permissions?.toSet() ?: emptySet(),
                    visibilityStatuses = data.orderVisibilityStatuses?.toSet() ?: emptySet()
                )
            }
            response.data ?: throw Exception(response.message ?: "Failed to fetch permissions")
        }
    }

    private suspend fun handleAuthResponse(response: ApiResponse<AuthData>): AuthResult {
        if (response.success && response.data != null) {
            val token = response.data.accessToken
            val userDto = response.data.user
            if (token != null && userDto != null) {
                val roles = userDto.roleNames?.toSet() ?: userDto.roleIds?.toSet() ?: emptySet()
                if (roles.contains("ROLE_VENDOR_ADMIN") || roles.contains("ROLE_VENDOR_MANAGER")) {
                    return AuthResult.Error("Not allowed to login from this app")
                }
                
                userPreferences.saveAuthToken(token)
                userPreferences.saveUserSession(
                    userId = userDto.id,
                    name = userDto.name,
                    email = userDto.email,
                    shopName = userDto.shopName,
                    vendorId = userDto.vendorId,
                    roles = roles,
                    permissions = userDto.effectivePermissions?.toSet() ?: emptySet(),
                    visibilityStatuses = userDto.orderVisibilityStatuses?.toSet() ?: emptySet()
                )
                return AuthResult.Success(userDto.toDomain(), token)
            }
        }
        return AuthResult.Error(response.message ?: "Authentication failed")
    }
}
