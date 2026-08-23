package com.appvendor.feature_staff.data.remote

import com.appvendor.core.network.ApiResponse
import com.appvendor.feature_staff.data.remote.dto.CreateStaffRequest
import com.appvendor.feature_staff.data.remote.dto.PageResponseDto
import com.appvendor.feature_staff.data.remote.dto.StaffDto
import com.appvendor.feature_staff.data.remote.dto.StaffStatusRequest
import com.appvendor.feature_staff.data.remote.dto.UpdateStaffRequest
import retrofit2.http.*
import retrofit2.Response

interface StaffApiService {
    @GET("api/v1/staff")
    suspend fun getStaffList(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 100
    ): ApiResponse<PageResponseDto<StaffDto>>

    @POST("api/v1/staff")
    suspend fun createStaff(@Body request: CreateStaffRequest): ApiResponse<StaffDto>

    @PUT("api/v1/staff/{id}")
    suspend fun updateStaff(
        @Path("id") id: String,
        @Body request: UpdateStaffRequest
    ): ApiResponse<StaffDto>

    @PATCH("api/v1/staff/{id}/status")
    suspend fun toggleStaffStatus(
        @Path("id") id: String,
        @Body request: StaffStatusRequest
    ): ApiResponse<StaffDto>

    @DELETE("api/v1/staff/{id}")
    suspend fun deleteStaff(@Path("id") id: String): Response<Unit>
}
