package com.appvendor.feature_orders.data.remote

import com.appvendor.core.network.ApiResponse
import com.appvendor.feature_orders.data.remote.dto.OrderResponseDto
import com.appvendor.feature_orders.data.remote.dto.OrderSummaryDto
import com.appvendor.feature_orders.data.remote.dto.PageResponseDto
import com.appvendor.feature_orders.data.remote.dto.UpdateOrderStatusRequest
import retrofit2.http.*

interface OrderApiService {

    // GET /api/v1/orders/active — Live order board (PENDING, ACCEPTED, PREPARING, READY)
    @GET("api/v1/orders/active")
    suspend fun getActiveOrders(): ApiResponse<List<OrderSummaryDto>>

    // GET /api/v1/orders — Paginated history with filters
    @GET("api/v1/orders")
    suspend fun getOrderHistory(
        @Query("status") status: String? = null,
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null,
        @Query("queueNumber") queueNumber: String? = null,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): ApiResponse<PageResponseDto<OrderSummaryDto>>

    // GET /api/v1/orders/{id} — Full order details
    @GET("api/v1/orders/{id}")
    suspend fun getOrderById(@Path("id") id: String): ApiResponse<OrderResponseDto>

    // PATCH /api/v1/orders/{id}/status — Advance order through pipeline
    @PATCH("api/v1/orders/{id}/status")
    suspend fun updateOrderStatus(
        @Path("id") id: String,
        @Body request: UpdateOrderStatusRequest
    ): ApiResponse<OrderResponseDto>
}
