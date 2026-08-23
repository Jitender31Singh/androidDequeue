package com.appvendor.feature_customizations.presentation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.appvendor.feature_customizations.domain.model.CustomizationGroup
import com.appvendor.feature_customizations.domain.model.CustomizationOption
import com.appvendor.feature_customizations.domain.model.SelectionType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomizationsScreen(
    viewModel: CustomizationsViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var groupToDelete by remember { mutableStateOf<CustomizationGroup?>(null) }

    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text("Customizations", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, fontSize = 22.sp) 
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.openCreateForm() },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                icon = { Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(20.dp)) },
                text = { Text("New Customization", fontWeight = FontWeight.SemiBold, fontSize = 14.sp) },
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 2.dp)
            )
        },
        modifier = modifier
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (state.isLoading) {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(4) {
                        CustomizationSkeletonCard()
                    }
                }
            } else if (state.customizations.isEmpty()) {
                EmptyCustomizationState { viewModel.openCreateForm() }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(state.customizations, key = { it.id }) { group ->
                        CustomizationCard(
                            group = group,
                            onEdit = { viewModel.openEditForm(group) },
                            onDelete = { groupToDelete = group }
                        )
                    }
                }
            }

            state.error?.let { error ->
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    action = { TextButton(onClick = { viewModel.dismissError() }) { Text("Dismiss") } }
                ) { Text(error) }
            }
        }
    }

    if (state.isFormOpen) {
        CustomizationFormFullScreen(
            initialData = state.editingCustomization,
            onDismiss = { viewModel.closeForm() },
            onSave = { viewModel.saveCustomization(it) },
            isUpdating = state.isUpdating
        )
    }

    groupToDelete?.let { group ->
        DeleteConfirmationDialog(
            groupName = group.name,
            onConfirm = { 
                viewModel.deleteCustomization(group.id)
                groupToDelete = null 
            },
            onDismiss = { groupToDelete = null }
        )
    }
}

@Composable
fun EmptyCustomizationState(onAdd: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Tune,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No customizations yet",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Add choices like size, sugar, toppings or extras to your menu items.",
            style = MaterialTheme.typography.bodyMedium,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onAdd, shape = RoundedCornerShape(12.dp)) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Create Customization", fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun CustomizationSkeletonCard() {
    val infiniteTransition = rememberInfiniteTransition(label = "skeleton")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(animation = tween(1000, easing = LinearEasing), repeatMode = RepeatMode.Reverse),
        label = "alpha"
    )
    val color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha)

    Card(
        modifier = Modifier.fillMaxWidth().height(84.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Row(modifier = Modifier.fillMaxSize().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)).background(color))
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Box(modifier = Modifier.width(120.dp).height(18.dp).clip(RoundedCornerShape(4.dp)).background(color))
                Spacer(modifier = Modifier.height(8.dp))
                Box(modifier = Modifier.width(80.dp).height(14.dp).clip(RoundedCornerShape(4.dp)).background(color))
            }
        }
    }
}

fun getCustomizationIcon(name: String): ImageVector {
    val lower = name.lowercase()
    return when {
        lower.contains("size") -> Icons.Default.FormatSize
        lower.contains("sugar") || lower.contains("sweet") -> Icons.Default.Opacity
        lower.contains("extra") || lower.contains("add") -> Icons.Default.Extension
        lower.contains("type") || lower.contains("base") || lower.contains("crust") -> Icons.Default.Category
        else -> Icons.Default.Tune
    }
}

@Composable
fun CustomizationCard(
    group: CustomizationGroup,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }

    val icon = getCustomizationIcon(group.name)
    val rotation by animateFloatAsState(targetValue = if (expanded) 180f else 0f, label = "rotation")

    Card(
        modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 8.dp, bottom = 16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = group.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    val typeText = if (group.selectionType == SelectionType.SINGLE) "Single choice" else "Multiple choice"
                    Text(
                        text = "$typeText · ${group.options.size} options",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Box {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Options", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }, modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
                        DropdownMenuItem(
                            text = { Text("Edit") },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(20.dp)) },
                            onClick = { menuExpanded = false; onEdit() }
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        DropdownMenuItem(
                            text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp)) },
                            onClick = { menuExpanded = false; onDelete() }
                        )
                    }
                }
                
                Icon(
                    Icons.Default.KeyboardArrowDown,
                    contentDescription = "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(end = 8.dp).size(24.dp).graphicsLayer(rotationZ = rotation)
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(if (group.required) "● Required" else "○ Optional", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = if (group.required) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                        if (group.selectionType == SelectionType.MULTIPLE) {
                            Text("Select ${group.minSelection} to ${group.maxSelection}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    group.options.forEach { opt ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(opt.name, style = MaterialTheme.typography.bodyMedium, fontSize = 14.sp)
                            val priceStr = if (opt.additionalPrice > 0) "+₹${opt.additionalPrice}" else "₹0"
                            Text(priceStr, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (opt != group.options.last()) {
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f))
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomizationFormFullScreen(
    initialData: CustomizationGroup?,
    onDismiss: () -> Unit,
    onSave: (CustomizationGroup) -> Unit,
    isUpdating: Boolean
) {
    var name by remember { mutableStateOf(initialData?.name ?: "") }
    var selectionType by remember { mutableStateOf(initialData?.selectionType ?: SelectionType.SINGLE) }
    var required by remember { mutableStateOf(initialData?.required ?: false) }
    var minSelection by remember { mutableStateOf(initialData?.minSelection?.toString() ?: "1") }
    var maxSelection by remember { mutableStateOf(initialData?.maxSelection?.toString() ?: "1") }
    var options by remember { mutableStateOf<List<CustomizationOption>>(initialData?.options ?: emptyList()) }

    var editingOptionIndex by remember { mutableStateOf<Int?>(null) }
    var isAddingOption by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = { if (!isUpdating) onDismiss() }, properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = true)) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text(if (initialData == null) "New Customization" else "Edit Customization", fontSize = 20.sp, fontWeight = FontWeight.SemiBold) },
                        navigationIcon = {
                            IconButton(onClick = onDismiss, enabled = !isUpdating) { Icon(Icons.Default.Close, contentDescription = "Close") }
                        },
                        actions = {
                            TextButton(
                                onClick = {
                                    onSave(CustomizationGroup(
                                        id = initialData?.id ?: "",
                                        name = name.trim(),
                                        selectionType = selectionType,
                                        required = required,
                                        minSelection = minSelection.toIntOrNull() ?: 0,
                                        maxSelection = maxSelection.toIntOrNull() ?: 1,
                                        options = options
                                    ))
                                },
                                enabled = name.isNotBlank() && options.isNotEmpty() && !isUpdating
                            ) {
                                if (isUpdating) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                else Text("Save", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
                    )
                }
            ) { paddingValues ->
                Column(
                    modifier = Modifier.fillMaxSize().padding(paddingValues).verticalScroll(rememberScrollState()).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(32.dp)
                ) {
                    // SECTION 1
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Customization details", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        OutlinedTextField(
                            value = name, onValueChange = { name = it },
                            label = { Text("Name") },
                            placeholder = { Text("e.g. Size, Sugar, Toppings") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    // SECTION 2
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text("Selection behavior", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        
                        // Segmented Control
                        Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surfaceVariant).padding(4.dp)) {
                            val singleBg = if (selectionType == SelectionType.SINGLE) MaterialTheme.colorScheme.surface else Color.Transparent
                            val singleShadow = if (selectionType == SelectionType.SINGLE) 1.dp else 0.dp
                            val multiBg = if (selectionType == SelectionType.MULTIPLE) MaterialTheme.colorScheme.surface else Color.Transparent
                            val multiShadow = if (selectionType == SelectionType.MULTIPLE) 1.dp else 0.dp
                            
                            Surface(modifier = Modifier.weight(1f).clickable { selectionType = SelectionType.SINGLE }, shape = RoundedCornerShape(8.dp), color = singleBg, shadowElevation = singleShadow) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 10.dp)) {
                                    Text("Single choice", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                }
                            }
                            Surface(modifier = Modifier.weight(1f).clickable { selectionType = SelectionType.MULTIPLE }, shape = RoundedCornerShape(8.dp), color = multiBg, shadowElevation = multiShadow) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 10.dp)) {
                                    Text("Multiple choice", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                }
                            }
                        }

                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Customer must select an option", fontSize = 15.sp)
                            Switch(checked = required, onCheckedChange = { required = it })
                        }

                        AnimatedVisibility(visible = selectionType == SelectionType.MULTIPLE) {
                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                OutlinedTextField(
                                    value = minSelection, onValueChange = { minSelection = it.filter { c -> c.isDigit() } },
                                    label = { Text("Min Select") }, modifier = Modifier.weight(1f), singleLine = true, shape = RoundedCornerShape(12.dp)
                                )
                                OutlinedTextField(
                                    value = maxSelection, onValueChange = { maxSelection = it.filter { c -> c.isDigit() } },
                                    label = { Text("Max Select") }, modifier = Modifier.weight(1f), singleLine = true, shape = RoundedCornerShape(12.dp)
                                )
                            }
                        }
                    }

                    // SECTION 3
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Options", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            Box(modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surfaceVariant).padding(horizontal = 8.dp, vertical = 2.dp)) {
                                Text("${options.size}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }

                        if (options.isEmpty()) {
                            Text("No options added yet.", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 16.dp))
                        } else {
                            options.forEachIndexed { index, option ->
                                Card(
                                    modifier = Modifier.fillMaxWidth().clickable { editingOptionIndex = index },
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                                ) {
                                    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Menu, contentDescription = "Drag", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(option.name, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                                            val priceStr = if (option.additionalPrice > 0) "+₹${option.additionalPrice}" else "₹0"
                                            Text(priceStr, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                                        }
                                        Icon(Icons.Default.ChevronRight, contentDescription = "Edit", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }

                        OutlinedButton(
                            onClick = { isAddingOption = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Add Option", fontWeight = FontWeight.SemiBold)
                        }
                    }
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }

    if (isAddingOption || editingOptionIndex != null) {
        val isEdit = editingOptionIndex != null
        val targetOption = if (isEdit) options[editingOptionIndex!!] else CustomizationOption(name = "", additionalPrice = 0.0)
        
        OptionEditorSheet(
            initialOption = targetOption,
            isEdit = isEdit,
            onDismiss = { 
                isAddingOption = false
                editingOptionIndex = null 
            },
            onSave = { newOpt ->
                val updated = options.toMutableList()
                if (isEdit) updated[editingOptionIndex!!] = newOpt else updated.add(newOpt)
                options = updated
                isAddingOption = false
                editingOptionIndex = null
            },
            onDelete = {
                if (isEdit) {
                    val updated = options.toMutableList()
                    updated.removeAt(editingOptionIndex!!)
                    options = updated
                }
                isAddingOption = false
                editingOptionIndex = null
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OptionEditorSheet(
    initialOption: CustomizationOption,
    isEdit: Boolean,
    onDismiss: () -> Unit,
    onSave: (CustomizationOption) -> Unit,
    onDelete: () -> Unit
) {
    var name by remember { mutableStateOf(initialOption.name) }
    var price by remember { mutableStateOf(if (initialOption.additionalPrice > 0) initialOption.additionalPrice.toString().removeSuffix(".0") else "") }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState, containerColor = MaterialTheme.colorScheme.surface) {
        Column(modifier = Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, bottom = 48.dp, top = 8.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Text(if (isEdit) "Edit option" else "Add option", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            
            OutlinedTextField(
                value = name, onValueChange = { name = it },
                label = { Text("Option name") }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(12.dp)
            )
            
            OutlinedTextField(
                value = price, onValueChange = { price = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("Additional price (+₹)") }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(12.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                if (isEdit) {
                    TextButton(onClick = onDelete, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                        Text("Delete", fontWeight = FontWeight.SemiBold)
                    }
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TextButton(onClick = onDismiss) { Text("Cancel", fontWeight = FontWeight.SemiBold) }
                    Button(
                        onClick = { onSave(CustomizationOption(id = initialOption.id, name = name.trim(), additionalPrice = price.toDoubleOrNull() ?: 0.0)) },
                        enabled = name.isNotBlank(), shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Save", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun DeleteConfirmationDialog(groupName: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete \"$groupName\"?", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
        text = { Text("This customization may be used by menu items. Are you sure you want to delete it?", fontSize = 14.sp) },
        confirmButton = {
            Button(onClick = onConfirm, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error), shape = RoundedCornerShape(10.dp)) { Text("Delete") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp)
    )
}
