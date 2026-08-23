package com.appvendor.feature_settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appvendor.core.network.ApiResult
import com.appvendor.feature_settings.data.repository.SettingsRepository
import com.appvendor.feature_settings.domain.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: SettingsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    init {
        loadSettings()
    }

    fun loadSettings() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            when (val result = repository.getSettings()) {
                is ApiResult.Success -> _state.update { it.copy(isLoading = false, settings = result.data) }
                is ApiResult.Error -> _state.update { it.copy(isLoading = false, error = result.message) }
                is ApiResult.Loading -> {}
            }
        }
    }

    fun updateSettings(updated: SettingsData) {
        viewModelScope.launch {
            _state.update { it.copy(isUpdating = true, error = null) }
            when (val result = repository.updateSettings(updated)) {
                is ApiResult.Success -> _state.update { it.copy(isUpdating = false, settings = result.data) }
                is ApiResult.Error -> _state.update { it.copy(isUpdating = false, error = result.message) }
                is ApiResult.Loading -> {}
            }
        }
    }

    fun dismissError() = _state.update { it.copy(error = null) }
}
