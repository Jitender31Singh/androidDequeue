package com.appvendor.feature_qr.data.repository

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.appvendor.feature_qr.domain.model.QrData
import com.appvendor.feature_qr.domain.repository.QrRepository
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QrRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : QrRepository {

    object Constants {
        const val QR_BASE_URL = "https://appvendor.com/v/"
    }

    private var cachedQrData: QrData? = null

    override suspend fun generateQrCode(vendorCode: String): Result<QrData> = withContext(Dispatchers.IO) {
        try {
            if (cachedQrData?.vendorId == vendorCode) {
                return@withContext Result.success(cachedQrData!!)
            }

            val url = "https://dequeue-qofo.onrender.com/customer.html?vendor=$vendorCode"
            val size = 1024 // High resolution for crisp printing/display
            
            val writer = QRCodeWriter()
            val hints = mapOf(EncodeHintType.MARGIN to 2)
            val bitMatrix = writer.encode(url, BarcodeFormat.QR_CODE, size, size, hints)
            val width = bitMatrix.width
            val height = bitMatrix.height
            
            // Use ARGB_8888 as required
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    // Dark theme brand color or black. Let's use black for standard high contrast
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }

            val qrData = QrData(vendorCode, url, bitmap)
            cachedQrData = qrData
            Result.success(qrData)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveQrCodeToFile(bitmap: Bitmap, fileName: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, "$fileName.png")
                put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/DeQueue")
                }
            }
            
            val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            if (uri != null) {
                context.contentResolver.openOutputStream(uri)?.use { stream ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                }
                Result.success(uri.toString())
            } else {
                Result.failure(Exception("Failed to create MediaStore entry"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
