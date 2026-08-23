package com.appvendor.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appvendor.core.datastore.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.appvendor.core.network.safeApiCall
import com.appvendor.core.network.ApiResult
import com.appvendor.feature_dashboard.data.remote.VendorApiService
import com.appvendor.feature_dashboard.data.remote.UpdateShopStatusRequest

@HiltViewModel
class MainViewModel @Inject constructor(
    private val userPreferences: UserPreferences,
    private val vendorApi: VendorApiService
) : ViewModel() {

    private val _isBusinessActive = MutableStateFlow(false)

    val state: StateFlow<MainState> = combine(
        userPreferences.userShopName,
        userPreferences.userName,
        _isBusinessActive,
        userPreferences.userEmail,
        userPreferences.logoUrl,
        userPreferences.userPermissions,
        userPreferences.userRoles
    ) { args: Array<Any?> ->
        @Suppress("UNCHECKED_CAST")
        MainState(
            shopName = args[0] as String?,
            userName = args[1] as String?,
            isBusinessActive = args[2] as Boolean,
            userEmail = args[3] as String?,
            logoUrl = args[4] as String?,
            userPermissions = args[5] as Set<String>,
            userRoles = args[6] as Set<String>,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MainState(isLoading = true)
    )

    fun toggleBusinessActive() {
        val newStatus = !_isBusinessActive.value
        val statusString = if (newStatus) "OPEN" else "CLOSED"
        
        viewModelScope.launch {
            // Optimistically update UI
            _isBusinessActive.value = newStatus
            
            val result = safeApiCall { vendorApi.updateStatus(UpdateShopStatusRequest(statusString)) }
            if (result !is ApiResult.Success) {
                // Revert on failure
                _isBusinessActive.value = !newStatus
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            userPreferences.clearSession()
        }
    }
}
