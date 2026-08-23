package com.appvendor.feature_departments.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appvendor.core.network.ApiResult
import com.appvendor.feature_departments.data.repository.DepartmentRepository
import com.appvendor.feature_departments.domain.model.Department
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DepartmentsViewModel @Inject constructor(
    private val repository: DepartmentRepository
) : ViewModel() {

    private val _state = MutableStateFlow(DepartmentsState())
    val state: StateFlow<DepartmentsState> = _state.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = it.departments.isEmpty(), error = null) }
            fetchDepartments(isLoading = true)
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isRefreshing = true, error = null) }
            fetchDepartments(isLoading = false)
        }
    }

    private suspend fun fetchDepartments(isLoading: Boolean) {
        when (val result = repository.getDepartments()) {
            is ApiResult.Success -> {
                _state.update { it.copy(
                    isLoading = false,
                    isRefreshing = false,
                    departments = result.data.sortedBy { dept -> dept.name }
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

    fun saveDepartment(id: String?, name: String, description: String?) {
        viewModelScope.launch {
            _state.update { it.copy(isUpdating = true) }
            val result = if (id.isNullOrBlank()) {
                repository.createDepartment(name, description)
            } else {
                repository.updateDepartment(id, name, description)
            }
            
            when (result) {
                is ApiResult.Success -> {
                    _state.update { it.copy(isUpdating = false, isFormOpen = false, editingDepartment = null) }
                    refresh()
                }
                is ApiResult.Error -> _state.update { it.copy(isUpdating = false, error = result.message) }
                is ApiResult.Loading -> {}
            }
        }
    }

    fun deleteDepartment(id: String) {
        viewModelScope.launch {
            _state.update { it.copy(isUpdating = true) }
            val result = repository.deleteDepartment(id)
            if (result.isSuccess) {
                _state.update { it.copy(isUpdating = false) }
                refresh()
            } else {
                _state.update { it.copy(isUpdating = false, error = result.exceptionOrNull()?.message ?: "Delete failed") }
            }
        }
    }

    fun openCreateForm() = _state.update { it.copy(isFormOpen = true, editingDepartment = null) }
    fun openEditForm(dept: Department) = _state.update { it.copy(isFormOpen = true, editingDepartment = dept) }
    fun closeForm() = _state.update { it.copy(isFormOpen = false, editingDepartment = null) }
    fun dismissError() = _state.update { it.copy(error = null) }
}
