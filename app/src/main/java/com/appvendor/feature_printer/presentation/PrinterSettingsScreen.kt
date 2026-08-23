package com.appvendor.feature_printer.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothSearching
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.outlined.Bluetooth
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Print
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

// Brand colors
private val VioletPrimary = Color(0xFF5E35B1)
private val VioletLight  = Color(0xFFEDE7F6)
private val SlateText    = Color(0xFF0F172A)
private val MutedText    = Color(0xFF64748B)
private val CardBorder   = Color(0xFFE2E8F0)
private val CardBg       = Color(0xFFFFFFFF)
private val GreenOk      = Color(0xFF16A34A)
private val RedErr       = Color(0xFFDC2626)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrinterSettingsScreen(
    viewModel: PrinterSettingsViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Auto-dismiss messages
    LaunchedEffect(state.successMessage) {
        if (state.successMessage != null) {
            kotlinx.coroutines.delay(2500)
            viewModel.dismissMessages()
        }
    }

    Box(modifier = modifier.fillMaxSize().background(Color(0xFFF8FAFC))) {
        if (state.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = VioletPrimary
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ── Save progress bar ─────────────────────────────────────────
                if (state.isSaving) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(4.dp)),
                        color = VioletPrimary
                    )
                }

                // ── Connection status banner ──────────────────────────────────
                ConnectionStatusBanner(
                    status = state.connectionStatus,
                    onTest = viewModel::testConnection
                )

                // ── Master enable / disable ───────────────────────────────────
                PrinterCard(title = "Printer Automation") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Enable Printer", fontWeight = FontWeight.SemiBold, color = SlateText)
                            Text(
                                "Turn off to require manual print taps",
                                style = MaterialTheme.typography.bodySmall,
                                color = MutedText
                            )
                        }
                        Switch(
                            checked = state.printerConfig.enabled,
                            onCheckedChange = viewModel::setEnabled,
                            colors = SwitchDefaults.colors(checkedThumbColor = VioletPrimary, checkedTrackColor = VioletLight)
                        )
                    }
                }

                // ── Connection type selector ──────────────────────────────────
                PrinterCard(title = "Connection Type") {
                    PrinterTypeSelector(
                        selected = state.printerConfig.printerType,
                        onSelect = viewModel::setPrinterType
                    )
                }

                // ── Type-specific configuration ───────────────────────────────
                AnimatedVisibility(
                    visible = state.printerConfig.printerType == "BLUETOOTH",
                    enter = fadeIn() + expandVertically(),
                    exit  = fadeOut() + shrinkVertically()
                ) {
                    BluetoothSection(
                        selectedName    = state.printerConfig.printerName,
                        pairedDevices   = state.pairedDevices,
                        onRefresh       = viewModel::loadPairedBluetoothDevices,
                        onSelectDevice  = viewModel::selectBluetoothDevice
                    )
                }

                AnimatedVisibility(
                    visible = state.printerConfig.printerType in listOf("NETWORK", "WIFI"),
                    enter = fadeIn() + expandVertically(),
                    exit  = fadeOut() + shrinkVertically()
                ) {
                    NetworkSection(
                        ip   = state.printerConfig.networkIp,
                        port = state.printerConfig.networkPort,
                        onIpChange   = viewModel::setNetworkIp,
                        onPortChange = viewModel::setNetworkPort
                    )
                }

                AnimatedVisibility(
                    visible = state.printerConfig.printerType == "BROWSER",
                    enter = fadeIn() + expandVertically(),
                    exit  = fadeOut() + shrinkVertically()
                ) {
                    PrinterCard(title = "Browser Print") {
                        Text(
                            "Uses the Android system print dialog. No extra configuration needed.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MutedText
                        )
                    }
                }

                // ── Paper width ───────────────────────────────────────────────
                PrinterCard(title = "Paper Size") {
                    Text("Select receipt roll width:", style = MaterialTheme.typography.bodyMedium, color = MutedText)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        listOf("58mm" to "2 inch (Narrow)", "80mm" to "3 inch (Standard)").forEach { (width, label) ->
                            PaperWidthChip(
                                width    = width,
                                label    = label,
                                selected = state.printerConfig.paperWidth == width,
                                onClick  = { viewModel.setPaperWidth(width) }
                            )
                        }
                    }
                }

                // ── Auto-print triggers ───────────────────────────────────────
                PrinterCard(title = "Auto-Print Triggers") {
                    AutoPrintRow(
                        label       = "Print when order is READY",
                        sublabel    = "Bill prints the moment cashier taps 'Mark as Ready'",
                        checked     = state.printerConfig.autoPrintOnReady,
                        onChecked   = viewModel::setAutoPrintOnReady
                    )
                    Spacer(Modifier.height(4.dp))
                    AutoPrintRow(
                        label       = "Print when order is COMPLETED",
                        sublabel    = "Bill prints on final completion",
                        checked     = state.printerConfig.autoPrintOnComplete,
                        onChecked   = viewModel::setAutoPrintOnComplete
                    )
                }

                // ── Action buttons ────────────────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = viewModel::testConnection,
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.5.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = VioletPrimary)
                    ) {
                        Icon(Icons.Filled.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Test", fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = viewModel::save,
                        modifier = Modifier.weight(2f).height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = VioletPrimary),
                        enabled = !state.isSaving
                    ) {
                        if (state.isSaving) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Save Settings", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))
            }
        }

        // ── Snackbars ─────────────────────────────────────────────────────────
        state.successMessage?.let { msg ->
            Snackbar(
                modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
                containerColor = GreenOk,
                contentColor = Color.White
            ) { Text(msg) }
        }

        state.error?.let { err ->
            Snackbar(
                modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
                containerColor = RedErr,
                contentColor = Color.White,
                action = {
                    TextButton(onClick = viewModel::dismissMessages) {
                        Text("Dismiss", color = Color.White)
                    }
                }
            ) { Text(err) }
        }
    }
}

// ── Sub-components ─────────────────────────────────────────────────────────────

@Composable
private fun PrinterCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CardBorder, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = VioletPrimary,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            content()
        }
    }
}

@Composable
private fun ConnectionStatusBanner(
    status: PrinterConnectionStatus,
    onTest: () -> Unit
) {
    val (bg, fg, label) = when (status) {
        PrinterConnectionStatus.CONNECTED    -> Triple(Color(0xFFDCFCE7), GreenOk, "✓ Printer reachable")
        PrinterConnectionStatus.DISCONNECTED -> Triple(Color(0xFFFEE2E2), RedErr, "✗ Printer unreachable")
        PrinterConnectionStatus.UNKNOWN      -> Triple(Color(0xFFF1F5F9), MutedText, "Connection not tested")
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .clickable { onTest() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontWeight = FontWeight.SemiBold, color = fg, fontSize = 14.sp)
        Icon(Icons.Filled.Refresh, contentDescription = "Re-test", tint = fg, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun PrinterTypeSelector(selected: String, onSelect: (String) -> Unit) {
    data class TypeOption(val key: String, val label: String, val sublabel: String, val icon: ImageVector, val selectedIcon: ImageVector)

    val options = listOf(
        TypeOption("BLUETOOTH", "Bluetooth",  "ESC/POS thermal printer",   Icons.Outlined.Bluetooth, Icons.Filled.Bluetooth),
        TypeOption("WIFI",      "Wi-Fi",      "Network printer (IP-based)", Icons.Outlined.Wifi,      Icons.Filled.Wifi),
        TypeOption("NETWORK",   "LAN / USB",  "Same network TCP printing",  Icons.Outlined.Wifi,      Icons.Filled.Wifi),
        TypeOption("BROWSER",   "Browser",    "System print dialog",        Icons.Outlined.Language,  Icons.Filled.Language)
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { opt ->
            val isSelected = selected == opt.key
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) VioletLight else Color(0xFFF8FAFC))
                    .border(
                        width  = if (isSelected) 2.dp else 1.dp,
                        color  = if (isSelected) VioletPrimary else CardBorder,
                        shape  = RoundedCornerShape(12.dp)
                    )
                    .clickable { onSelect(opt.key) }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isSelected) opt.selectedIcon else opt.icon,
                    contentDescription = opt.label,
                    tint = if (isSelected) VioletPrimary else MutedText,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(opt.label, fontWeight = FontWeight.SemiBold, color = if (isSelected) VioletPrimary else SlateText)
                    Text(opt.sublabel, style = MaterialTheme.typography.bodySmall, color = MutedText)
                }
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(VioletPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                    }
                } else {
                    Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = MutedText, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
private fun BluetoothSection(
    selectedName: String,
    pairedDevices: List<BluetoothDeviceInfo>,
    onRefresh: () -> Unit,
    onSelectDevice: (BluetoothDeviceInfo) -> Unit
) {
    PrinterCard(title = "Bluetooth Printer") {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Paired Devices", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = SlateText)
            IconButton(onClick = onRefresh) {
                Icon(Icons.Filled.BluetoothSearching, contentDescription = "Refresh", tint = VioletPrimary)
            }
        }

        if (pairedDevices.isEmpty()) {
            Text(
                "No paired Bluetooth devices found. Pair your printer in Android Settings > Bluetooth first.",
                style = MaterialTheme.typography.bodySmall,
                color = MutedText,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        } else {
            Spacer(Modifier.height(4.dp))
            pairedDevices.forEach { device ->
                val isSelected = selectedName == device.name
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) VioletLight else Color.Transparent)
                        .border(1.dp, if (isSelected) VioletPrimary else CardBorder, RoundedCornerShape(10.dp))
                        .clickable { onSelectDevice(device) }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(device.name, fontWeight = FontWeight.Medium, color = if (isSelected) VioletPrimary else SlateText)
                        Text(device.address, style = MaterialTheme.typography.bodySmall, color = MutedText)
                    }
                    if (isSelected) {
                        Icon(Icons.Filled.Check, contentDescription = null, tint = VioletPrimary, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun NetworkSection(
    ip: String,
    port: Int,
    onIpChange: (String) -> Unit,
    onPortChange: (Int) -> Unit
) {
    PrinterCard(title = "Network / Wi-Fi Printer") {
        OutlinedTextField(
            value = ip,
            onValueChange = onIpChange,
            label = { Text("Printer IP Address") },
            placeholder = { Text("e.g. 192.168.1.100") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = VioletPrimary,
                focusedLabelColor  = VioletPrimary
            )
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = port.toString(),
            onValueChange = { it.toIntOrNull()?.let(onPortChange) },
            label = { Text("Port") },
            placeholder = { Text("9100") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = VioletPrimary,
                focusedLabelColor  = VioletPrimary
            )
        )
    }
}

@Composable
private fun PaperWidthChip(width: String, label: String, selected: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) VioletPrimary else Color(0xFFF1F5F9))
            .border(1.5.dp, if (selected) VioletPrimary else CardBorder, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            width,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 18.sp,
            color = if (selected) Color.White else SlateText
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) Color.White.copy(alpha = 0.8f) else MutedText
        )
    }
}

@Composable
private fun AutoPrintRow(label: String, sublabel: String, checked: Boolean, onChecked: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
            Text(label, fontWeight = FontWeight.Medium, color = SlateText, fontSize = 14.sp)
            Text(sublabel, style = MaterialTheme.typography.bodySmall, color = MutedText)
        }
        Switch(
            checked = checked,
            onCheckedChange = onChecked,
            colors = SwitchDefaults.colors(checkedThumbColor = VioletPrimary, checkedTrackColor = VioletLight)
        )
    }
}
