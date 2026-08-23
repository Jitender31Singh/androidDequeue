package com.appvendor.feature_dashboard.data.remote.dto

data class DashboardResponse(
    val shopStatus: String = "CLOSED",
    val currentlyServing: String? = null,
    val queueLength: Int = 0,
    val averageWaitTime: Int = 0,
    val peakHour: String? = null,
    val todayStats: TodayStats = TodayStats(),
    val recentOrders: List<OrderSummary> = emptyList()
)

data class TodayStats(
    val totalOrders: Int = 0,
    val pendingOrders: Int = 0,
    val preparingOrders: Int = 0,
    val readyOrders: Int = 0,
    val collectedOrders: Int = 0,
    val cancelledOrders: Int = 0,
    val totalRevenue: Double = 0.0
)

data class OrderSummary(
    val id: String = "",
    val queueNumber: String = "",
    val totalAmount: Double = 0.0,
    val status: String = "",
    val itemCount: Int = 0,
    val metadata: Map<String, String>? = null,
    val customFields: Map<String, String>? = null,
    val createdAt: String = ""
)
