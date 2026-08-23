package com.appvendor.feature_reports.presentation

import com.appvendor.feature_reports.domain.model.SummaryReport
import com.appvendor.feature_reports.domain.model.TodayReport

data class ReportsState(
    val todayReport: TodayReport? = null,
    val summaryReport: SummaryReport? = null,
    val startDate: String = "", // YYYY-MM-DD
    val endDate: String = "",   // YYYY-MM-DD
    val isLoadingToday: Boolean = false,
    val isLoadingSummary: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null
)
