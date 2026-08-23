package com.appvendor.feature_reports.data.remote.dto

data class TodayReportDto(
    val date: String = "",
    val totalOrders: Int = 0,
    val completedOrders: Int = 0,
    val pendingOrders: Int = 0,
    val cancelledOrders: Int = 0,
    val averagePrepTime: Int = 0,
    val totalRevenue: Double = 0.0,
    val comparedToYesterday: ComparedStatsDto? = null
)

data class ComparedStatsDto(
    val orders: Double = 0.0,
    val revenue: Double = 0.0
)

data class OrderReportDto(
    val dateRange: String? = null,
    val totalOrders: Int = 0,
    val totalRevenue: Double = 0.0,
    val averageOrderValue: Double = 0.0,
    val byStatus: Map<String, Int> = emptyMap()
)

data class PopularItemReportDto(
    val items: List<PopularItemDto> = emptyList()
)

data class PopularItemDto(
    val menuItemId: String = "",
    val menuItemName: String = "",
    val orderCount: Int = 0,
    val totalRevenue: Double = 0.0
)

data class PeakHourReportDto(
    val hours: List<PeakHourDto> = emptyList()
)

data class PeakHourDto(
    val hour: Int = 0,
    val orderCount: Int = 0,
    val revenue: Double = 0.0
)

data class QueueStatsReportDto(
    val averageWaitTime: Int = 0,
    val averagePrepTime: Int = 0,
    val maxQueueLength: Int = 0,
    val totalServed: Int = 0
)

data class SummaryReportDto(
    val orderReport: OrderReportDto?,
    val popularItemReport: PopularItemReportDto?,
    val peakHourReport: PeakHourReportDto?,
    val queueStatsReport: QueueStatsReportDto?
)
