package com.appvendor.feature_reports.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appvendor.core.network.ApiResult
import com.appvendor.feature_reports.data.repository.ReportRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val repository: ReportRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ReportsState())
    val state: StateFlow<ReportsState> = _state.asStateFlow()

    init {
        val today = LocalDate.now()
        val startOfMonth = today.withDayOfMonth(1)
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE
        
        _state.update { it.copy(
            startDate = startOfMonth.format(formatter),
            endDate = today.format(formatter)
        )}
        
        loadData()
    }

    fun loadData() {
        fetchTodayReport(isLoading = true)
        fetchSummaryReport(isLoading = true)
    }

    fun refresh() {
        _state.update { it.copy(isRefreshing = true, error = null) }
        fetchTodayReport(isLoading = false)
        fetchSummaryReport(isLoading = false)
    }

    fun updateDateRange(startDate: String, endDate: String) {
        _state.update { it.copy(startDate = startDate, endDate = endDate) }
        fetchSummaryReport(isLoading = true)
    }

    private fun fetchTodayReport(isLoading: Boolean) {
        viewModelScope.launch {
            if (isLoading) _state.update { it.copy(isLoadingToday = true, error = null) }
            
            when (val result = repository.getTodayReport()) {
                is ApiResult.Success -> _state.update { it.copy(isLoadingToday = false, isRefreshing = false, todayReport = result.data) }
                is ApiResult.Error -> _state.update { it.copy(isLoadingToday = false, isRefreshing = false, error = result.message) }
                is ApiResult.Loading -> {}
            }
        }
    }

    private fun fetchSummaryReport(isLoading: Boolean) {
        viewModelScope.launch {
            if (isLoading) _state.update { it.copy(isLoadingSummary = true, error = null) }
            
            val currentState = _state.value
            when (val result = repository.getSummaryReport(currentState.startDate, currentState.endDate)) {
                is ApiResult.Success -> _state.update { it.copy(isLoadingSummary = false, isRefreshing = false, summaryReport = result.data) }
                is ApiResult.Error -> _state.update { it.copy(isLoadingSummary = false, isRefreshing = false, error = result.message) }
                is ApiResult.Loading -> {}
            }
        }
    }

    fun dismissError() = _state.update { it.copy(error = null) }
}
