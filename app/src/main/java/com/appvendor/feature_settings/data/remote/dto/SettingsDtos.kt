package com.appvendor.feature_settings.data.remote.dto

data class VendorSettingsDto(
    val allowCustomOrder: Boolean? = null,
    val autoAcceptOrders: Boolean? = null,
    val maxQueueSize: Int? = null,
    val estimatedPrepTime: Int? = null,
    val enableGeofence: Boolean? = null,
    val orderPrefix: String? = null,
    val currency: String? = null,
    val taxPercentage: Double? = null,
    val taxName: String? = null,
    val gstNumber: String? = null,
    val showPreparationTime: Boolean? = null,
    val additionalCharges: Double? = null,
    val additionalChargeName: String? = null,
    val platformFeePercentage: Double? = null,
    val cashfreeFeePercentage: Double? = null,
    val cashfreeTaxPercentage: Double? = null,
    val enableOnlinePayment: Boolean? = null,
    val upiId: String? = null,
    val bankAccountName: String? = null,
    val bankAccountNumber: String? = null,
    val bankIfscCode: String? = null,
    val printerSettings: com.appvendor.feature_shop_profile.data.remote.dto.PrinterConfigDto? = null
)

data class PublicVendorDto(
    val settings: VendorSettingsDto? = null
)
