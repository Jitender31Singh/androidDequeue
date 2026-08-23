package com.appvendor.feature_dashboard.data.remote

import com.appvendor.core.network.ApiResponse
import com.appvendor.feature_dashboard.data.remote.dto.DashboardResponse
import com.appvendor.feature_dashboard.data.remote.dto.OrderSummary
import com.appvendor.feature_dashboard.data.remote.dto.TodayStats
import retrofit2.http.GET

interface DashboardApiService {
    @GET("api/v1/dashboard")
    suspend fun getDashboard(): ApiResponse<DashboardResponse>

    @GET("api/v1/dashboard/stats")
    suspend fun getDashboardStats(): ApiResponse<TodayStats>

    @GET("api/v1/dashboard/recent-orders")
    suspend fun getRecentOrders(): ApiResponse<List<OrderSummary>>
}
