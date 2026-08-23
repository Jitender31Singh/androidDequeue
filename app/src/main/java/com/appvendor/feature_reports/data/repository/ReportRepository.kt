package com.appvendor.feature_reports.data.repository

import com.appvendor.core.network.ApiResult
import com.appvendor.core.network.safeApiCall
import com.appvendor.feature_reports.data.remote.ReportApiService
import com.appvendor.feature_reports.data.remote.dto.*
import com.appvendor.feature_reports.domain.model.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReportRepository @Inject constructor(
    private val api: ReportApiService
) {
    suspend fun getTodayReport(): ApiResult<TodayReport> {
        return when (val result = safeApiCall { api.getTodayReport() }) {
            is ApiResult.Success -> {
                val dto = result.data.data
                if (dto != null) ApiResult.Success(dto.toDomain())
                else ApiResult.Error("No data returned")
            }
            is ApiResult.Error -> result
            is ApiResult.Loading -> result
        }
    }

    suspend fun getSummaryReport(startDate: String, endDate: String): ApiResult<SummaryReport> {
        return when (val result = safeApiCall { api.getSummaryReport(startDate, endDate) }) {
            is ApiResult.Success -> {
                val dto = result.data.data
                if (dto != null) ApiResult.Success(dto.toDomain())
                else ApiResult.Error("No data returned")
            }
            is ApiResult.Error -> result
            is ApiResult.Loading -> result
        }
    }
}

fun TodayReportDto.toDomain() = TodayReport(
    date = date,
    totalOrders = totalOrders,
    completedOrders = completedOrders,
    pendingOrders = pendingOrders,
    cancelledOrders = cancelledOrders,
    averagePrepTime = averagePrepTime,
    totalRevenue = totalRevenue,
    comparedToYesterday = comparedToYesterday?.toDomain() ?: ComparedStats(0.0, 0.0)
)

fun ComparedStatsDto.toDomain() = ComparedStats(
    orders = orders,
    revenue = revenue
)

fun OrderReportDto.toDomain() = OrderReport(
    dateRange = dateRange,
    totalOrders = totalOrders,
    totalRevenue = totalRevenue,
    averageOrderValue = averageOrderValue,
    byStatus = byStatus
)

fun PopularItemDto.toDomain() = PopularItem(
    menuItemId = menuItemId,
    menuItemName = menuItemName,
    orderCount = orderCount,
    totalRevenue = totalRevenue
)

fun PeakHourDto.toDomain() = PeakHour(
    hour = hour,
    orderCount = orderCount,
    revenue = revenue
)

fun QueueStatsReportDto.toDomain() = QueueStatsReport(
    averageWaitTime = averageWaitTime,
    averagePrepTime = averagePrepTime,
    maxQueueLength = maxQueueLength,
    totalServed = totalServed
)

fun SummaryReportDto.toDomain() = SummaryReport(
    orderReport = orderReport?.toDomain() ?: OrderReport(null, 0, 0.0, 0.0, emptyMap()),
    popularItemReport = popularItemReport?.items?.map { it.toDomain() } ?: emptyList(),
    peakHourReport = peakHourReport?.hours?.map { it.toDomain() } ?: emptyList(),
    queueStatsReport = queueStatsReport?.toDomain() ?: QueueStatsReport(0, 0, 0, 0)
)
