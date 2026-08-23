package com.appvendor.feature_qr.domain.model

import android.graphics.Bitmap

/**
 * Represents the data associated with a generated QR code.
 *
 * @property vendorId The unique identifier of the vendor.
 * @property url The URL encoded in the QR code.
 * @property bitmap The generated QR code bitmap image.
 */
data class QrData(
    val vendorId: String,
    val url: String,
    val bitmap: Bitmap
)
