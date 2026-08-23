package com.appvendor.feature_dashboard.data.remote

import com.appvendor.feature_dashboard.data.remote.dto.OrderDto
import com.appvendor.feature_dashboard.data.remote.dto.PaginatedResponse
import com.appvendor.feature_dashboard.data.remote.dto.UpdateStatusRequest
import com.appvendor.core.network.ApiResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface OrderApiService {
    @GET("orders/active")
    suspend fun getActiveOrder(@Query("vendorId") vendorId: String): ApiResponse<OrderDto?>
    
    @GET("orders/pending")
    suspend fun getPendingOrders(
        @Query("vendorId") vendorId: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): ApiResponse<PaginatedResponse<OrderDto>>
    
    @PUT("orders/{orderId}/status")
    suspend fun updateOrderStatus(
        @Path("orderId") orderId: String,
        @Body request: UpdateStatusRequest
    ): ApiResponse<OrderDto>
    
    @GET("orders/{orderId}")
    suspend fun getOrderById(@Path("orderId") orderId: String): ApiResponse<OrderDto>
}
