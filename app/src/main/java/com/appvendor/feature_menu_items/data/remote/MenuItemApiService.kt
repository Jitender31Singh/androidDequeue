package com.appvendor.feature_menu_items.data.remote

import com.appvendor.core.network.ApiResponse
import com.appvendor.feature_menu_items.data.remote.dto.*
import retrofit2.http.*

interface MenuItemApiService {

    @GET("api/v1/menu/items")
    suspend fun getMenuItems(
        @Query("categoryId") categoryId: String? = null,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 100 // Get a large page for UI simplicity
    ): ApiResponse<PageResponseDto<MenuItemDto>>

    @POST("api/v1/menu/items")
    suspend fun createMenuItem(@Body request: CreateMenuItemRequest): ApiResponse<MenuItemDto>

    @PUT("api/v1/menu/items/{id}")
    suspend fun updateMenuItem(
        @Path("id") id: String,
        @Body request: UpdateMenuItemRequest
    ): ApiResponse<MenuItemDto>

    @PATCH("api/v1/menu/items/{id}/availability")
    suspend fun toggleAvailability(
        @Path("id") id: String,
        @Body request: AvailabilityRequest
    ): ApiResponse<MenuItemDto>

    @PATCH("api/v1/menu/items/{id}/visibility")
    suspend fun toggleVisibility(
        @Path("id") id: String,
        @Body request: VisibilityRequest
    ): ApiResponse<MenuItemDto>

    @DELETE("api/v1/menu/items/{id}")
    suspend fun deleteMenuItem(@Path("id") id: String): retrofit2.Response<Unit>

    @PUT("api/v1/menu/items/sort")
    suspend fun updateSortOrder(@Body request: SortOrderRequest): retrofit2.Response<Unit>

    @Multipart
    @POST("api/v1/menu/items/upload-image")
    suspend fun uploadImage(@Part file: okhttp3.MultipartBody.Part): ApiResponse<UploadResponseDto>

    @Multipart
    @POST("api/v1/menu/extract-from-image")
    suspend fun extractMenuFromImage(@Part image: okhttp3.MultipartBody.Part): ApiResponse<ExtractionResultDto>
}
