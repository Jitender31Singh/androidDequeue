package com.appvendor.feature_orders.presentation.detail

import com.appvendor.feature_orders.domain.model.Order
import com.appvendor.feature_shop_profile.domain.model.VendorDetails
import com.appvendor.feature_settings.domain.model.SettingsData

data class OrderDetailState(
    val order: Order? = null,
    val isLoading: Boolean = false,
    val isUpdating: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val userPermissions: Set<String> = emptySet(),
    val userRoles: Set<String> = emptySet(),
    val shopName: String = "DeQueue Shop",
    val vendorDetails: VendorDetails? = null,
    val settingsData: SettingsData? = null,
    val printerConfig: com.appvendor.feature_shop_profile.domain.model.PrinterConfig? = null,
    val triggerAutoPrint: Boolean = false,
    val orderVisibilityStatuses: Set<String> = emptySet()
)
