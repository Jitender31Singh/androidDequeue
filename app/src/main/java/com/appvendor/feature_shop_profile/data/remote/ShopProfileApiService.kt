package com.appvendor.feature_shop_profile.data.remote

import com.appvendor.core.network.ApiResponse
import com.appvendor.feature_shop_profile.data.remote.dto.*
import okhttp3.MultipartBody
import retrofit2.http.*

interface ShopProfileApiService {
    @GET("api/v1/vendors/me")
    suspend fun getVendorDetails(): ApiResponse<VendorResponseDto>

    @PUT("api/v1/vendors/me")
    suspend fun updateVendorDetails(@Body request: UpdateVendorRequestDto): ApiResponse<VendorResponseDto>

    @PATCH("api/v1/vendors/me/status")
    suspend fun updateShopStatus(@Body request: ShopStatusRequestDto): ApiResponse<String>

    @GET("api/v1/profile")
    suspend fun getProfile(): ApiResponse<ProfileResponseDto>

    @PUT("api/v1/profile")
    suspend fun updateProfile(@Body request: UpdateProfileRequestDto): ApiResponse<ProfileResponseDto>

    @Multipart
    @PATCH("api/v1/profile/logo")
    suspend fun uploadLogo(@Part file: MultipartBody.Part): ApiResponse<ProfileResponseDto>

    @Multipart
    @PATCH("api/v1/profile/banner")
    suspend fun uploadBanner(@Part file: MultipartBody.Part): ApiResponse<ProfileResponseDto>

    @GET("api/v1/vendors/me/printer")
    suspend fun getPrinterConfig(): ApiResponse<PrinterConfigDto>

    @PATCH("api/v1/vendors/me/printer")
    suspend fun updatePrinterConfig(@Body request: PrinterConfigDto): ApiResponse<PrinterConfigDto>
}
