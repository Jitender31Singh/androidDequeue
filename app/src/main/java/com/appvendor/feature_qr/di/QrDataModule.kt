package com.appvendor.feature_qr.di

import com.appvendor.feature_qr.data.repository.QrRepositoryImpl
import com.appvendor.feature_qr.domain.repository.QrRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Dagger Hilt module for providing dependencies related to the QR feature data layer.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class QrDataModule {

    @Binds
    @Singleton
    abstract fun bindQrRepository(
        qrRepositoryImpl: QrRepositoryImpl
    ): QrRepository
}
