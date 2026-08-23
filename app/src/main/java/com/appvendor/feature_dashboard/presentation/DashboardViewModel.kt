package com.appvendor.feature_dashboard.presentation

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appvendor.core.datastore.UserPreferences
import com.appvendor.core.network.ApiResult
import com.appvendor.core.network.safeApiCall
import com.appvendor.feature_dashboard.data.remote.DashboardApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.StompClient
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val dashboardApi: DashboardApiService,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    private var stompClient: StompClient? = null

    init {
        loadDashboard()
        setupWebSocket()
        observePreferences()
    }

    private fun observePreferences() {
        viewModelScope.launch {
            userPreferences.userRoles.collect { roles ->
                _state.update { it.copy(userRoles = roles) }
            }
        }
        viewModelScope.launch {
            userPreferences.orderVisibilityStatuses.collect { statuses ->
                _state.update { it.copy(orderVisibilityStatuses = statuses) }
            }
        }
    }

    private fun loadDashboard() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            when (val result = safeApiCall { dashboardApi.getDashboard() }) {
                is ApiResult.Success -> {
                    _state.update { it.copy(isLoading = false, dashboardData = result.data.data) }
                }
                is ApiResult.Error -> {
                    _state.update { it.copy(isLoading = false, error = result.message) }
                }
                is ApiResult.Loading -> {}
            }
        }
    }

    fun refreshOrders() {
        viewModelScope.launch {
            _state.update { it.copy(isRefreshing = true, error = null) }
            when (val result = safeApiCall { dashboardApi.getDashboard() }) {
                is ApiResult.Success -> {
                    _state.update { it.copy(isRefreshing = false, dashboardData = result.data.data) }
                }
                is ApiResult.Error -> {
                    _state.update { it.copy(isRefreshing = false, error = result.message) }
                }
                is ApiResult.Loading -> {}
            }
        }
    }

    @SuppressLint("CheckResult")
    private fun setupWebSocket() {
        viewModelScope.launch {
            val vendorId = userPreferences.userVendorId.firstOrNull() ?: return@launch
            
            stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, "ws://192.168.31.95:8080/ws/websocket")
            stompClient?.connect()

            stompClient?.topic("/topic/vendor/$vendorId")?.subscribe({ _ ->
                // When any order changes, auto-refresh the full dashboard
                refreshOrders()
            }, { error ->
                error.printStackTrace()
            })
        }
    }

    fun dismissError() {
        _state.update { it.copy(error = null) }
    }

    override fun onCleared() {
        super.onCleared()
        stompClient?.disconnect()
    }
}
