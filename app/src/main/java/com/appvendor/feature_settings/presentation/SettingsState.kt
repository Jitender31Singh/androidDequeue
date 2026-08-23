package com.appvendor.feature_settings.presentation

import com.appvendor.feature_settings.domain.model.SettingsData

data class SettingsState(
    val settings: SettingsData? = null,
    val isLoading: Boolean = false,
    val isUpdating: Boolean = false,
    val error: String? = null
)
