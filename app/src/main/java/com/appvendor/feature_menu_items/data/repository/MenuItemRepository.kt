package com.appvendor.feature_menu_items.data.repository

import com.appvendor.core.network.ApiResult
import com.appvendor.core.network.safeApiCall
import com.appvendor.feature_customizations.data.repository.toDomain
import com.appvendor.feature_menu_items.data.remote.MenuItemApiService
import com.appvendor.feature_menu_items.data.remote.dto.*
import com.appvendor.feature_menu_items.domain.model.MenuItem
import com.appvendor.feature_menu_items.domain.model.ExtractedMenuItem
import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MenuItemRepository @Inject constructor(
    private val api: MenuItemApiService,
    private val cloudinaryApi: com.appvendor.core.network.CloudinaryApiService,
    @ApplicationContext private val context: Context
) {

    suspend fun getMenuItems(categoryId: String? = null): ApiResult<List<MenuItem>> {
        return when (val result = safeApiCall { api.getMenuItems(categoryId = categoryId) }) {
            is ApiResult.Success -> ApiResult.Success(
                result.data.data?.content?.map { it.toDomain() } ?: emptyList()
            )
            is ApiResult.Error -> result
            is ApiResult.Loading -> result
        }
    }

    suspend fun createMenuItem(
        name: String,
        description: String?,
        price: Double,
        categoryId: String,
        preparationTime: Int,
        sortOrder: Int,
        image: String?,
        customizationGroupIds: List<String>
    ): ApiResult<MenuItem> {
        val request = CreateMenuItemRequest(
            name = name,
            description = description,
            price = price,
            categoryId = categoryId,
            preparationTime = preparationTime,
            sortOrder = sortOrder,
            image = image,
            customizationGroupIds = customizationGroupIds
        )
        return when (val result = safeApiCall { api.createMenuItem(request) }) {
            is ApiResult.Success -> {
                val dto = result.data.data
                if (dto != null) ApiResult.Success(dto.toDomain())
                else ApiResult.Error("No data returned")
            }
            is ApiResult.Error -> result
            is ApiResult.Loading -> result
        }
    }

    suspend fun updateMenuItem(
        id: String,
        name: String?,
        description: String?,
        price: Double?,
        categoryId: String?,
        preparationTime: Int?,
        image: String?,
        customizationGroupIds: List<String>?
    ): ApiResult<MenuItem> {
        val request = UpdateMenuItemRequest(
            name = name,
            description = description,
            price = price,
            categoryId = categoryId,
            preparationTime = preparationTime,
            image = image,
            customizationGroupIds = customizationGroupIds
        )
        return when (val result = safeApiCall { api.updateMenuItem(id, request) }) {
            is ApiResult.Success -> {
                val dto = result.data.data
                if (dto != null) ApiResult.Success(dto.toDomain())
                else ApiResult.Error("No data returned")
            }
            is ApiResult.Error -> result
            is ApiResult.Loading -> result
        }
    }

    suspend fun toggleAvailability(id: String, available: Boolean): ApiResult<MenuItem> {
        return when (val result = safeApiCall { api.toggleAvailability(id, AvailabilityRequest(available)) }) {
            is ApiResult.Success -> {
                val dto = result.data.data
                if (dto != null) ApiResult.Success(dto.toDomain())
                else ApiResult.Error("No data returned")
            }
            is ApiResult.Error -> result
            is ApiResult.Loading -> result
        }
    }

    suspend fun toggleVisibility(id: String, visible: Boolean): ApiResult<MenuItem> {
        return when (val result = safeApiCall { api.toggleVisibility(id, VisibilityRequest(visible)) }) {
            is ApiResult.Success -> {
                val dto = result.data.data
                if (dto != null) ApiResult.Success(dto.toDomain())
                else ApiResult.Error("No data returned")
            }
            is ApiResult.Error -> result
            is ApiResult.Loading -> result
        }
    }

    suspend fun deleteMenuItem(id: String): Result<Unit> {
        return try {
            val response = api.deleteMenuItem(id)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to delete menu item: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateSortOrder(items: List<Pair<String, Int>>): Result<Unit> {
        return try {
            val requestItems = items.map { SortOrderItem(it.first, it.second) }
            val response = api.updateSortOrder(SortOrderRequest(requestItems))
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to update sort order: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadImage(uri: Uri): ApiResult<String> {
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
                ApiResult.Success(response.body()!!.secure_url)
            } else {
                ApiResult.Error("Failed to upload image to Cloudinary: ${response.code()} ${response.message()}")
            }
        } catch (e: Exception) {
            ApiResult.Error("Failed to upload image: ${e.message}")
        }
    }

    suspend fun extractMenuFromImage(uri: Uri): ApiResult<List<ExtractedMenuItem>> {
        return try {
            val file = getFileFromUri(uri)
            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("image", file.name, requestFile)
            
            val result = safeApiCall { api.extractMenuFromImage(body) }
            
            file.delete() // Clean up temp file
            
            when (result) {
                is ApiResult.Success -> {
                    val resultDto = result.data.data
                    if (resultDto != null) {
                        ApiResult.Success(resultDto.items.map { it.toDomain() })
                    } else {
                        ApiResult.Error("No data returned")
                    }
                }
                is ApiResult.Error -> result
                is ApiResult.Loading -> result
            }
        } catch (e: Exception) {
            ApiResult.Error("Failed to extract menu: ${e.message}")
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

fun MenuItemDto.toDomain() = MenuItem(
    id = id,
    name = name,
    description = description,
    price = price,
    image = image,
    available = available,
    visible = visible,
    categoryId = categoryId,
    categoryName = categoryName,
    preparationTime = preparationTime,
    sortOrder = sortOrder,
    customizationGroups = customizationGroups?.map { it.toDomain() } ?: emptyList()
)

fun ExtractedMenuItemDto.toDomain() = com.appvendor.feature_menu_items.domain.model.ExtractedMenuItem(
    name = name,
    price = price,
    categoryName = categoryName
)
