package com.appvendor.feature_shop_profile.presentation

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appvendor.core.network.ApiResult
import com.appvendor.feature_shop_profile.data.repository.ShopProfileRepository
import com.appvendor.feature_shop_profile.domain.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.appvendor.core.datastore.UserPreferences
import javax.inject.Inject

@HiltViewModel
class ShopProfileViewModel @Inject constructor(
    private val repository: ShopProfileRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _state = MutableStateFlow(ShopProfileState())
    val state: StateFlow<ShopProfileState> = _state.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            
            var vdError: String? = null
            var profError: String? = null
            
            when (val result = repository.getVendorDetails()) {
                is ApiResult.Success -> {
                    _state.update { it.copy(vendorDetails = result.data) }
                    userPreferences.setLogoUrl(result.data.logo)
                }
                is ApiResult.Error -> vdError = result.message
                is ApiResult.Loading -> {}
            }
            
            when (val result = repository.getProfile()) {
                is ApiResult.Success -> {
                    _state.update { it.copy(profile = result.data) }
                    userPreferences.setLogoUrl(result.data.logoUrl) // Overwrites with profile logo if available
                }
                is ApiResult.Error -> profError = result.message
                is ApiResult.Loading -> {}
            }
            
            _state.update { it.copy(
                isLoading = false, 
                error = vdError ?: profError
            )}
        }
    }

    fun updateStatus(status: String) {
        viewModelScope.launch {
            _state.update { it.copy(isUpdating = true) }
            when (val result = repository.updateShopStatus(status)) {
                is ApiResult.Success -> {
                    val newVd = _state.value.vendorDetails?.copy(shopStatus = status)
                    _state.update { it.copy(isUpdating = false, vendorDetails = newVd) }
                }
                is ApiResult.Error -> _state.update { it.copy(isUpdating = false, error = result.message) }
                is ApiResult.Loading -> {}
            }
        }
    }

    fun updateVendorDetails(
        shopName: String, ownerName: String, phone: String, 
        address: Address, businessHours: List<BusinessHour>
    ) {
        viewModelScope.launch {
            _state.update { it.copy(isUpdating = true) }
            when (val result = repository.updateVendorDetails(shopName, ownerName, phone, address, businessHours)) {
                is ApiResult.Success -> _state.update { it.copy(isUpdating = false, vendorDetails = result.data) }
                is ApiResult.Error -> _state.update { it.copy(isUpdating = false, error = result.message) }
                is ApiResult.Loading -> {}
            }
        }
    }

    fun updateProfile(shopName: String, description: String?, socialLinks: SocialLinks) {
        viewModelScope.launch {
            _state.update { it.copy(isUpdating = true) }
            val currentProfile = _state.value.profile
            when (val result = repository.updateProfile(
                shopName = shopName, 
                description = description, 
                socialLinks = socialLinks,
                logoUrl = currentProfile?.logoUrl,
                bannerUrl = currentProfile?.bannerUrl
            )) {
                is ApiResult.Success -> _state.update { it.copy(isUpdating = false, profile = result.data) }
                is ApiResult.Error -> _state.update { it.copy(isUpdating = false, error = result.message) }
                is ApiResult.Loading -> {}
            }
        }
    }

    fun uploadImage(uri: Uri, isLogo: Boolean) {
        viewModelScope.launch {
            val currentProfile = _state.value.profile
            
            _state.update { it.copy(isUpdating = true) }
            when (val result = repository.uploadImage(
                uri = uri, 
                isLogo = isLogo,
                currentShopName = currentProfile?.shopName ?: "",
                currentDescription = currentProfile?.description,
                currentSocialLinks = currentProfile?.socialLinks ?: SocialLinks(null, null, null),
                currentLogo = currentProfile?.logoUrl,
                currentBanner = currentProfile?.bannerUrl
            )) {
                is ApiResult.Success -> {
                    _state.update { it.copy(isUpdating = false, profile = result.data) }
                    if (isLogo) {
                        userPreferences.setLogoUrl(result.data.logoUrl)
                    }
                }
                is ApiResult.Error -> _state.update { it.copy(isUpdating = false, error = result.message) }
                is ApiResult.Loading -> {}
            }
        }
    }

    fun dismissError() = _state.update { it.copy(error = null) }
}
