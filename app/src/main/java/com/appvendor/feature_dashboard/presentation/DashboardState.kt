package com.appvendor.feature_dashboard.presentation

import com.appvendor.feature_dashboard.data.remote.dto.DashboardResponse
import com.appvendor.feature_dashboard.domain.model.Order

data class DashboardState(
    val dashboardData: DashboardResponse? = null,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val userRoles: Set<String> = emptySet(),
    val orderVisibilityStatuses: Set<String> = emptySet()
)
