package com.appvendor.feature_menu_items.presentation.import_menu

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.appvendor.feature_menu_items.presentation.import_menu.components.ImportLoadingState
import com.appvendor.feature_menu_items.presentation.import_menu.components.ImportReviewStep
import com.appvendor.feature_menu_items.presentation.import_menu.components.ImportUploadStep

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuImportFlow(
    onNavigateBack: () -> Unit,
    viewModel: MenuImportViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Import Menu from Image", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (state.step) {
                ImportStep.UPLOAD -> {
                    ImportUploadStep(
                        selectedImageUri = state.selectedImageUri,
                        onImageSelected = viewModel::onImageSelected,
                        onExtract = viewModel::extractMenu
                    )
                }
                ImportStep.LOADING -> {
                    ImportLoadingState(
                        isExtracting = state.isExtracting,
                        error = state.error,
                        onRetry = viewModel::retryExtraction,
                        onCancel = viewModel::cancelExtraction
                    )
                }
                ImportStep.REVIEW -> {
                    ImportReviewStep(
                        state = state,
                        onNameChange = viewModel::updateItemName,
                        onPriceChange = viewModel::updateItemPrice,
                        onCategoryChange = viewModel::updateItemCategory,
                        onRemove = viewModel::removeItem,
                        onAddRow = viewModel::addNewRow,
                        onScanNextPage = viewModel::scanNextPage,
                        onSave = {
                            viewModel.saveToMenu {
                                onNavigateBack() // Go back when finished saving
                            }
                        },
                        onCancel = viewModel::cancelExtraction
                    )
                }
            }
            
            // Progress Indicator at top
            LinearProgressIndicator(
                progress = when (state.step) {
                    ImportStep.UPLOAD -> 0.33f
                    ImportStep.LOADING -> 0.66f
                    ImportStep.REVIEW -> 1f
                },
                modifier = Modifier.fillMaxWidth().height(4.dp),
                color = MaterialTheme.colorScheme.tertiary
            )
        }
    }
}
