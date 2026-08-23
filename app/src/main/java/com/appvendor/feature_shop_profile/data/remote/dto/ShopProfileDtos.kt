package com.appvendor.feature_shop_profile.data.remote.dto

data class AddressDto(
    val street: String? = null,
    val city: String? = null,
    val state: String? = null,
    val pincode: String? = null,
    val country: String? = null
)

data class BusinessHourDto(
    val dayOfWeek: String? = null,
    val openTime: String? = null,
    val closeTime: String? = null,
    val closed: Boolean? = null
)

data class VendorResponseDto(
    val id: String? = null,
    val vendorCode: String? = null,
    val shopName: String? = null,
    val ownerName: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val logo: String? = null,
    val banner: String? = null,
    val address: AddressDto? = null,
    val businessHours: List<BusinessHourDto>? = null,
    val shopStatus: String = "CLOSED"
)

data class UpdateVendorRequestDto(
    val shopName: String,
    val ownerName: String,
    val phone: String,
    val address: AddressDto,
    val businessHours: List<BusinessHourDto>
)

data class ShopStatusRequestDto(
    val status: String
)

data class SocialLinksDto(
    val instagram: String? = null,
    val facebook: String? = null,
    val website: String? = null
)

data class ProfileResponseDto(
    val id: String? = null,
    val shopName: String? = null,
    val description: String? = null,
    val socialLinks: SocialLinksDto? = null,
    val logoUrl: String? = null,
    val bannerUrl: String? = null
)

data class UpdateProfileRequestDto(
    val shopName: String,
    val description: String?,
    val socialLinks: SocialLinksDto,
    val logo: String? = null,
    val banner: String? = null
)

data class PrinterConfigDto(
    val enabled: Boolean? = null,
    val printerType: String? = null,
    val printerName: String? = null,
    val paperWidth: String? = null,
    val networkIp: String? = null,
    val networkPort: Int? = null,
    val autoPrintOnReady: Boolean? = null,
    val autoPrintOnComplete: Boolean? = null,
    val printerSettings: PrinterConfigDto? = null // Handle nested payload
)
