package com.appvendor.core.network

import androidx.annotation.Keep
import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

@Keep
data class CloudinaryUploadResponse(
    val secure_url: String,
    val public_id: String,
    val format: String,
    val error: CloudinaryError? = null
)

@Keep
data class CloudinaryError(
    val message: String
)

interface CloudinaryApiService {
    @Multipart
    @POST("{cloud_name}/image/upload")
    suspend fun uploadImage(
        @Path("cloud_name") cloudName: String,
        @Part file: MultipartBody.Part,
        @Part uploadPreset: MultipartBody.Part
    ): retrofit2.Response<CloudinaryUploadResponse>
}
