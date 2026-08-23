package com.appvendor.feature_staff.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appvendor.core.network.ApiResult
import com.appvendor.feature_staff.data.repository.StaffRepository
import com.appvendor.feature_departments.data.repository.DepartmentRepository
import com.appvendor.feature_staff.domain.model.Staff
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StaffViewModel @Inject constructor(
    private val repository: StaffRepository,
    private val departmentRepository: DepartmentRepository
) : ViewModel() {

    private val _state = MutableStateFlow(StaffState())
    val state: StateFlow<StaffState> = _state.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = it.staffList.isEmpty(), error = null) }
            fetchData(isLoading = true)
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isRefreshing = true, error = null) }
            fetchData(isLoading = false)
        }
    }

    private suspend fun fetchData(isLoading: Boolean) {
        // Fetch departments
        when (val deptResult = departmentRepository.getDepartments()) {
            is ApiResult.Success -> _state.update { it.copy(departments = deptResult.data) }
            else -> {}
        }
        
        // Fetch staff
        when (val result = repository.getStaffList()) {
            is ApiResult.Success -> {
                _state.update { it.copy(
                    isLoading = false,
                    isRefreshing = false,
                    staffList = result.data.sortedBy { staff -> staff.name }
                )}
            }
            is ApiResult.Error -> {
                _state.update { it.copy(
                    isLoading = false,
                    isRefreshing = false,
                    error = result.message
                )}
            }
            is ApiResult.Loading -> {}
        }
    }

    fun saveStaff(
        id: String?,
        name: String,
        email: String,
        password: String?,
        phone: String?,
        departmentId: String?,
        role: String,
        permissions: List<String>
    ) {
        viewModelScope.launch {
            _state.update { it.copy(isUpdating = true) }
            val result = if (id.isNullOrBlank()) {
                repository.createStaff(name, email, password, phone, departmentId, role, permissions)
            } else {
                repository.updateStaff(id, name, phone, departmentId, role, permissions)
            }
            
            when (result) {
                is ApiResult.Success -> {
                    _state.update { it.copy(isUpdating = false, isFormOpen = false, editingStaff = null) }
                    refresh()
                }
                is ApiResult.Error -> _state.update { it.copy(isUpdating = false, error = result.message) }
                is ApiResult.Loading -> {}
            }
        }
    }

    fun toggleStatus(id: String, active: Boolean) {
        viewModelScope.launch {
            val status = if (active) "ACTIVE" else "INACTIVE"
            when (val result = repository.toggleStaffStatus(id, status)) {
                is ApiResult.Success -> refresh()
                is ApiResult.Error -> _state.update { it.copy(error = result.message) }
                is ApiResult.Loading -> {}
            }
        }
    }

    fun deleteStaff(id: String) {
        viewModelScope.launch {
            _state.update { it.copy(isUpdating = true) }
            val result = repository.deleteStaff(id)
            if (result.isSuccess) {
                _state.update { it.copy(isUpdating = false) }
                refresh()
            } else {
                _state.update { it.copy(isUpdating = false, error = result.exceptionOrNull()?.message ?: "Delete failed") }
            }
        }
    }

    fun openCreateForm() = _state.update { it.copy(isFormOpen = true, editingStaff = null) }
    fun openEditForm(staff: Staff) = _state.update { it.copy(isFormOpen = true, editingStaff = staff) }
    fun closeForm() = _state.update { it.copy(isFormOpen = false, editingStaff = null) }
    fun dismissError() = _state.update { it.copy(error = null) }
}
