package com.appvendor.feature_menu_items.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appvendor.core.network.ApiResult
import com.appvendor.feature_categories.data.repository.CategoryRepository
import com.appvendor.feature_customizations.data.repository.CustomizationRepository
import com.appvendor.feature_menu_items.data.repository.MenuItemRepository
import com.appvendor.feature_menu_items.domain.model.MenuItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MenuItemsViewModel @Inject constructor(
    private val repository: MenuItemRepository,
    private val categoryRepository: CategoryRepository,
    private val customizationRepository: CustomizationRepository
) : ViewModel() {

    private val _state = MutableStateFlow(MenuItemsState())
    val state: StateFlow<MenuItemsState> = _state.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = it.items.isEmpty(), error = null) }
            
            // Load Categories for filter and form
            when (val catResult = categoryRepository.getCategories()) {
                is ApiResult.Success -> _state.update { it.copy(categories = catResult.data) }
                else -> {}
            }
            
            // Load Customizations for form
            when (val custResult = customizationRepository.getCustomizations()) {
                is ApiResult.Success -> _state.update { it.copy(customizations = custResult.data) }
                else -> {}
            }

            // Load Menu Items
            when (val result = repository.getMenuItems()) {
                is ApiResult.Success -> {
                    _state.update { it.copy(isLoading = false, items = result.data.sortedBy { item -> item.sortOrder }) }
                    applyFilters()
                }
                is ApiResult.Error -> _state.update { it.copy(isLoading = false, error = result.message) }
                is ApiResult.Loading -> {}
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isRefreshing = true, error = null) }
            
            // Refresh Categories for filter and form
            when (val catResult = categoryRepository.getCategories()) {
                is ApiResult.Success -> _state.update { it.copy(categories = catResult.data) }
                else -> {}
            }
            
            // Refresh Customizations for form
            when (val custResult = customizationRepository.getCustomizations()) {
                is ApiResult.Success -> _state.update { it.copy(customizations = custResult.data) }
                else -> {}
            }

            when (val result = repository.getMenuItems()) {
                is ApiResult.Success -> {
                    _state.update { it.copy(isRefreshing = false, items = result.data.sortedBy { item -> item.sortOrder }) }
                    applyFilters()
                }
                is ApiResult.Error -> _state.update { it.copy(isRefreshing = false, error = result.message) }
                is ApiResult.Loading -> {}
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _state.update { it.copy(searchQuery = query) }
        applyFilters()
    }

    fun updateFilterCategory(categoryId: String?) {
        _state.update { it.copy(filterCategoryId = categoryId) }
        applyFilters()
    }

    private fun applyFilters() {
        val currentState = _state.value
        val query = currentState.searchQuery.trim()
        val categoryId = currentState.filterCategoryId

        val filtered = currentState.items.filter { item ->
            val matchesQuery = if (query.isNotEmpty()) {
                item.name.contains(query, ignoreCase = true)
            } else true
            val matchesCategory = if (categoryId != null) {
                item.categoryId == categoryId
            } else true
            matchesQuery && matchesCategory
        }

        _state.update { it.copy(filteredItems = filtered) }
    }

    fun toggleAvailability(id: String, available: Boolean) {
        viewModelScope.launch {
            when (val result = repository.toggleAvailability(id, available)) {
                is ApiResult.Success -> refresh()
                is ApiResult.Error -> _state.update { it.copy(error = result.message) }
                is ApiResult.Loading -> {}
            }
        }
    }

    fun toggleVisibility(id: String, visible: Boolean) {
        viewModelScope.launch {
            when (val result = repository.toggleVisibility(id, visible)) {
                is ApiResult.Success -> refresh()
                is ApiResult.Error -> _state.update { it.copy(error = result.message) }
                is ApiResult.Loading -> {}
            }
        }
    }

    fun saveMenuItem(
        id: String?,
        name: String,
        description: String?,
        price: Double,
        categoryId: String,
        preparationTime: Int,
        newImageUri: android.net.Uri?,
        currentImageUrl: String?,
        customizationGroupIds: List<String>
    ) {
        viewModelScope.launch {
            _state.update { it.copy(isUpdating = true) }
            
            var imageUrlToSave = currentImageUrl
            
            if (newImageUri != null) {
                _state.update { it.copy(isUploadingImage = true) }
                when (val uploadResult = repository.uploadImage(newImageUri)) {
                    is ApiResult.Success -> {
                        imageUrlToSave = uploadResult.data
                        _state.update { it.copy(isUploadingImage = false) }
                    }
                    is ApiResult.Error -> {
                        _state.update { it.copy(isUpdating = false, isUploadingImage = false, error = uploadResult.message) }
                        return@launch
                    }
                    is ApiResult.Loading -> {}
                }
            }
            
            val isNew = id.isNullOrBlank()
            
            val result = if (isNew) {
                val sortOrder = _state.value.items.count { it.categoryId == categoryId } + 1
                repository.createMenuItem(
                    name = name,
                    description = description,
                    price = price,
                    categoryId = categoryId,
                    preparationTime = preparationTime,
                    sortOrder = sortOrder,
                    image = imageUrlToSave,
                    customizationGroupIds = customizationGroupIds
                )
            } else {
                repository.updateMenuItem(
                    id = id!!,
                    name = name,
                    description = description,
                    price = price,
                    categoryId = categoryId,
                    preparationTime = preparationTime,
                    image = imageUrlToSave,
                    customizationGroupIds = customizationGroupIds
                )
            }
            
            when (result) {
                is ApiResult.Success -> {
                    _state.update { it.copy(isUpdating = false, isFormOpen = false, editingItem = null) }
                    refresh()
                }
                is ApiResult.Error -> _state.update { it.copy(isUpdating = false, error = result.message) }
                is ApiResult.Loading -> {}
            }
        }
    }

    fun deleteMenuItem(id: String) {
        viewModelScope.launch {
            _state.update { it.copy(isUpdating = true) }
            val result = repository.deleteMenuItem(id)
            if (result.isSuccess) {
                _state.update { it.copy(isUpdating = false) }
                refresh()
            } else {
                _state.update { it.copy(isUpdating = false, error = result.exceptionOrNull()?.message ?: "Delete failed") }
            }
        }
    }

    fun updateSortOrder(items: List<MenuItem>) {
        // Can only sort when filtered to a specific category
        if (_state.value.filterCategoryId == null) return
        
        _state.update { it.copy(filteredItems = items) }
        
        viewModelScope.launch {
            val sortPairs = items.mapIndexed { index, item ->
                Pair(item.id, index + 1)
            }
            val result = repository.updateSortOrder(sortPairs)
            if (result.isFailure) {
                _state.update { it.copy(error = "Failed to update sort order") }
                refresh()
            }
        }
    }

    fun openCreateForm() = _state.update { it.copy(isFormOpen = true, editingItem = null) }
    fun openEditForm(item: MenuItem) = _state.update { it.copy(isFormOpen = true, editingItem = item) }
    fun closeForm() = _state.update { it.copy(isFormOpen = false, editingItem = null) }
    
    fun dismissError() = _state.update { it.copy(error = null) }
}
