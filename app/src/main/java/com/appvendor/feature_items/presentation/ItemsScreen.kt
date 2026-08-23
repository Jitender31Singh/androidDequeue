package com.appvendor.feature_items.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.appvendor.feature_items.domain.model.Item
import com.appvendor.feature_items.presentation.components.AddCategoryDialog
import com.appvendor.feature_items.presentation.components.AddItemSheet
import com.appvendor.feature_items.presentation.components.CategoryCard
import com.appvendor.feature_items.presentation.components.ItemCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemsScreen(
    viewModel: ItemsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var showAddItemSheet by remember { mutableStateOf(false) }
    var itemToEdit by remember { mutableStateOf<Item?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Menu Items") },
                actions = {
                    IconButton(onClick = { showAddCategoryDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Category")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            if (state.selectedCategoryId != null) {
                FloatingActionButton(
                    onClick = { 
                        itemToEdit = null
                        showAddItemSheet = true 
                    }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Item")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (state.error != null) {
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    action = {
                        TextButton(onClick = { viewModel.clearError() }) {
                            Text("Dismiss", color = MaterialTheme.colorScheme.inversePrimary)
                        }
                    }
                ) {
                    Text(state.error!!)
                }
            }

            // Categories Row
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = state.selectedCategoryId == null,
                        onClick = { viewModel.selectCategory(null) },
                        label = { Text("All") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
                items(state.categories) { category ->
                    CategoryCard(
                        category = category,
                        isSelected = state.selectedCategoryId == category.id,
                        onClick = { viewModel.selectCategory(category.id) }
                    )
                }
            }

            if (state.isLoading && state.items.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (state.items.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "No items found.\nAdd a category first, then add items.",
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 300.dp),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(8.dp)
                ) {
                    items(state.items) { item ->
                        ItemCard(
                            item = item,
                            onEditClick = {
                                itemToEdit = item
                                showAddItemSheet = true
                            },
                            onDeleteClick = { viewModel.deleteItem(item.id) }
                        )
                    }
                }
            }
        }

        if (showAddCategoryDialog) {
            AddCategoryDialog(
                onDismiss = { showAddCategoryDialog = false },
                onSave = { category ->
                    viewModel.createCategory(category)
                    showAddCategoryDialog = false
                }
            )
        }

        if (showAddItemSheet) {
            val selectedCategory = state.categories.find { it.id == state.selectedCategoryId }
                ?: state.categories.firstOrNull { it.id == itemToEdit?.categoryId }
                ?: state.categories.firstOrNull()

            if (selectedCategory != null) {
                AddItemSheet(
                    category = selectedCategory,
                    itemToEdit = itemToEdit,
                    onDismiss = { 
                        showAddItemSheet = false 
                        itemToEdit = null
                    },
                    onSave = { item ->
                        if (itemToEdit == null) {
                            viewModel.createItem(item)
                        } else {
                            viewModel.updateItem(item)
                        }
                    }
                )
            }
        }
    }
}
