package com.appvendor.feature_reports.data.remote

import com.appvendor.core.network.ApiResponse
import com.appvendor.feature_reports.data.remote.dto.SummaryReportDto
import com.appvendor.feature_reports.data.remote.dto.TodayReportDto
import retrofit2.http.GET
import retrofit2.http.Query

interface ReportApiService {
    @GET("api/v1/reports/today")
    suspend fun getTodayReport(): ApiResponse<TodayReportDto>

    @GET("api/v1/reports/summary")
    suspend fun getSummaryReport(
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String
    ): ApiResponse<SummaryReportDto>
}
