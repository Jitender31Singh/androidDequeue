package com.appvendor.feature_departments.presentation

import com.appvendor.feature_departments.domain.model.Department

data class DepartmentsState(
    val departments: List<Department> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isUpdating: Boolean = false,
    val error: String? = null,
    val isFormOpen: Boolean = false,
    val editingDepartment: Department? = null
)
