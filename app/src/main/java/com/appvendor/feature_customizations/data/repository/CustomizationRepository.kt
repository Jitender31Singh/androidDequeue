package com.appvendor.feature_customizations.data.repository

import com.appvendor.core.network.ApiResult
import com.appvendor.core.network.safeApiCall
import com.appvendor.feature_customizations.data.remote.CustomizationApiService
import com.appvendor.feature_customizations.data.remote.dto.CreateCustomizationGroupRequest
import com.appvendor.feature_customizations.data.remote.dto.CustomizationGroupDto
import com.appvendor.feature_customizations.data.remote.dto.CustomizationOptionDto
import com.appvendor.feature_customizations.data.remote.dto.UpdateCustomizationGroupRequest
import com.appvendor.feature_customizations.domain.model.CustomizationGroup
import com.appvendor.feature_customizations.domain.model.CustomizationOption
import com.appvendor.feature_customizations.domain.model.SelectionType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CustomizationRepository @Inject constructor(
    private val api: CustomizationApiService
) {

    suspend fun getCustomizations(): ApiResult<List<CustomizationGroup>> {
        return when (val result = safeApiCall { api.getCustomizations() }) {
            is ApiResult.Success -> ApiResult.Success(
                result.data.data?.map { it.toDomain() } ?: emptyList()
            )
            is ApiResult.Error -> result
            is ApiResult.Loading -> result
        }
    }

    suspend fun createCustomization(group: CustomizationGroup): ApiResult<CustomizationGroup> {
        val request = CreateCustomizationGroupRequest(
            name = group.name,
            selectionType = group.selectionType.name,
            required = group.required,
            minSelection = group.minSelection,
            maxSelection = group.maxSelection,
            options = group.options.map { CustomizationOptionDto(null, it.name, it.additionalPrice) }
        )
        return when (val result = safeApiCall { api.createCustomization(request) }) {
            is ApiResult.Success -> {
                val dto = result.data.data
                if (dto != null) ApiResult.Success(dto.toDomain())
                else ApiResult.Error("No data returned")
            }
            is ApiResult.Error -> result
            is ApiResult.Loading -> result
        }
    }

    suspend fun updateCustomization(id: String, group: CustomizationGroup): ApiResult<CustomizationGroup> {
        val request = UpdateCustomizationGroupRequest(
            name = group.name,
            selectionType = group.selectionType.name,
            required = group.required,
            minSelection = group.minSelection,
            maxSelection = group.maxSelection,
            options = group.options.map { CustomizationOptionDto(it.id, it.name, it.additionalPrice) }
        )
        return when (val result = safeApiCall { api.updateCustomization(id, request) }) {
            is ApiResult.Success -> {
                val dto = result.data.data
                if (dto != null) ApiResult.Success(dto.toDomain())
                else ApiResult.Error("No data returned")
            }
            is ApiResult.Error -> result
            is ApiResult.Loading -> result
        }
    }

    suspend fun deleteCustomization(id: String): Result<Unit> {
        return try {
            val response = api.deleteCustomization(id)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to delete customization: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

fun CustomizationGroupDto.toDomain() = CustomizationGroup(
    id = id,
    name = name,
    selectionType = runCatching { SelectionType.valueOf(selectionType) }.getOrDefault(SelectionType.SINGLE),
    required = required,
    minSelection = minSelection,
    maxSelection = maxSelection,
    options = options.map { CustomizationOption(it.id, it.name, it.additionalPrice) }
)
