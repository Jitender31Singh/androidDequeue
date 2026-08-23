package com.appvendor.core.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

sealed class ApiResult<out T> {
    data class Success<out T>(val data: T) : ApiResult<T>()
    data class Error(val message: String, val code: Int? = null, val exception: Exception? = null) : ApiResult<Nothing>()
    object Loading : ApiResult<Nothing>()
}

suspend fun <T> safeApiCall(apiCall: suspend () -> T): ApiResult<T> {
    return withContext(Dispatchers.IO) {
        try {
            ApiResult.Success(apiCall())
        } catch (e: HttpException) {
            ApiResult.Error(
                message = e.localizedMessage ?: "An unexpected HTTP error occurred",
                code = e.code(),
                exception = e
            )
        } catch (e: IOException) {
            ApiResult.Error(
                message = "Couldn't reach server. Check your internet connection.",
                exception = e
            )
        } catch (e: Exception) {
            ApiResult.Error(
                message = e.localizedMessage ?: "An unexpected error occurred",
                exception = e
            )
        }
    }
}
