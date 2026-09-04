package com.appvendor.feature_auth.data.remote.dto

data class DeviceRegistrationRequest(
    val deviceId: String,
    val fcmToken: String,
    val deviceName: String,
    val platform: String,
    val appVersion: String
)
