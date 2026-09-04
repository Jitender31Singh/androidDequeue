package com.appvendor.feature_auth.data.remote

import com.appvendor.core.network.ApiResponse
import com.appvendor.feature_auth.data.remote.dto.*
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {

    @POST("api/v1/auth/register")
    suspend fun register(@Body request: RegisterRequest): ApiResponse<AuthData>

    @POST("api/v1/auth/login")
    suspend fun login(@Body request: LoginRequest): ApiResponse<AuthData>

    @POST("api/v1/auth/refresh")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): ApiResponse<AuthData>

    @retrofit2.http.GET("api/v1/auth/me/permissions")
    suspend fun getPermissions(): ApiResponse<PermissionsResponseData>

    @POST("api/v1/devices/register")
    suspend fun registerDevice(@Body request: DeviceRegistrationRequest): retrofit2.Response<Unit>

    @retrofit2.http.DELETE("api/v1/devices/unregister/{deviceId}")
    suspend fun unregisterDevice(@retrofit2.http.Path("deviceId") deviceId: String): retrofit2.Response<Unit>
}
