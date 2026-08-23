package com.appvendor.feature_printer.presentation

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appvendor.core.network.ApiResult
import com.appvendor.feature_settings.data.repository.SettingsRepository
import com.appvendor.feature_shop_profile.data.repository.ShopProfileRepository
import com.appvendor.feature_shop_profile.domain.model.PrinterConfig
import com.appvendor.feature_settings.domain.model.SettingsData
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PrinterSettingsViewModel @Inject constructor(
    private val shopProfileRepository: ShopProfileRepository,
    private val settingsRepository: SettingsRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(PrinterSettingsState())
    val state: StateFlow<PrinterSettingsState> = _state.asStateFlow()

    /** Cached settings data so we can patch only the printerSettings field on save */
    private var cachedSettings: SettingsData? = null

    init {
        loadPrinterConfig()
        loadPairedBluetoothDevices()
    }

    // ── Data loading ──────────────────────────────────────────────────────────

    fun loadPrinterConfig() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            // Primary source: dedicated printer endpoint
            when (val result = shopProfileRepository.getPrinterConfig()) {
                is ApiResult.Success -> {
                    _state.update { it.copy(isLoading = false, printerConfig = result.data) }
                }
                is ApiResult.Error -> {
                    // Fallback: read printerSettings from general settings
                    when (val settingsResult = settingsRepository.getSettings()) {
                        is ApiResult.Success -> {
                            cachedSettings = settingsResult.data
                            val pc = settingsResult.data.printerSettings
                            if (pc != null) {
                                _state.update { it.copy(isLoading = false, printerConfig = pc) }
                            } else {
                                _state.update { it.copy(isLoading = false, error = result.message) }
                            }
                        }
                        else -> _state.update { it.copy(isLoading = false, error = result.message) }
                    }
                }
                else -> {}
            }

            // Also pre-load settings for save path
            if (cachedSettings == null) {
                when (val s = settingsRepository.getSettings()) {
                    is ApiResult.Success -> cachedSettings = s.data
                    else -> {}
                }
            }
        }
    }

    /** Refresh Bluetooth paired devices list */
    fun loadPairedBluetoothDevices() {
        try {
            val bm = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
            val adapter: BluetoothAdapter? = bm?.adapter
            val paired = adapter?.bondedDevices?.map { device ->
                BluetoothDeviceInfo(name = device.name ?: "Unknown", address = device.address)
            } ?: emptyList()
            _state.update { it.copy(pairedDevices = paired) }
        } catch (e: SecurityException) {
            // Bluetooth permission not granted yet — the UI will request it
        }
    }

    // ── Config mutations (immediate UI feedback, saved on demand) ─────────────

    fun setEnabled(enabled: Boolean) =
        _state.update { it.copy(printerConfig = it.printerConfig.copy(enabled = enabled)) }

    fun setPrinterType(type: String) =
        _state.update { it.copy(printerConfig = it.printerConfig.copy(printerType = type)) }

    fun setPaperWidth(width: String) =
        _state.update { it.copy(printerConfig = it.printerConfig.copy(paperWidth = width)) }

    fun setPrinterName(name: String) =
        _state.update { it.copy(printerConfig = it.printerConfig.copy(printerName = name)) }

    fun setNetworkIp(ip: String) =
        _state.update { it.copy(printerConfig = it.printerConfig.copy(networkIp = ip)) }

    fun setNetworkPort(port: Int) =
        _state.update { it.copy(printerConfig = it.printerConfig.copy(networkPort = port)) }

    fun setAutoPrintOnReady(value: Boolean) =
        _state.update { it.copy(printerConfig = it.printerConfig.copy(autoPrintOnReady = value)) }

    fun setAutoPrintOnComplete(value: Boolean) =
        _state.update { it.copy(printerConfig = it.printerConfig.copy(autoPrintOnComplete = value)) }

    /** Select a Bluetooth device — writes name + address to config */
    fun selectBluetoothDevice(device: BluetoothDeviceInfo) {
        _state.update {
            it.copy(
                printerConfig = it.printerConfig.copy(
                    printerName = device.name,
                    networkIp = device.address   // store BT MAC in networkIp field for transport
                )
            )
        }
    }

    // ── Save ──────────────────────────────────────────────────────────────────

    fun save() {
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, error = null) }

            val configToSave = _state.value.printerConfig

            when (val result = shopProfileRepository.updatePrinterConfig(configToSave)) {
                is ApiResult.Success -> {
                    _state.update {
                        it.copy(
                            isSaving = false,
                            printerConfig = result.data,
                            successMessage = "Printer settings saved!"
                        )
                    }
                }
                is ApiResult.Error -> _state.update { it.copy(isSaving = false, error = result.message) }
                is ApiResult.Loading -> {}
            }
        }
    }

    // ── Connection test (stub — real BT/Network ping goes here) ──────────────

    fun testConnection() {
        viewModelScope.launch {
            _state.update { it.copy(connectionStatus = PrinterConnectionStatus.UNKNOWN, error = null) }
            kotlinx.coroutines.delay(1200)
            val config = _state.value.printerConfig
            val reachable = when {
                config.printerType == "BROWSER" -> true
                config.printerType == "BLUETOOTH" -> config.printerName.isNotBlank()
                config.printerType == "NETWORK" || config.printerType == "WIFI" -> config.networkIp.isNotBlank()
                else -> false
            }
            _state.update {
                it.copy(
                    connectionStatus = if (reachable) PrinterConnectionStatus.CONNECTED
                                       else PrinterConnectionStatus.DISCONNECTED
                )
            }
        }
    }

    fun dismissMessages() = _state.update { it.copy(successMessage = null, error = null) }
}
