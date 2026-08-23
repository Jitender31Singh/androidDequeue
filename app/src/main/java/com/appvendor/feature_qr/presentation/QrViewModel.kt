package com.appvendor.feature_qr.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appvendor.core.datastore.UserPreferences
import com.appvendor.feature_qr.domain.repository.QrRepository
import com.appvendor.feature_qr.domain.usecase.GenerateQrUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.appvendor.core.network.ApiResult

@HiltViewModel
class QrViewModel @Inject constructor(
    private val generateQrUseCase: GenerateQrUseCase,
    private val qrRepository: QrRepository,
    private val shopProfileRepository: com.appvendor.feature_shop_profile.data.repository.ShopProfileRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _state = MutableStateFlow(QrState())
    val state: StateFlow<QrState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            userPreferences.userShopName.collectLatest { name ->
                _state.update { it.copy(shopName = name) }
            }
        }
        
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val profileResult = shopProfileRepository.getVendorDetails()
            if (profileResult is ApiResult.Success) {
                val vendorCode = profileResult.data.vendorCode
                if (vendorCode.isNotBlank()) {
                    generateQr(vendorCode)
                } else {
                    _state.update { it.copy(isLoading = false, error = "Vendor Code is empty. Please contact support.") }
                }
            } else if (profileResult is ApiResult.Error) {
                _state.update { it.copy(isLoading = false, error = "Failed to fetch Vendor details: ${profileResult.message}") }
            }
        }
    }

    private fun generateQr(vendorCode: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val result = generateQrUseCase(vendorCode)
            result.onSuccess { data ->
                _state.update { it.copy(isLoading = false, qrData = data) }
            }.onFailure { exception ->
                _state.update { it.copy(isLoading = false, error = exception.message ?: "Failed to generate QR code") }
            }
        }
    }

    fun saveQrToFile() {
        val currentData = _state.value.qrData ?: return
        viewModelScope.launch {
            val fileName = "vendor_qr_${currentData.vendorId}"
            val result = qrRepository.saveQrCodeToFile(currentData.bitmap, fileName)
            result.onSuccess { path ->
                _state.update { it.copy(savedFilePath = path) }
            }.onFailure { exception ->
                _state.update { it.copy(error = exception.message ?: "Failed to save QR code") }
            }
        }
    }
    
    fun clearMessages() {
        _state.update { it.copy(error = null, savedFilePath = null) }
    }
}
