package com.appvendor.feature_settings.domain.model

data class SettingsData(
    val allowCustomOrder: Boolean = false,
    val autoAcceptOrders: Boolean = false,
    val maxQueueSize: Int = 10,
    val estimatedPrepTime: Int = 5,
    val enableGeofence: Boolean = false,
    val orderPrefix: String = "Q",
    val currency: String = "INR",
    val taxPercentage: Double = 0.0,
    val taxName: String = "Tax",
    val gstNumber: String? = null,
    val showPreparationTime: Boolean = true,
    val additionalCharges: Double = 0.0,
    val additionalChargeName: String = "Service Charge",
    val platformFeePercentage: Double = 5.0,
    val cashfreeFeePercentage: Double = 2.0,
    val cashfreeTaxPercentage: Double = 18.0,
    val enableOnlinePayment: Boolean = false,
    val upiId: String? = null,
    val bankAccountName: String? = null,
    val bankAccountNumber: String? = null,
    val bankIfscCode: String? = null,
    val printerSettings: com.appvendor.feature_shop_profile.domain.model.PrinterConfig? = null
)
