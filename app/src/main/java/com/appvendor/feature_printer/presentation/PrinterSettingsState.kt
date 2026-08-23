package com.appvendor.feature_printer.presentation

import com.appvendor.feature_shop_profile.domain.model.PrinterConfig

/**
 * UI state for the Counter-only Printer Settings screen.
 */
data class PrinterSettingsState(
    // The live config fetched from the server
    val printerConfig: PrinterConfig = PrinterConfig(
        enabled = false,
        printerType = "BROWSER",
        printerName = "",
        paperWidth = "80mm",
        networkIp = "",
        networkPort = 9100,
        autoPrintOnReady = false,
        autoPrintOnComplete = false
    ),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val successMessage: String? = null,
    val error: String? = null,

    // Bluetooth device discovery
    val isScanning: Boolean = false,
    val pairedDevices: List<BluetoothDeviceInfo> = emptyList(),
    val scannedDevices: List<BluetoothDeviceInfo> = emptyList(),

    // Connection status indicator
    val connectionStatus: PrinterConnectionStatus = PrinterConnectionStatus.UNKNOWN
)

data class BluetoothDeviceInfo(
    val name: String,
    val address: String
)

enum class PrinterConnectionStatus {
    UNKNOWN,    // Not yet tested
    CONNECTED,  // Last test succeeded
    DISCONNECTED // Last test failed
}
