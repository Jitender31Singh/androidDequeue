package com.appvendor.feature_settings.data.remote

import com.appvendor.core.network.ApiResponse
import com.appvendor.feature_settings.data.remote.dto.VendorSettingsDto
import com.appvendor.feature_settings.data.remote.dto.PublicVendorDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path

interface SettingsApiService {
    @GET("api/v1/vendors/me/settings")
    suspend fun getSettings(): ApiResponse<VendorSettingsDto>

    @GET("api/v1/public/vendors/{vendorCode}")
    suspend fun getPublicVendor(@Path("vendorCode") vendorCode: String): ApiResponse<PublicVendorDto>

    @PATCH("api/v1/vendors/me/settings")
    suspend fun updateSettings(@Body request: VendorSettingsDto): ApiResponse<VendorSettingsDto>
}
