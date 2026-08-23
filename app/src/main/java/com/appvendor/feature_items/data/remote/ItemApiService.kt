package com.appvendor.feature_items.data.remote

import com.appvendor.feature_items.data.remote.dto.CategoryDto
import com.appvendor.feature_items.data.remote.dto.CreateCategoryRequest
import com.appvendor.feature_items.data.remote.dto.CreateItemRequest
import com.appvendor.feature_items.data.remote.dto.ItemDto
import com.appvendor.core.network.ApiResponse
import retrofit2.http.*

interface ItemApiService {
    @GET("api/v1/categories")
    suspend fun getCategories(): ApiResponse<List<CategoryDto>>

    @POST("api/v1/categories")
    suspend fun createCategory(@Body request: CreateCategoryRequest): ApiResponse<CategoryDto>

    @PUT("api/v1/categories/{id}")
    suspend fun updateCategory(@Path("id") id: String, @Body request: CreateCategoryRequest): ApiResponse<CategoryDto>

    @DELETE("api/v1/categories/{id}")
    suspend fun deleteCategory(@Path("id") id: String): ApiResponse<Unit>

    @GET("api/v1/items")
    suspend fun getItems(@Query("categoryId") categoryId: String? = null): ApiResponse<List<ItemDto>>

    @POST("api/v1/items")
    suspend fun createItem(@Body request: CreateItemRequest): ApiResponse<ItemDto>

    @PUT("api/v1/items/{id}")
    suspend fun updateItem(@Path("id") id: String, @Body request: CreateItemRequest): ApiResponse<ItemDto>

    @DELETE("api/v1/items/{id}")
    suspend fun deleteItem(@Path("id") id: String): ApiResponse<Unit>
}
