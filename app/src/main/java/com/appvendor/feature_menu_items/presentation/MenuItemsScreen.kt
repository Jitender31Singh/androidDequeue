package com.appvendor.feature_menu_items.presentation

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.appvendor.feature_categories.domain.model.Category
import com.appvendor.feature_customizations.domain.model.CustomizationGroup
import com.appvendor.feature_menu_items.domain.model.MenuItem
import java.util.Collections
import com.appvendor.feature_menu_items.presentation.components.MenuItemCard
import com.appvendor.feature_menu_items.presentation.components.MenuItemFormFullScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuItemsScreen(
    viewModel: MenuItemsViewModel,
    onImportMenuClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Menu", fontWeight = FontWeight.Bold)
                        Text("Manage your menu items", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                actions = {
                    TextButton(onClick = onImportMenuClick) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = "Import from Image", tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Import AI", color = MaterialTheme.colorScheme.tertiary)
                    }
                    TextButton(onClick = { viewModel.openCreateForm() }) {
                        Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Add")
                    }
                }
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
            Column(modifier = Modifier.fillMaxSize()) {
                // Search & Filter Section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    OutlinedTextField(
                        value = state.searchQuery,
                        onValueChange = { viewModel.updateSearchQuery(it) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Search menu items...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        )
                    )
                    
                    Spacer(Modifier.height(16.dp))
                    
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            FilterChip(
                                selected = state.filterCategoryId == null,
                                onClick = { viewModel.updateFilterCategory(null) },
                                label = { Text("All") }
                            )
                        }
                        items(state.categories) { category ->
                            FilterChip(
                                selected = state.filterCategoryId == category.id,
                                onClick = { viewModel.updateFilterCategory(category.id) },
                                label = { Text(category.name) }
                            )
                        }
                    }
                }
                
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                if (state.isLoading) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (state.filteredItems.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.RestaurantMenu, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                            Spacer(Modifier.height(16.dp))
                            Text("Your menu is empty", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(8.dp))
                            Text("Add your first menu item to\nstart building your menu.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                            Spacer(Modifier.height(24.dp))
                            Button(onClick = { viewModel.openCreateForm() }) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Add menu item")
                            }
                            Spacer(Modifier.height(16.dp))
                            OutlinedButton(
                                onClick = onImportMenuClick,
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.tertiary)
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Import AI Menu")
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        itemsIndexed(state.filteredItems, key = { _, item -> item.id }) { index, item ->
                            MenuItemCard(
                                item = item,
                                canReorder = state.filterCategoryId != null,
                                isFirst = index == 0,
                                isLast = index == state.filteredItems.size - 1,
                                onMoveUp = {
                                    val newList = state.filteredItems.toMutableList()
                                    Collections.swap(newList, index, index - 1)
                                    viewModel.updateSortOrder(newList)
                                },
                                onMoveDown = {
                                    val newList = state.filteredItems.toMutableList()
                                    Collections.swap(newList, index, index + 1)
                                    viewModel.updateSortOrder(newList)
                                },
                                onEdit = { viewModel.openEditForm(item) },
                                onDelete = { viewModel.deleteMenuItem(item.id) },
                                onToggleAvailability = { viewModel.toggleAvailability(item.id, it) },
                                onToggleVisibility = { viewModel.toggleVisibility(item.id, it) }
                            )
                        }
                    }
                }
            }

            state.error?.let { error ->
                Snackbar(
                    modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
                    action = { TextButton(onClick = { viewModel.dismissError() }) { Text("Dismiss") } }
                ) { Text(error) }
            }
        }
    }

    if (state.isFormOpen) {
        MenuItemFormFullScreen(
            initialData = state.editingItem,
            categories = state.categories,
            customizations = state.customizations,
            onDismiss = { viewModel.closeForm() },
            onSave = { id, name, desc, price, catId, prep, newImageUri, currentImageUrl, customIds ->
                viewModel.saveMenuItem(id, name, desc, price, catId, prep, newImageUri, currentImageUrl, customIds)
            },
            isUpdating = state.isUpdating,
            isUploadingImage = state.isUploadingImage
        )
    }
}



@Composable
fun StatusChip(
    isActive: Boolean,
    activeText: String,
    inactiveText: String,
    onClick: () -> Unit
) {
    val backgroundColor = if (isActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
    val contentColor = if (isActive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
    val icon = if (isActive) Icons.Default.CheckCircle else Icons.Default.Cancel

    Surface(
        color = backgroundColor,
        shape = CircleShape,
        modifier = Modifier
            .clickable(onClick = onClick)
            .height(28.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = contentColor
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = if (isActive) activeText else inactiveText,
                style = MaterialTheme.typography.labelSmall,
                color = contentColor,
                fontWeight = FontWeight.Medium
            )
        }
    }
}


