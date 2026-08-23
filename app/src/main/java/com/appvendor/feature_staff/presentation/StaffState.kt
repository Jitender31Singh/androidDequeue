package com.appvendor.feature_staff.presentation

import com.appvendor.feature_staff.domain.model.Staff
import com.appvendor.feature_departments.domain.model.Department

data class StaffState(
    val staffList: List<Staff> = emptyList(),
    val departments: List<Department> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isUpdating: Boolean = false,
    val error: String? = null,
    val isFormOpen: Boolean = false,
    val editingStaff: Staff? = null
)
