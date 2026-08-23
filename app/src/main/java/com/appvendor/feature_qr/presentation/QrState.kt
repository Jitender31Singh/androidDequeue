package com.appvendor.feature_qr.presentation

import com.appvendor.feature_qr.domain.model.QrData

/**
 * Represents the UI state for the QR code screen.
 *
 * @property isLoading Whether the QR code is currently being generated.
 * @property qrData The generated QR code data, if available.
 * @property error Any error message that occurred during generation or saving.
 * @property savedFilePath The file path of the saved QR code, if applicable.
 */
data class QrState(
    val isLoading: Boolean = false,
    val qrData: QrData? = null,
    val error: String? = null,
    val savedFilePath: String? = null,
    val shopName: String? = null
)
