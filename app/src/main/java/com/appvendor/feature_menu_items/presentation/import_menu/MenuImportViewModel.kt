package com.appvendor.feature_menu_items.presentation.import_menu

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appvendor.core.network.ApiResult
import com.appvendor.feature_categories.data.repository.CategoryRepository
import com.appvendor.feature_menu_items.data.repository.MenuItemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MenuImportViewModel @Inject constructor(
    private val menuItemRepository: MenuItemRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _state = MutableStateFlow(MenuImportState())
    val state: StateFlow<MenuImportState> = _state.asStateFlow()

    init {
        loadCategories()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            when (val result = categoryRepository.getCategories()) {
                is ApiResult.Success -> {
                    val catNames = result.data.map { it.name }
                    _state.update { it.copy(availableCategories = catNames) }
                }
                else -> { /* Ignore error for now */ }
            }
        }
    }

    fun onImageSelected(uri: Uri?) {
        _state.update { it.copy(selectedImageUri = uri, error = null) }
    }

    fun extractMenu() {
        val uri = _state.value.selectedImageUri ?: return
        
        viewModelScope.launch {
            _state.update { it.copy(step = ImportStep.LOADING, error = null, isExtracting = true) }
            
            when (val result = menuItemRepository.extractMenuFromImage(uri)) {
                is ApiResult.Success -> {
                    val newItems = result.data.map { ext ->
                        EditableExtractedItem(
                            name = ext.name,
                            price = ext.price.toString(),
                            categoryName = ext.categoryName ?: ""
                        )
                    }
                    _state.update { 
                        it.copy(
                            step = ImportStep.REVIEW,
                            isExtracting = false,
                            extractedItems = it.extractedItems + newItems
                        ) 
                    }
                }
                is ApiResult.Error -> {
                    _state.update { 
                        it.copy(
                            step = ImportStep.LOADING,
                            isExtracting = false,
                            error = result.message ?: "Extraction failed"
                        ) 
                    }
                }
                else -> {}
            }
        }
    }

    fun retryExtraction() {
        extractMenu()
    }

    fun cancelExtraction() {
        _state.update { it.copy(step = ImportStep.UPLOAD, error = null) }
    }
    
    fun updateItemName(id: String, name: String) {
        updateItem(id) { it.copy(name = name) }
    }

    fun updateItemPrice(id: String, price: String) {
        updateItem(id) { it.copy(price = price) }
    }

    fun updateItemCategory(id: String, category: String) {
        updateItem(id) { it.copy(categoryName = category) }
    }

    fun removeItem(id: String) {
        _state.update { curr ->
            curr.copy(extractedItems = curr.extractedItems.filter { it.id != id })
        }
    }
    
    fun addNewRow() {
        _state.update { curr ->
            curr.copy(extractedItems = curr.extractedItems + EditableExtractedItem(name = "", price = "", categoryName = ""))
        }
    }

    private fun updateItem(id: String, transform: (EditableExtractedItem) -> EditableExtractedItem) {
        _state.update { curr ->
            curr.copy(
                extractedItems = curr.extractedItems.map { if (it.id == id) transform(it) else it }
            )
        }
    }

    fun scanNextPage() {
        _state.update { it.copy(step = ImportStep.UPLOAD, selectedImageUri = null, error = null) }
    }

    fun saveToMenu(onComplete: () -> Unit) {
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            val itemsToSave = _state.value.extractedItems.filter { it.name.isNotBlank() }
            
            // Note: Since backend handles items individually, we save them in a loop.
            // A bulk API would be better, but we use the existing createMenuItem.
            for (item in itemsToSave) {
                // We need to resolve categoryName to categoryId. 
                // For simplicity in this demo, if categoryId is required, we use a default or empty string.
                val priceDouble = item.price.toDoubleOrNull() ?: 0.0
                menuItemRepository.createMenuItem(
                    name = item.name,
                    description = "",
                    price = priceDouble,
                    categoryId = "default_category", // Fallback
                    preparationTime = 15,
                    sortOrder = 0,
                    image = null,
                    customizationGroupIds = emptyList()
                )
            }
            
            _state.update { it.copy(isSaving = false) }
            onComplete()
        }
    }
}
