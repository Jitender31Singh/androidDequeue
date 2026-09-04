package com.appvendor.feature_orders.presentation.active

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appvendor.core.datastore.UserPreferences
import com.appvendor.core.network.ApiResult
import com.appvendor.feature_orders.data.repository.OrderRepository
import com.appvendor.feature_orders.domain.model.OrderStatus
import com.appvendor.feature_orders.domain.model.OrderSummary
import dagger.hilt.android.lifecycle.HiltViewModel
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
class ActiveOrdersViewModel @Inject constructor(
    private val repository: OrderRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _state = MutableStateFlow(ActiveOrdersState())
    val state: StateFlow<ActiveOrdersState> = _state.asStateFlow()

    private var stompClient: StompClient? = null

    init {
        loadActiveOrders()
        setupWebSocket()
        observePreferences()
    }

    private fun observePreferences() {
        viewModelScope.launch {
            userPreferences.userPermissions.collect { permissions ->
                _state.update { it.copy(userPermissions = permissions) }
            }
        }
        viewModelScope.launch {
            userPreferences.userRoles.collect { roles ->
                _state.update { it.copy(userRoles = roles) }
            }
        }
        viewModelScope.launch {
            userPreferences.orderVisibilityStatuses.collect { statuses ->
                _state.update { it.copy(orderVisibilityStatuses = statuses) }
                applyFilters()
            }
        }
    }

    fun loadActiveOrders() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = it.orders.isEmpty(), error = null) }
            when (val result = repository.getActiveOrders()) {
                is ApiResult.Success -> {
                    _state.update { it.copy(isLoading = false, orders = result.data) }
                    applyFilters()
                }
                is ApiResult.Error -> _state.update { it.copy(isLoading = false, error = result.message) }
                is ApiResult.Loading -> {}
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isRefreshing = true) }
            when (val result = repository.getActiveOrders()) {
                is ApiResult.Success -> {
                    _state.update { it.copy(isRefreshing = false, orders = result.data) }
                    applyFilters()
                }
                is ApiResult.Error -> _state.update { it.copy(isRefreshing = false, error = result.message) }
                is ApiResult.Loading -> {}
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _state.update { it.copy(searchQuery = query) }
        applyFilters()
    }

    fun updateFilterStatus(status: OrderStatus?) {
        _state.update { it.copy(filterStatus = status) }
        applyFilters()
    }

    private fun isStatusVisible(status: OrderStatus, allowedStatuses: Set<String>): Boolean {
        if (allowedStatuses.isEmpty()) return true
        return allowedStatuses.contains(status.name)
    }

    private fun applyFilters() {
        val currentState = _state.value
        val query = currentState.searchQuery.trim()
        val status = currentState.filterStatus

        val filtered = currentState.orders.filter { order ->
            val matchesQuery = if (query.isNotEmpty()) {
                order.queueNumber.contains(query, ignoreCase = true)
            } else true
            val matchesStatus = if (status != null) {
                order.status == status
            } else true
            val matchesVisibility = isStatusVisible(order.status, currentState.orderVisibilityStatuses)
            matchesQuery && matchesStatus && matchesVisibility
        }

        _state.update { it.copy(filteredOrders = filtered) }
    }

    fun updateStatus(orderId: String, newStatus: OrderStatus) {
        viewModelScope.launch {
            when (val result = repository.updateOrderStatus(orderId, newStatus)) {
                is ApiResult.Success -> {
                    // Refresh to get the latest order list after status change
                    loadActiveOrders()
                }
                is ApiResult.Error -> _state.update { it.copy(error = result.message) }
                is ApiResult.Loading -> {}
            }
        }
    }

    @SuppressLint("CheckResult")
    private fun setupWebSocket() {
        viewModelScope.launch {
            val vendorId = userPreferences.userVendorId.firstOrNull() ?: return@launch
            val token = userPreferences.userToken.firstOrNull() ?: ""
            
            val baseUrl = com.appvendor.core.utils.Constants.BASE_URL
            val wsUrl = baseUrl.replace("http://", "ws://").replace("https://", "wss://") + "ws/websocket"
            
            val httpHeaders = mapOf("Authorization" to "Bearer $token")
            stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, wsUrl, httpHeaders)
            stompClient?.withClientHeartbeat(10000)?.withServerHeartbeat(10000)

            stompClient?.lifecycle()?.subscribe { event ->
                when (event.type) {
                    ua.naiksoftware.stomp.dto.LifecycleEvent.Type.OPENED -> {
                        println("STOMP active orders connection opened")
                    }
                    ua.naiksoftware.stomp.dto.LifecycleEvent.Type.CLOSED -> {
                        println("STOMP active orders connection closed, attempting to reconnect...")
                        Thread.sleep(3000)
                        val reconnectToken = kotlinx.coroutines.runBlocking { userPreferences.userToken.firstOrNull() } ?: ""
                        val headers = listOf(ua.naiksoftware.stomp.dto.StompHeader("Authorization", "Bearer $reconnectToken"))
                        stompClient?.connect(headers)
                    }
                    ua.naiksoftware.stomp.dto.LifecycleEvent.Type.ERROR -> {
                        event.exception?.printStackTrace()
                    }
                    else -> {}
                }
            }

            val headers = listOf(ua.naiksoftware.stomp.dto.StompHeader("Authorization", "Bearer $token"))
            stompClient?.connect(headers)

            stompClient?.topic("/topic/vendor/$vendorId")?.subscribe({
                // Instantly refresh when any order event fires
                loadActiveOrders()
            }, { it.printStackTrace() })
        }
    }

    fun dismissError() = _state.update { it.copy(error = null) }

    override fun onCleared() {
        super.onCleared()
        stompClient?.disconnect()
    }
}
