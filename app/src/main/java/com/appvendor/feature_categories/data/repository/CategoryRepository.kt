package com.appvendor.feature_categories.data.repository

import com.appvendor.core.network.ApiResult
import com.appvendor.core.network.safeApiCall
import com.appvendor.feature_categories.data.remote.CategoryApiService
import com.appvendor.feature_categories.data.remote.dto.CategoryDto
import com.appvendor.feature_categories.data.remote.dto.CreateCategoryRequest
import com.appvendor.feature_categories.data.remote.dto.SortOrderItem
import com.appvendor.feature_categories.data.remote.dto.SortOrderRequest
import com.appvendor.feature_categories.data.remote.dto.UpdateCategoryRequest
import com.appvendor.feature_categories.domain.model.Category
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepository @Inject constructor(
    private val api: CategoryApiService
) {

    suspend fun getCategories(): ApiResult<List<Category>> {
        return when (val result = safeApiCall { api.getCategories() }) {
            is ApiResult.Success -> ApiResult.Success(
                result.data.data?.map { it.toDomain() } ?: emptyList()
            )
            is ApiResult.Error -> result
            is ApiResult.Loading -> result
        }
    }

    suspend fun createCategory(name: String, description: String?, sortOrder: Int): ApiResult<Category> {
        return when (val result = safeApiCall { 
            api.createCategory(CreateCategoryRequest(name, description, sortOrder)) 
        }) {
            is ApiResult.Success -> {
                val dto = result.data.data
                if (dto != null) ApiResult.Success(dto.toDomain())
                else ApiResult.Error("No data returned")
            }
            is ApiResult.Error -> result
            is ApiResult.Loading -> result
        }
    }

    suspend fun updateCategory(id: String, name: String, description: String?): ApiResult<Category> {
        return when (val result = safeApiCall { 
            api.updateCategory(id, UpdateCategoryRequest(name, description)) 
        }) {
            is ApiResult.Success -> {
                val dto = result.data.data
                if (dto != null) ApiResult.Success(dto.toDomain())
                else ApiResult.Error("No data returned")
            }
            is ApiResult.Error -> result
            is ApiResult.Loading -> result
        }
    }

    suspend fun deleteCategory(id: String): Result<Unit> {
        return try {
            val response = api.deleteCategory(id)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to delete category: ${response.code()}"))
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
}

fun CategoryDto.toDomain() = Category(
    id = id,
    name = name,
    description = description,
    image = image,
    sortOrder = sortOrder,
    active = active,
    itemCount = itemCount
)
