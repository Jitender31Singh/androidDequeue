package com.appvendor.feature_menu_items.presentation.import_menu

import android.net.Uri
import com.appvendor.feature_menu_items.domain.model.ExtractedMenuItem

enum class ImportStep {
    UPLOAD,
    LOADING,
    REVIEW
}

data class EditableExtractedItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val price: String, // Keep as string for text field editability
    val categoryName: String
)

data class MenuImportState(
    val step: ImportStep = ImportStep.UPLOAD,
    val selectedImageUri: Uri? = null,
    val extractedItems: List<EditableExtractedItem> = emptyList(),
    val isExtracting: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val availableCategories: List<String> = emptyList() // For dropdowns
)
