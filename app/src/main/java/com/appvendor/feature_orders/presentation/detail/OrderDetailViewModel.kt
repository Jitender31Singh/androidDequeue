package com.appvendor.feature_orders.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appvendor.core.network.ApiResult
import com.appvendor.feature_orders.data.repository.OrderRepository
import com.appvendor.feature_orders.domain.model.OrderStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.appvendor.core.datastore.UserPreferences
import com.appvendor.feature_shop_profile.data.repository.ShopProfileRepository
import com.appvendor.feature_settings.data.repository.SettingsRepository

@HiltViewModel
class OrderDetailViewModel @Inject constructor(
    private val repository: OrderRepository,
    private val userPreferences: UserPreferences,
    private val shopProfileRepository: ShopProfileRepository,
    private val settingsRepository: SettingsRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val orderId: String = checkNotNull(savedStateHandle["orderId"])

    private val _state = MutableStateFlow(OrderDetailState())
    val state: StateFlow<OrderDetailState> = _state.asStateFlow()

    init {
        loadOrder()
        observePermissions()
        loadVendorData()
    }

    private fun loadVendorData() {
        viewModelScope.launch {
            var vendorCode: String? = null
            when (val result = shopProfileRepository.getVendorDetails()) {
                is ApiResult.Success -> {
                    _state.update { it.copy(vendorDetails = result.data) }
                    vendorCode = result.data.vendorCode
                }
                else -> {}
            }
            when (val result = settingsRepository.getSettings()) {
                is ApiResult.Success -> {
                    _state.update { 
                        it.copy(
                            settingsData = result.data,
                            printerConfig = result.data.printerSettings ?: it.printerConfig
                        ) 
                    }
                }
                else -> {
                    if (!vendorCode.isNullOrBlank()) {
                        when (val pubResult = settingsRepository.getPublicSettings(vendorCode)) {
                            is ApiResult.Success -> {
                                _state.update { 
                                    it.copy(
                                        settingsData = pubResult.data,
                                        printerConfig = pubResult.data.printerSettings ?: it.printerConfig
                                    ) 
                                }
                            }
                            else -> {}
                        }
                    }
                }
            }
            when (val result = shopProfileRepository.getPrinterConfig()) {
                is ApiResult.Success -> _state.update { it.copy(printerConfig = result.data) }
                else -> {}
            }
        }
    }

    private fun observePermissions() {
        viewModelScope.launch {
            kotlinx.coroutines.flow.combine(
                userPreferences.userPermissions,
                userPreferences.userRoles,
                userPreferences.userShopName,
                userPreferences.orderVisibilityStatuses
            ) { permissions, roles, shopName, visibilityStatuses ->
                // Custom data class or array is needed since combine only supports up to 5 cleanly with Flow
                listOf(permissions, roles, shopName, visibilityStatuses)
            }.collect { values ->
                val permissions = values[0] as Set<String>
                val roles = values[1] as Set<String>
                val shopName = values[2] as String? ?: "DeQueue Shop"
                val visibilityStatuses = values[3] as Set<String>
                _state.update { 
                    it.copy(
                        userPermissions = permissions, 
                        userRoles = roles, 
                        shopName = shopName,
                        orderVisibilityStatuses = visibilityStatuses
                    ) 
                }
            }
        }
    }

    private fun loadOrder() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            when (val result = repository.getOrderById(orderId)) {
                is ApiResult.Success -> {
                    val order = result.data
                    val visibilityStatuses = _state.value.orderVisibilityStatuses
                    if (visibilityStatuses.isNotEmpty() && !visibilityStatuses.contains(order.status.name)) {
                        _state.update { 
                            it.copy(
                                isLoading = false, 
                                order = null,
                                error = "This order is currently in ${order.status.displayLabel()} state, which is assigned to another role."
                            ) 
                        }
                    } else {
                        _state.update { it.copy(isLoading = false, order = order) }
                    }
                }
                is ApiResult.Error -> _state.update { it.copy(isLoading = false, error = result.message) }
                is ApiResult.Loading -> {}
            }
        }
    }

    fun advanceStatus() {
        val currentOrder = _state.value.order ?: return
        val nextStatus = currentOrder.status.nextStatus() ?: return
        viewModelScope.launch {
            _state.update { it.copy(isUpdating = true) }
            when (val result = repository.updateOrderStatus(orderId, nextStatus)) {
                is ApiResult.Success -> {
                    val config = _state.value.printerConfig
                    val shouldAutoPrint = config?.enabled == true && 
                                          ((nextStatus == OrderStatus.READY && config.autoPrintOnReady) ||
                                           (nextStatus == OrderStatus.COMPLETED && config.autoPrintOnComplete))
                                           
                    _state.update {
                        it.copy(
                            isUpdating = false, 
                            order = result.data, 
                            successMessage = "Order updated to ${nextStatus.displayLabel()}",
                            triggerAutoPrint = shouldAutoPrint
                        )
                    }
                }
                is ApiResult.Error -> _state.update { it.copy(isUpdating = false, error = result.message) }
                is ApiResult.Loading -> {}
            }
        }
    }

    fun clearAutoPrintTrigger() {
        _state.update { it.copy(triggerAutoPrint = false) }
    }

    fun cancelOrder() {
        viewModelScope.launch {
            _state.update { it.copy(isUpdating = true) }
            when (val result = repository.updateOrderStatus(orderId, OrderStatus.CANCELLED)) {
                is ApiResult.Success -> _state.update {
                    it.copy(isUpdating = false, order = result.data, successMessage = "Order cancelled")
                }
                is ApiResult.Error -> _state.update { it.copy(isUpdating = false, error = result.message) }
                is ApiResult.Loading -> {}
            }
        }
    }

    fun dismissMessages() = _state.update { it.copy(error = null, successMessage = null) }

    fun generateBill(order: com.appvendor.feature_orders.domain.model.Order) {
        _state.update { it.copy(successMessage = "Opening print dialog...") }
    }
}
