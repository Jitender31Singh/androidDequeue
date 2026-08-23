package com.appvendor.feature_menu_items.presentation.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.appvendor.feature_categories.domain.model.Category
import com.appvendor.feature_customizations.domain.model.CustomizationGroup
import com.appvendor.feature_menu_items.domain.model.MenuItem
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuItemFormFullScreen(
    initialData: MenuItem?,
    categories: List<Category>,
    customizations: List<CustomizationGroup>,
    onDismiss: () -> Unit,
    onSave: (String?, String, String?, Double, String, Int, Uri?, String?, List<String>) -> Unit,
    isUpdating: Boolean,
    isUploadingImage: Boolean
) {
    var name by remember { mutableStateOf(initialData?.name ?: "") }
    var description by remember { mutableStateOf(initialData?.description ?: "") }
    var price by remember { mutableStateOf(initialData?.price?.toString() ?: "") }
    var prepTime by remember { mutableStateOf(initialData?.preparationTime?.toString() ?: "15") }
    var selectedCategoryId by remember { mutableStateOf(initialData?.categoryId ?: categories.firstOrNull()?.id ?: "") }
    
    val currentImageUrl = initialData?.image
    var newImageUri by remember { mutableStateOf<Uri?>(null) }
    
    var selectedCustomizations by remember { mutableStateOf(initialData?.customizationGroups?.map { it.id }?.toSet() ?: emptySet()) }

    var showCategorySheet by remember { mutableStateOf(false) }
    var showCustomizationSheet by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                newImageUri = uri
            }
        }
    )

    Dialog(
        onDismissRequest = { if (!isUpdating && !isUploadingImage) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text(if (initialData == null) "New Item" else "Edit Item", fontWeight = FontWeight.SemiBold) },
                        navigationIcon = {
                            IconButton(onClick = onDismiss, enabled = !isUpdating && !isUploadingImage) {
                                Icon(Icons.Default.Close, contentDescription = "Close")
                            }
                        },
                        actions = {
                            Button(
                                onClick = {
                                    onSave(
                                        initialData?.id,
                                        name.trim(),
                                        description.trim().ifEmpty { null },
                                        price.toDoubleOrNull() ?: 0.0,
                                        selectedCategoryId,
                                        prepTime.toIntOrNull() ?: 15,
                                        newImageUri,
                                        currentImageUrl,
                                        selectedCustomizations.toList()
                                    )
                                },
                                enabled = name.isNotBlank() && price.isNotBlank() && selectedCategoryId.isNotBlank() && !isUpdating && !isUploadingImage,
                                modifier = Modifier.padding(end = 8.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Save")
                            }
                        }
                    )
                }
            ) { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                ) {
                    if (isUploadingImage || isUpdating) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        Text(
                            text = if (isUploadingImage) "Uploading image..." else "Saving item...",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }

                    // Gorgeous Photo Header Section
                    val hasImage = newImageUri != null || !currentImageUrl.isNullOrBlank()
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                            .clickable {
                                imagePickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (newImageUri != null) {
                            AsyncImage(
                                model = newImageUri,
                                contentDescription = "New Image",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else if (!currentImageUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = currentImageUrl,
                                contentDescription = "Current Image",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    modifier = Modifier.size(64.dp),
                                    shadowElevation = 4.dp
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    }
                                }
                                Spacer(Modifier.height(12.dp))
                                Text("Tap to upload photo", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
                            }
                        }
                        
                        // Edit overlay if image exists
                        if (hasImage) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(16.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    shadowElevation = 4.dp
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text("Change", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    // Form Fields
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Item Name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            textStyle = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            OutlinedTextField(
                                value = price,
                                onValueChange = { price = it.filter { char -> char.isDigit() || char == '.' } },
                                label = { Text("Price (₹)") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                shape = RoundedCornerShape(12.dp),
                                leadingIcon = { Text("₹", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(start = 12.dp)) }
                            )
                            OutlinedTextField(
                                value = prepTime,
                                onValueChange = { prepTime = it.filter { char -> char.isDigit() } },
                                label = { Text("Prep (min)") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(12.dp),
                                leadingIcon = { Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(20.dp)) }
                            )
                        }

                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Description (Optional)") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 4,
                            minLines = 2,
                            shape = RoundedCornerShape(12.dp),
                            placeholder = { Text("e.g. A delicious blend of spices and fresh ingredients...") }
                        )

                        // Category Selector
                        Column {
                            Text("Category", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(8.dp))
                            val selectedCategoryName = categories.find { it.id == selectedCategoryId }?.name ?: "Select Category"
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showCategorySheet = true },
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                color = MaterialTheme.colorScheme.surface
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(selectedCategoryName, style = MaterialTheme.typography.bodyLarge)
                                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }

                        // Customizations Selector
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Customizations", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                    Text("Add-ons & variants", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                FilledTonalButton(onClick = { showCustomizationSheet = true }) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Add")
                                }
                            }
                            
                            Spacer(Modifier.height(12.dp))
                            
                            if (selectedCustomizations.isNotEmpty()) {
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    val selectedGroups = customizations.filter { selectedCustomizations.contains(it.id) }
                                    selectedGroups.forEach { group ->
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(16.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(group.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                                                    Text(
                                                        "${group.selectionType.name.lowercase(Locale.getDefault()).replace("_", " ").replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }} · ${group.options.size} options",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                                IconButton(onClick = { selectedCustomizations = selectedCustomizations - group.id }) {
                                                    Icon(Icons.Default.Close, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error)
                                                }
                                            }
                                        }
                                    }
                                }
                            } else {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                                    modifier = Modifier.fillMaxWidth(),
                                    color = Color.Transparent
                                ) {
                                    Text(
                                        text = "No customizations added",
                                        modifier = Modifier.padding(16.dp),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }
                        
                        Spacer(Modifier.height(80.dp))
                    }
                }
            }
        }
    }

    if (showCategorySheet) {
        ModalBottomSheet(onDismissRequest = { showCategorySheet = false }) {
            Column(modifier = Modifier.padding(bottom = 32.dp)) {
                Text(
                    "Select Category",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(16.dp)
                )
                LazyColumn {
                    items(categories) { category ->
                        ListItem(
                            headlineContent = { Text(category.name, fontWeight = if (category.id == selectedCategoryId) FontWeight.Bold else FontWeight.Normal) },
                            trailingContent = {
                                if (category.id == selectedCategoryId) {
                                    Icon(Icons.Default.Check, contentDescription = "Selected", tint = MaterialTheme.colorScheme.primary)
                                }
                            },
                            modifier = Modifier.clickable {
                                selectedCategoryId = category.id
                                showCategorySheet = false
                            }
                        )
                    }
                }
            }
        }
    }

    if (showCustomizationSheet) {
        ModalBottomSheet(onDismissRequest = { showCustomizationSheet = false }) {
            Column(modifier = Modifier.padding(bottom = 32.dp)) {
                Text(
                    "Select Customizations",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(16.dp)
                )
                LazyColumn {
                    items(customizations) { group ->
                        ListItem(
                            headlineContent = { Text(group.name, fontWeight = FontWeight.Medium) },
                            supportingContent = { Text("${group.options.size} options") },
                            trailingContent = {
                                Checkbox(
                                    checked = selectedCustomizations.contains(group.id),
                                    onCheckedChange = { isChecked ->
                                        selectedCustomizations = if (isChecked) {
                                            selectedCustomizations + group.id
                                        } else {
                                            selectedCustomizations - group.id
                                        }
                                    }
                                )
                            },
                            modifier = Modifier.clickable {
                                selectedCustomizations = if (selectedCustomizations.contains(group.id)) {
                                    selectedCustomizations - group.id
                                } else {
                                    selectedCustomizations + group.id
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
