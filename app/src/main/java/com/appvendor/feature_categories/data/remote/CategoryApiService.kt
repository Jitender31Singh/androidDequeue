package com.appvendor.feature_categories.data.remote

import com.appvendor.core.network.ApiResponse
import com.appvendor.feature_categories.data.remote.dto.CategoryDto
import com.appvendor.feature_categories.data.remote.dto.CreateCategoryRequest
import com.appvendor.feature_categories.data.remote.dto.SortOrderRequest
import com.appvendor.feature_categories.data.remote.dto.UpdateCategoryRequest
import retrofit2.http.*

interface CategoryApiService {

    @GET("api/v1/categories")
    suspend fun getCategories(): ApiResponse<List<CategoryDto>>

    @POST("api/v1/categories")
    suspend fun createCategory(@Body request: CreateCategoryRequest): ApiResponse<CategoryDto>

    @PUT("api/v1/categories/{id}")
    suspend fun updateCategory(
        @Path("id") id: String,
        @Body request: UpdateCategoryRequest
    ): ApiResponse<CategoryDto>

    @DELETE("api/v1/categories/{id}")
    suspend fun deleteCategory(@Path("id") id: String): retrofit2.Response<Unit>

    @PUT("api/v1/categories/sort")
    suspend fun updateSortOrder(@Body request: SortOrderRequest): retrofit2.Response<Unit>
}
