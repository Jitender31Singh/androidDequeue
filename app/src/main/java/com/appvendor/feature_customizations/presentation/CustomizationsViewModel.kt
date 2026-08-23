package com.appvendor.feature_customizations.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appvendor.core.network.ApiResult
import com.appvendor.feature_customizations.data.repository.CustomizationRepository
import com.appvendor.feature_customizations.domain.model.CustomizationGroup
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CustomizationsViewModel @Inject constructor(
    private val repository: CustomizationRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CustomizationsState())
    val state: StateFlow<CustomizationsState> = _state.asStateFlow()

    init {
        loadCustomizations()
    }

    fun loadCustomizations() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = it.customizations.isEmpty(), error = null) }
            when (val result = repository.getCustomizations()) {
                is ApiResult.Success -> _state.update { it.copy(isLoading = false, customizations = result.data) }
                is ApiResult.Error -> _state.update { it.copy(isLoading = false, error = result.message) }
                is ApiResult.Loading -> {}
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isRefreshing = true, error = null) }
            when (val result = repository.getCustomizations()) {
                is ApiResult.Success -> _state.update { it.copy(isRefreshing = false, customizations = result.data) }
                is ApiResult.Error -> _state.update { it.copy(isRefreshing = false, error = result.message) }
                is ApiResult.Loading -> {}
            }
        }
    }

    fun saveCustomization(group: CustomizationGroup) {
        viewModelScope.launch {
            _state.update { it.copy(isUpdating = true) }
            val isNew = group.id.isBlank()
            val result = if (isNew) {
                repository.createCustomization(group)
            } else {
                repository.updateCustomization(group.id, group)
            }
            
            when (result) {
                is ApiResult.Success -> {
                    _state.update { it.copy(isUpdating = false, isFormOpen = false, editingCustomization = null) }
                    loadCustomizations()
                }
                is ApiResult.Error -> _state.update { it.copy(isUpdating = false, error = result.message) }
                is ApiResult.Loading -> {}
            }
        }
    }

    fun deleteCustomization(id: String) {
        viewModelScope.launch {
            _state.update { it.copy(isUpdating = true) }
            val result = repository.deleteCustomization(id)
            if (result.isSuccess) {
                _state.update { it.copy(isUpdating = false) }
                loadCustomizations()
            } else {
                _state.update { it.copy(isUpdating = false, error = result.exceptionOrNull()?.message ?: "Delete failed") }
            }
        }
    }

    fun openCreateForm() = _state.update { it.copy(isFormOpen = true, editingCustomization = null) }
    fun openEditForm(group: CustomizationGroup) = _state.update { it.copy(isFormOpen = true, editingCustomization = group) }
    fun closeForm() = _state.update { it.copy(isFormOpen = false, editingCustomization = null) }
    
    fun dismissError() = _state.update { it.copy(error = null) }
}
