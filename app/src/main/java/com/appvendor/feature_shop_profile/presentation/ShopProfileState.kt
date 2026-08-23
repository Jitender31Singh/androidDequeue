package com.appvendor.feature_shop_profile.presentation

import com.appvendor.feature_shop_profile.domain.model.ShopProfile
import com.appvendor.feature_shop_profile.domain.model.VendorDetails

data class ShopProfileState(
    val vendorDetails: VendorDetails? = null,
    val profile: ShopProfile? = null,
    val isLoading: Boolean = false,
    val isUpdating: Boolean = false,
    val error: String? = null
)
