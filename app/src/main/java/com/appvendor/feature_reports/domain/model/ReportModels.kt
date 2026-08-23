package com.appvendor.feature_reports.domain.model

data class TodayReport(
    val date: String,
    val totalOrders: Int,
    val completedOrders: Int,
    val pendingOrders: Int,
    val cancelledOrders: Int,
    val averagePrepTime: Int,
    val totalRevenue: Double,
    val comparedToYesterday: ComparedStats
)

data class ComparedStats(
    val orders: Double,
    val revenue: Double
)

data class OrderReport(
    val dateRange: String?,
    val totalOrders: Int,
    val totalRevenue: Double,
    val averageOrderValue: Double,
    val byStatus: Map<String, Int>
)

data class PopularItem(
    val menuItemId: String,
    val menuItemName: String,
    val orderCount: Int,
    val totalRevenue: Double
)

data class PeakHour(
    val hour: Int,
    val orderCount: Int,
    val revenue: Double
)

data class QueueStatsReport(
    val averageWaitTime: Int,
    val averagePrepTime: Int,
    val maxQueueLength: Int,
    val totalServed: Int
)

data class SummaryReport(
    val orderReport: OrderReport,
    val popularItemReport: List<PopularItem>,
    val peakHourReport: List<PeakHour>,
    val queueStatsReport: QueueStatsReport
)
