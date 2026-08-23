package com.appvendor.feature_shop_profile.domain.model

data class Address(
    val street: String,
    val city: String,
    val state: String,
    val pincode: String,
    val country: String
)

data class BusinessHour(
    val dayOfWeek: String,
    val openTime: String,
    val closeTime: String,
    val closed: Boolean
)

data class VendorDetails(
    val id: String,
    val vendorCode: String,
    val shopName: String,
    val ownerName: String?,
    val email: String?,
    val phone: String?,
    val logo: String?,
    val banner: String?,
    val address: Address?,
    val businessHours: List<BusinessHour>,
    val shopStatus: String
)

data class SocialLinks(
    val instagram: String?,
    val facebook: String?,
    val website: String?
)

data class ShopProfile(
    val id: String,
    val shopName: String,
    val description: String?,
    val socialLinks: SocialLinks,
    val logoUrl: String?,
    val bannerUrl: String?
)

data class PrinterConfig(
    val enabled: Boolean,
    val printerType: String,
    val printerName: String,
    val paperWidth: String,
    val networkIp: String,
    val networkPort: Int,
    val autoPrintOnReady: Boolean,
    val autoPrintOnComplete: Boolean
)
