package com.appvendor.feature_shop_profile.data.repository

import android.content.Context
import android.net.Uri
import com.appvendor.core.network.ApiResult
import com.appvendor.core.network.safeApiCall
import com.appvendor.feature_shop_profile.data.remote.ShopProfileApiService
import com.appvendor.feature_shop_profile.data.remote.dto.*
import com.appvendor.feature_shop_profile.domain.model.*
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShopProfileRepository @Inject constructor(
    private val api: ShopProfileApiService,
    private val cloudinaryApi: com.appvendor.core.network.CloudinaryApiService,
    @ApplicationContext private val context: Context
) {
    suspend fun getVendorDetails(): ApiResult<VendorDetails> {
        return when (val result = safeApiCall { api.getVendorDetails() }) {
            is ApiResult.Success -> {
                val dto = result.data.data
                if (dto != null) ApiResult.Success(dto.toDomain())
                else ApiResult.Error("No data returned")
            }
            is ApiResult.Error -> result
            is ApiResult.Loading -> result
        }
    }

    suspend fun updateVendorDetails(
        shopName: String, ownerName: String, phone: String, 
        address: Address, businessHours: List<BusinessHour>
    ): ApiResult<VendorDetails> {
        val request = UpdateVendorRequestDto(
            shopName = shopName,
            ownerName = ownerName,
            phone = phone,
            address = address.toDto(),
            businessHours = businessHours.map { it.toDto() }
        )
        return when (val result = safeApiCall { api.updateVendorDetails(request) }) {
            is ApiResult.Success -> {
                val dto = result.data.data
                if (dto != null) ApiResult.Success(dto.toDomain())
                else ApiResult.Error("No data returned")
            }
            is ApiResult.Error -> result
            is ApiResult.Loading -> result
        }
    }

    suspend fun updateShopStatus(status: String): ApiResult<String> {
        return when (val result = safeApiCall { api.updateShopStatus(ShopStatusRequestDto(status)) }) {
            is ApiResult.Success -> ApiResult.Success(result.data.data ?: status)
            is ApiResult.Error -> result
            is ApiResult.Loading -> result
        }
    }

    suspend fun getProfile(): ApiResult<ShopProfile> {
        return when (val result = safeApiCall { api.getProfile() }) {
            is ApiResult.Success -> {
                val dto = result.data.data
                if (dto != null) ApiResult.Success(dto.toDomain())
                else ApiResult.Error("No data returned")
            }
            is ApiResult.Error -> result
            is ApiResult.Loading -> result
        }
    }

    suspend fun updateProfile(
        shopName: String, 
        description: String?, 
        socialLinks: SocialLinks,
        logoUrl: String? = null,
        bannerUrl: String? = null
    ): ApiResult<ShopProfile> {
        val request = UpdateProfileRequestDto(shopName, description, socialLinks.toDto(), logo = logoUrl, banner = bannerUrl)
        return when (val result = safeApiCall { api.updateProfile(request) }) {
            is ApiResult.Success -> {
                val dto = result.data.data
                if (dto != null) ApiResult.Success(dto.toDomain())
                else ApiResult.Error("No data returned")
            }
            is ApiResult.Error -> result
            is ApiResult.Loading -> result
        }
    }

    suspend fun uploadImage(
        uri: Uri, 
        isLogo: Boolean,
        currentShopName: String,
        currentDescription: String?,
        currentSocialLinks: SocialLinks,
        currentLogo: String?,
        currentBanner: String?
    ): ApiResult<ShopProfile> {
        return try {
            val file = getFileFromUri(uri)
            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
            val filePart = MultipartBody.Part.createFormData("file", file.name, requestFile)
            val presetPart = MultipartBody.Part.createFormData("upload_preset", "dequeue")
            
            // TODO: Replace YOUR_CLOUD_NAME and YOUR_UPLOAD_PRESET with actual Cloudinary details
            val response = cloudinaryApi.uploadImage(
                cloudName = "jcr3kntp",
                file = filePart,
                uploadPreset = presetPart
            )
            
            file.delete() // Clean up temp file
            
            if (response.isSuccessful && response.body() != null) {
                val imageUrl = response.body()!!.secure_url
                
                val updatedLogo = if (isLogo) imageUrl else currentLogo
                val updatedBanner = if (!isLogo) imageUrl else currentBanner
                
                // Since backend uses updateProfile, call it. If backend wants something else, adjust here.
                updateProfile(
                    shopName = currentShopName, 
                    description = currentDescription, 
                    socialLinks = currentSocialLinks,
                    logoUrl = updatedLogo,
                    bannerUrl = updatedBanner
                )
            } else {
                ApiResult.Error("Failed to upload image to Cloudinary: ${response.code()} ${response.message()}")
            }
        } catch (e: Exception) {
            ApiResult.Error("Failed to upload image: ${e.message}")
        }
    }

    suspend fun getPrinterConfig(): ApiResult<PrinterConfig> {
        return when (val result = safeApiCall { api.getPrinterConfig() }) {
            is ApiResult.Success -> {
                val dto = result.data.data
                if (dto != null) ApiResult.Success(dto.toDomain())
                else ApiResult.Error("No data returned")
            }
            is ApiResult.Error -> result
            is ApiResult.Loading -> result
        }
    }

    suspend fun updatePrinterConfig(config: PrinterConfig): ApiResult<PrinterConfig> {
        return when (val result = safeApiCall { api.updatePrinterConfig(config.toDto()) }) {
            is ApiResult.Success -> {
                val dto = result.data.data
                if (dto != null) ApiResult.Success(dto.toDomain())
                else ApiResult.Error("No data returned")
            }
            is ApiResult.Error -> result
            is ApiResult.Loading -> result
        }
    }

    private fun getFileFromUri(uri: Uri): File {
        val inputStream = context.contentResolver.openInputStream(uri)
        val tempFile = File.createTempFile("upload_", ".jpg", context.cacheDir)
        val outputStream = FileOutputStream(tempFile)
        inputStream?.copyTo(outputStream)
        inputStream?.close()
        outputStream.close()
        return tempFile
    }
}

// Mappers
fun AddressDto.toDomain() = Address(street ?: "", city ?: "", state ?: "", pincode ?: "", country ?: "")
fun Address.toDto() = AddressDto(street, city, state, pincode, country)

fun BusinessHourDto.toDomain() = BusinessHour(dayOfWeek ?: "", openTime ?: "", closeTime ?: "", closed ?: false)
fun BusinessHour.toDto() = BusinessHourDto(dayOfWeek, openTime, closeTime, closed)

fun VendorResponseDto.toDomain() = VendorDetails(
    id = id ?: "", 
    vendorCode = vendorCode ?: "", 
    shopName = shopName ?: "", 
    ownerName = ownerName ?: "", 
    email = email ?: "", 
    phone = phone ?: "", 
    logo = logo, 
    banner = banner, 
    address = address?.toDomain(), 
    businessHours = businessHours?.map { it.toDomain() } ?: emptyList(), 
    shopStatus = shopStatus
)

fun SocialLinksDto.toDomain() = SocialLinks(instagram, facebook, website)
fun SocialLinks.toDto() = SocialLinksDto(instagram, facebook, website)

fun ProfileResponseDto.toDomain() = ShopProfile(
    id = id ?: "", 
    shopName = shopName ?: "", 
    description = description, 
    socialLinks = socialLinks?.toDomain() ?: SocialLinks(null, null, null), 
    logoUrl = logoUrl, 
    bannerUrl = bannerUrl
)

fun PrinterConfigDto.toDomain(): PrinterConfig {
    val actual = this.printerSettings ?: this
    return PrinterConfig(
        enabled = actual.enabled ?: false,
        printerType = actual.printerType ?: "BROWSER",
        printerName = actual.printerName ?: "",
        paperWidth = actual.paperWidth ?: "80mm",
        networkIp = actual.networkIp ?: "",
        networkPort = actual.networkPort ?: 9100,
        autoPrintOnReady = actual.autoPrintOnReady ?: false,
        autoPrintOnComplete = actual.autoPrintOnComplete ?: false
    )
}

fun PrinterConfig.toDto(): PrinterConfigDto {
    return PrinterConfigDto(
        enabled = this.enabled,
        printerType = this.printerType,
        printerName = this.printerName,
        paperWidth = this.paperWidth,
        networkIp = this.networkIp,
        networkPort = this.networkPort,
        autoPrintOnReady = this.autoPrintOnReady,
        autoPrintOnComplete = this.autoPrintOnComplete
    )
}
