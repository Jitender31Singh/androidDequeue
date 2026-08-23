package com.appvendor.feature_customizations.data.remote

import com.appvendor.core.network.ApiResponse
import com.appvendor.feature_customizations.data.remote.dto.CreateCustomizationGroupRequest
import com.appvendor.feature_customizations.data.remote.dto.CustomizationGroupDto
import com.appvendor.feature_customizations.data.remote.dto.UpdateCustomizationGroupRequest
import retrofit2.http.*

interface CustomizationApiService {

    @GET("api/v1/customizations")
    suspend fun getCustomizations(): ApiResponse<List<CustomizationGroupDto>>

    @POST("api/v1/customizations")
    suspend fun createCustomization(
        @Body request: CreateCustomizationGroupRequest
    ): ApiResponse<CustomizationGroupDto>

    @PUT("api/v1/customizations/{id}")
    suspend fun updateCustomization(
        @Path("id") id: String,
        @Body request: UpdateCustomizationGroupRequest
    ): ApiResponse<CustomizationGroupDto>

    @DELETE("api/v1/customizations/{id}")
    suspend fun deleteCustomization(@Path("id") id: String): retrofit2.Response<Unit>
}
