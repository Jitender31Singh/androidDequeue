package com.appvendor.feature_settings.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.appvendor.feature_settings.domain.model.*
import com.appvendor.feature_shop_profile.domain.model.PrinterConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        snackbarHost = {
            if (state.error != null) {
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    action = { TextButton(onClick = { viewModel.dismissError() }) { Text("Dismiss") } }
                ) { Text(state.error!!) }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        if (state.isLoading && state.settings == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (state.settings != null) {
            val s = state.settings!!
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (state.isUpdating) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }

                // Order & Queue Settings
                SettingsCard(title = "Order & Queue") {
                    SettingSwitch(
                        label = "Allow Custom Orders", 
                        checked = s.allowCustomOrder,
                        onCheckedChange = { viewModel.updateSettings(s.copy(allowCustomOrder = it)) }
                    )
                    SettingSwitch(
                        label = "Auto Accept Orders", 
                        checked = s.autoAcceptOrders,
                        onCheckedChange = { viewModel.updateSettings(s.copy(autoAcceptOrders = it)) }
                    )
                    SettingSwitch(
                        label = "Enable Geofence", 
                        checked = s.enableGeofence,
                        onCheckedChange = { viewModel.updateSettings(s.copy(enableGeofence = it)) }
                    )
                    SettingNumberInput(
                        label = "Max Queue Size",
                        value = s.maxQueueSize,
                        onValueChange = { viewModel.updateSettings(s.copy(maxQueueSize = it)) }
                    )
                    SettingNumberInput(
                        label = "Est. Prep Time (Mins)",
                        value = s.estimatedPrepTime,
                        onValueChange = { viewModel.updateSettings(s.copy(estimatedPrepTime = it)) }
                    )
                    SettingTextInput(
                        label = "Order Prefix",
                        value = s.orderPrefix,
                        onValueChange = { viewModel.updateSettings(s.copy(orderPrefix = it)) }
                    )
                }

                // Billing & Display
                SettingsCard(title = "Billing & Display") {
                    SettingSwitch(
                        label = "Show Prep Time", 
                        checked = s.showPreparationTime,
                        onCheckedChange = { viewModel.updateSettings(s.copy(showPreparationTime = it)) }
                    )
                    SettingTextInput(
                        label = "Currency",
                        value = s.currency,
                        onValueChange = { viewModel.updateSettings(s.copy(currency = it)) }
                    )
                    SettingTextInput(
                        label = "GST Number",
                        value = s.gstNumber ?: "",
                        onValueChange = { viewModel.updateSettings(s.copy(gstNumber = it.ifBlank { null })) }
                    )
                    SettingTextInput(
                        label = "Tax Name",
                        value = s.taxName,
                        onValueChange = { viewModel.updateSettings(s.copy(taxName = it)) }
                    )
                    SettingNumberInputDouble(
                        label = "Tax Percentage",
                        value = s.taxPercentage,
                        onValueChange = { viewModel.updateSettings(s.copy(taxPercentage = it)) }
                    )
                    SettingTextInput(
                        label = "Additional Charge Name",
                        value = s.additionalChargeName,
                        onValueChange = { viewModel.updateSettings(s.copy(additionalChargeName = it)) }
                    )
                    SettingNumberInputDouble(
                        label = "Additional Charges Amount",
                        value = s.additionalCharges,
                        onValueChange = { viewModel.updateSettings(s.copy(additionalCharges = it)) }
                    )
                }

                // Banking & Payment
                SettingsCard(title = "Payment & Banking") {
                    SettingSwitch(
                        label = "Enable Online Payment", 
                        checked = s.enableOnlinePayment,
                        onCheckedChange = { viewModel.updateSettings(s.copy(enableOnlinePayment = it)) }
                    )
                    SettingTextInput(
                        label = "UPI ID",
                        value = s.upiId ?: "",
                        onValueChange = { viewModel.updateSettings(s.copy(upiId = it.ifBlank { null })) }
                    )
                    SettingTextInput(
                        label = "Bank Account Name",
                        value = s.bankAccountName ?: "",
                        onValueChange = { viewModel.updateSettings(s.copy(bankAccountName = it.ifBlank { null })) }
                    )
                    SettingTextInput(
                        label = "Bank Account Number",
                        value = s.bankAccountNumber ?: "",
                        onValueChange = { viewModel.updateSettings(s.copy(bankAccountNumber = it.ifBlank { null })) }
                    )
                    SettingTextInput(
                        label = "Bank IFSC Code",
                        value = s.bankIfscCode ?: "",
                        onValueChange = { viewModel.updateSettings(s.copy(bankIfscCode = it.ifBlank { null })) }
                    )
                }

                // Printer Settings ── Scenarios 2, 3, 4, 5, 6
                val printer = s.printerSettings ?: PrinterConfig(
                    enabled = false,
                    printerType = "BROWSER",
                    printerName = "",
                    paperWidth = "80mm",
                    networkIp = "",
                    networkPort = 9100,
                    autoPrintOnReady = false,
                    autoPrintOnComplete = false
                )
                SettingsCard(title = "Printer Settings") {
                    // Scenario 6: Kill-switch — enable / disable all printer automation
                    SettingSwitch(
                        label = "Enable Printer Automation",
                        checked = printer.enabled,
                        onCheckedChange = { enabled ->
                            viewModel.updateSettings(s.copy(printerSettings = printer.copy(enabled = enabled)))
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Scenarios 2 & 3: Paper width — 58mm or 80mm
                    Text(
                        text = "Paper Width",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("58mm", "80mm").forEach { width ->
                            FilterChip(
                                selected = printer.paperWidth == width,
                                onClick = {
                                    viewModel.updateSettings(
                                        s.copy(printerSettings = printer.copy(paperWidth = width))
                                    )
                                },
                                label = { Text(width) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Scenario 5: Printer type — BROWSER, BLUETOOTH, NETWORK
                    Text(
                        text = "Printer Type",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("BROWSER", "BLUETOOTH", "NETWORK").forEach { type ->
                            FilterChip(
                                selected = printer.printerType == type,
                                onClick = {
                                    viewModel.updateSettings(
                                        s.copy(printerSettings = printer.copy(printerType = type))
                                    )
                                },
                                label = { Text(type) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Scenario 4: Auto-print when status changes to READY
                    SettingSwitch(
                        label = "Auto-Print when Order is READY",
                        checked = printer.autoPrintOnReady,
                        onCheckedChange = { autoReady ->
                            viewModel.updateSettings(
                                s.copy(printerSettings = printer.copy(autoPrintOnReady = autoReady))
                            )
                        }
                    )

                    // Auto-print when status changes to COMPLETED
                    SettingSwitch(
                        label = "Auto-Print when Order is COMPLETED",
                        checked = printer.autoPrintOnComplete,
                        onCheckedChange = { autoComplete ->
                            viewModel.updateSettings(
                                s.copy(printerSettings = printer.copy(autoPrintOnComplete = autoComplete))
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
fun SettingSwitch(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun SettingNumberInput(label: String, value: Int, onValueChange: (Int) -> Unit) {
    OutlinedTextField(
        value = value.toString(),
        onValueChange = { 
            val intVal = it.toIntOrNull()
            if (intVal != null) {
                onValueChange(intVal)
            } else if (it.isEmpty()) {
                onValueChange(0)
            }
        },
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    )
}

@Composable
fun SettingNumberInputDouble(label: String, value: Double, onValueChange: (Double) -> Unit) {
    OutlinedTextField(
        value = value.toString(),
        onValueChange = { 
            val doubleVal = it.toDoubleOrNull()
            if (doubleVal != null) {
                onValueChange(doubleVal)
            } else if (it.isEmpty()) {
                onValueChange(0.0)
            }
        },
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    )
}

@Composable
fun SettingTextInput(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    )
}
