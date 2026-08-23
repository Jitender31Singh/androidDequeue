package com.appvendor.feature_qr.domain.repository

import android.graphics.Bitmap
import com.appvendor.feature_qr.domain.model.QrData

/**
 * Repository interface for managing QR code data.
 */
interface QrRepository {
    
    /**
     * Generates a QR code for the given vendor ID.
     *
     * @param vendorId The unique identifier of the vendor.
     * @return Result containing [QrData] on success, or an Exception on failure.
     */
    suspend fun generateQrCode(vendorId: String): Result<QrData>

    /**
     * Saves the QR code bitmap to a file for sharing or downloading.
     *
     * @param bitmap The QR code bitmap to save.
     * @param fileName The desired name for the saved file.
     * @return Result containing the saved file path on success, or an Exception on failure.
     */
    suspend fun saveQrCodeToFile(bitmap: Bitmap, fileName: String): Result<String>
}
