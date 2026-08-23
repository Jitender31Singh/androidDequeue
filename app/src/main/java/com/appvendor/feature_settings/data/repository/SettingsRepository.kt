package com.appvendor.feature_settings.data.repository

import com.appvendor.core.network.ApiResult
import com.appvendor.core.network.safeApiCall
import com.appvendor.feature_settings.data.remote.SettingsApiService
import com.appvendor.feature_settings.data.remote.dto.*
import com.appvendor.feature_settings.domain.model.SettingsData
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    private val api: SettingsApiService
) {
    suspend fun getSettings(): ApiResult<SettingsData> {
        return when (val result = safeApiCall { api.getSettings() }) {
            is ApiResult.Success -> {
                val dto = result.data.data
                if (dto != null) ApiResult.Success(dto.toDomain())
                else ApiResult.Error("No data returned")
            }
            is ApiResult.Error -> result
            is ApiResult.Loading -> result
        }
    }

    suspend fun getPublicSettings(vendorCode: String): ApiResult<SettingsData> {
        return when (val result = safeApiCall { api.getPublicVendor(vendorCode) }) {
            is ApiResult.Success -> {
                val dto = result.data.data?.settings
                if (dto != null) ApiResult.Success(dto.toDomain())
                else ApiResult.Error("No settings data returned")
            }
            is ApiResult.Error -> result
            is ApiResult.Loading -> result
        }
    }

    suspend fun updateSettings(settings: SettingsData): ApiResult<SettingsData> {
        return handleUpdateResult(safeApiCall { api.updateSettings(settings.toDto()) })
    }

    private fun handleUpdateResult(result: ApiResult<com.appvendor.core.network.ApiResponse<VendorSettingsDto>>): ApiResult<SettingsData> {
        return when (result) {
            is ApiResult.Success -> {
                val dto = result.data.data
                if (dto != null) ApiResult.Success(dto.toDomain())
                else ApiResult.Error("No data returned")
            }
            is ApiResult.Error -> result
            is ApiResult.Loading -> result
        }
    }
}

// Mappers
fun VendorSettingsDto.toDomain() = SettingsData(
    allowCustomOrder = allowCustomOrder ?: false,
    autoAcceptOrders = autoAcceptOrders ?: false,
    maxQueueSize = maxQueueSize ?: 10,
    estimatedPrepTime = estimatedPrepTime ?: 5,
    enableGeofence = enableGeofence ?: false,
    orderPrefix = orderPrefix ?: "Q",
    currency = currency ?: "INR",
    taxPercentage = taxPercentage ?: 0.0,
    taxName = taxName ?: "Tax",
    gstNumber = gstNumber,
    showPreparationTime = showPreparationTime ?: true,
    additionalCharges = additionalCharges ?: 0.0,
    additionalChargeName = additionalChargeName ?: "Service Charge",
    platformFeePercentage = platformFeePercentage ?: 5.0,
    cashfreeFeePercentage = cashfreeFeePercentage ?: 2.0,
    cashfreeTaxPercentage = cashfreeTaxPercentage ?: 18.0,
    enableOnlinePayment = enableOnlinePayment ?: false,
    upiId = upiId,
    bankAccountName = bankAccountName,
    bankAccountNumber = bankAccountNumber,
    bankIfscCode = bankIfscCode,
    printerSettings = printerSettings?.let { com.appvendor.feature_shop_profile.domain.model.PrinterConfig(
        enabled = it.enabled ?: false,
        printerType = it.printerType ?: "BROWSER",
        printerName = it.printerName ?: "",
        paperWidth = it.paperWidth ?: "80mm",
        networkIp = it.networkIp ?: "",
        networkPort = it.networkPort ?: 9100,
        autoPrintOnReady = it.autoPrintOnReady ?: false,
        autoPrintOnComplete = it.autoPrintOnComplete ?: false
    )}
)

fun SettingsData.toDto() = VendorSettingsDto(
    allowCustomOrder = allowCustomOrder,
    autoAcceptOrders = autoAcceptOrders,
    maxQueueSize = maxQueueSize,
    estimatedPrepTime = estimatedPrepTime,
    enableGeofence = enableGeofence,
    orderPrefix = orderPrefix,
    currency = currency,
    taxPercentage = taxPercentage,
    taxName = taxName,
    gstNumber = gstNumber,
    showPreparationTime = showPreparationTime,
    additionalCharges = additionalCharges,
    additionalChargeName = additionalChargeName,
    platformFeePercentage = platformFeePercentage,
    cashfreeFeePercentage = cashfreeFeePercentage,
    cashfreeTaxPercentage = cashfreeTaxPercentage,
    enableOnlinePayment = enableOnlinePayment,
    upiId = upiId,
    bankAccountName = bankAccountName,
    bankAccountNumber = bankAccountNumber,
    bankIfscCode = bankIfscCode,
    printerSettings = printerSettings?.let { com.appvendor.feature_shop_profile.data.remote.dto.PrinterConfigDto(
        enabled = it.enabled,
        printerType = it.printerType,
        printerName = it.printerName,
        paperWidth = it.paperWidth,
        networkIp = it.networkIp,
        networkPort = it.networkPort,
        autoPrintOnReady = it.autoPrintOnReady,
        autoPrintOnComplete = it.autoPrintOnComplete
    )}
)
