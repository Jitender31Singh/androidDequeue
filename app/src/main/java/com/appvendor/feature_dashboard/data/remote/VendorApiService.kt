package com.appvendor.feature_dashboard.data.remote

import com.appvendor.core.network.ApiResponse
import retrofit2.http.Body
import retrofit2.http.PATCH

data class UpdateShopStatusRequest(val status: String)

interface VendorApiService {
    @PATCH("api/v1/vendors/me/status")
    suspend fun updateStatus(@Body request: UpdateShopStatusRequest): ApiResponse<Any>
}
