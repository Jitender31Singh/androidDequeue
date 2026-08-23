package com.appvendor.feature_qr.domain.usecase

import com.appvendor.feature_qr.domain.model.QrData
import com.appvendor.feature_qr.domain.repository.QrRepository
import javax.inject.Inject

/**
 * Use case for generating a QR code for a given vendor.
 *
 * @property repository The repository used to generate the QR code.
 */
class GenerateQrUseCase @Inject constructor(
    private val repository: QrRepository
) {
    /**
     * Executes the use case.
     *
     * @param vendorId The unique identifier of the vendor.
     * @return Result containing [QrData] on success, or an Exception on failure.
     */
    suspend operator fun invoke(vendorId: String): Result<QrData> {
        return repository.generateQrCode(vendorId)
    }
}
