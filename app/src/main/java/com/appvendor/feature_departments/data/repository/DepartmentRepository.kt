package com.appvendor.feature_departments.data.repository

import com.appvendor.core.network.ApiResult
import com.appvendor.core.network.safeApiCall
import com.appvendor.feature_departments.data.remote.DepartmentApiService
import com.appvendor.feature_departments.data.remote.dto.CreateDepartmentRequest
import com.appvendor.feature_departments.data.remote.dto.DepartmentDto
import com.appvendor.feature_departments.data.remote.dto.UpdateDepartmentRequest
import com.appvendor.feature_departments.domain.model.Department
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DepartmentRepository @Inject constructor(
    private val api: DepartmentApiService
) {
    suspend fun getDepartments(): ApiResult<List<Department>> {
        return when (val result = safeApiCall { api.getDepartments() }) {
            is ApiResult.Success -> ApiResult.Success(
                result.data.data?.map { it.toDomain() } ?: emptyList()
            )
            is ApiResult.Error -> result
            is ApiResult.Loading -> result
        }
    }

    suspend fun createDepartment(name: String, description: String?): ApiResult<Department> {
        val request = CreateDepartmentRequest(name = name, description = description)
        return when (val result = safeApiCall { api.createDepartment(request) }) {
            is ApiResult.Success -> {
                val dto = result.data.data
                if (dto != null) ApiResult.Success(dto.toDomain())
                else ApiResult.Error("No data returned")
            }
            is ApiResult.Error -> result
            is ApiResult.Loading -> result
        }
    }

    suspend fun updateDepartment(id: String, name: String?, description: String?): ApiResult<Department> {
        val request = UpdateDepartmentRequest(name = name, description = description)
        return when (val result = safeApiCall { api.updateDepartment(id, request) }) {
            is ApiResult.Success -> {
                val dto = result.data.data
                if (dto != null) ApiResult.Success(dto.toDomain())
                else ApiResult.Error("No data returned")
            }
            is ApiResult.Error -> result
            is ApiResult.Loading -> result
        }
    }

    suspend fun deleteDepartment(id: String): Result<Unit> {
        return try {
            val response = api.deleteDepartment(id)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Cannot delete department with active staff or failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

fun DepartmentDto.toDomain() = Department(
    id = id,
    name = name,
    description = description,
    staffCount = staffCount,
    active = active
)
