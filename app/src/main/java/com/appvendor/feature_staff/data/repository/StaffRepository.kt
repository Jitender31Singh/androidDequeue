package com.appvendor.feature_staff.data.repository

import com.appvendor.core.network.ApiResult
import com.appvendor.core.network.safeApiCall
import com.appvendor.feature_staff.data.remote.StaffApiService
import com.appvendor.feature_staff.data.remote.dto.CreateStaffRequest
import com.appvendor.feature_staff.data.remote.dto.StaffDto
import com.appvendor.feature_staff.data.remote.dto.StaffStatusRequest
import com.appvendor.feature_staff.data.remote.dto.UpdateStaffRequest
import com.appvendor.feature_staff.domain.model.Staff
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StaffRepository @Inject constructor(
    private val api: StaffApiService
) {
    suspend fun getStaffList(): ApiResult<List<Staff>> {
        return when (val result = safeApiCall { api.getStaffList() }) {
            is ApiResult.Success -> ApiResult.Success(
                result.data.data?.content?.map { it.toDomain() } ?: emptyList()
            )
            is ApiResult.Error -> result
            is ApiResult.Loading -> result
        }
    }

    suspend fun createStaff(
        name: String,
        email: String,
        password: String?,
        phone: String?,
        departmentId: String?,
        role: String,
        permissions: List<String>
    ): ApiResult<Staff> {
        val request = CreateStaffRequest(name, email, password, phone, departmentId, role, permissions)
        return when (val result = safeApiCall { api.createStaff(request) }) {
            is ApiResult.Success -> {
                val dto = result.data.data
                if (dto != null) ApiResult.Success(dto.toDomain())
                else ApiResult.Error("No data returned")
            }
            is ApiResult.Error -> result
            is ApiResult.Loading -> result
        }
    }

    suspend fun updateStaff(
        id: String,
        name: String?,
        phone: String?,
        departmentId: String?,
        role: String?,
        permissions: List<String>?
    ): ApiResult<Staff> {
        val request = UpdateStaffRequest(name, phone, departmentId, role, permissions)
        return when (val result = safeApiCall { api.updateStaff(id, request) }) {
            is ApiResult.Success -> {
                val dto = result.data.data
                if (dto != null) ApiResult.Success(dto.toDomain())
                else ApiResult.Error("No data returned")
            }
            is ApiResult.Error -> result
            is ApiResult.Loading -> result
        }
    }

    suspend fun toggleStaffStatus(id: String, status: String): ApiResult<Staff> {
        val request = StaffStatusRequest(status)
        return when (val result = safeApiCall { api.toggleStaffStatus(id, request) }) {
            is ApiResult.Success -> {
                val dto = result.data.data
                if (dto != null) ApiResult.Success(dto.toDomain())
                else ApiResult.Error("No data returned")
            }
            is ApiResult.Error -> result
            is ApiResult.Loading -> result
        }
    }

    suspend fun deleteStaff(id: String): Result<Unit> {
        return try {
            val response = api.deleteStaff(id)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to delete staff: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

fun StaffDto.toDomain() = Staff(
    id = id,
    name = name,
    email = email,
    phone = phone,
    departmentId = departmentId,
    departmentName = departmentName,
    role = role,
    permissions = permissions,
    status = status,
    avatar = avatar,
    lastLoginAt = lastLoginAt
)
