package com.appvendor.feature_departments.data.remote

import com.appvendor.core.network.ApiResponse
import com.appvendor.feature_departments.data.remote.dto.CreateDepartmentRequest
import com.appvendor.feature_departments.data.remote.dto.DepartmentDto
import com.appvendor.feature_departments.data.remote.dto.UpdateDepartmentRequest
import retrofit2.http.*
import retrofit2.Response

interface DepartmentApiService {
    @GET("api/v1/departments")
    suspend fun getDepartments(): ApiResponse<List<DepartmentDto>>

    @POST("api/v1/departments")
    suspend fun createDepartment(@Body request: CreateDepartmentRequest): ApiResponse<DepartmentDto>

    @PUT("api/v1/departments/{id}")
    suspend fun updateDepartment(
        @Path("id") id: String,
        @Body request: UpdateDepartmentRequest
    ): ApiResponse<DepartmentDto>

    @DELETE("api/v1/departments/{id}")
    suspend fun deleteDepartment(@Path("id") id: String): Response<Unit>
}
