package com.appvendor.feature_categories.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appvendor.core.network.ApiResult
import com.appvendor.feature_categories.data.repository.CategoryRepository
import com.appvendor.feature_categories.domain.model.Category
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoriesViewModel @Inject constructor(
    private val repository: CategoryRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CategoriesState())
    val state: StateFlow<CategoriesState> = _state.asStateFlow()

    init {
        loadCategories()
    }

    fun loadCategories() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = it.categories.isEmpty(), error = null) }
            when (val result = repository.getCategories()) {
                is ApiResult.Success -> _state.update { 
                    it.copy(isLoading = false, categories = result.data.sortedBy { cat -> cat.sortOrder }) 
                }
                is ApiResult.Error -> _state.update { it.copy(isLoading = false, error = result.message) }
                is ApiResult.Loading -> {}
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isRefreshing = true, error = null) }
            when (val result = repository.getCategories()) {
                is ApiResult.Success -> _state.update { 
                    it.copy(isRefreshing = false, categories = result.data.sortedBy { cat -> cat.sortOrder }) 
                }
                is ApiResult.Error -> _state.update { it.copy(isRefreshing = false, error = result.message) }
                is ApiResult.Loading -> {}
            }
        }
    }

    fun createCategory(name: String, description: String?) {
        viewModelScope.launch {
            _state.update { it.copy(isUpdating = true) }
            val sortOrder = _state.value.categories.size + 1
            when (val result = repository.createCategory(name, description, sortOrder)) {
                is ApiResult.Success -> {
                    _state.update { it.copy(isUpdating = false, isCreateDialogOpen = false) }
                    loadCategories()
                }
                is ApiResult.Error -> _state.update { it.copy(isUpdating = false, error = result.message) }
                is ApiResult.Loading -> {}
            }
        }
    }

    fun updateCategory(id: String, name: String, description: String?) {
        viewModelScope.launch {
            _state.update { it.copy(isUpdating = true) }
            when (val result = repository.updateCategory(id, name, description)) {
                is ApiResult.Success -> {
                    _state.update { it.copy(isUpdating = false, editCategory = null) }
                    loadCategories()
                }
                is ApiResult.Error -> _state.update { it.copy(isUpdating = false, error = result.message) }
                is ApiResult.Loading -> {}
            }
        }
    }

    fun deleteCategory(id: String) {
        viewModelScope.launch {
            _state.update { it.copy(isUpdating = true) }
            val result = repository.deleteCategory(id)
            if (result.isSuccess) {
                _state.update { it.copy(isUpdating = false) }
                loadCategories()
            } else {
                _state.update { it.copy(isUpdating = false, error = result.exceptionOrNull()?.message ?: "Delete failed") }
            }
        }
    }

    fun updateSortOrder(items: List<Category>) {
        // Optimistically update the UI list
        _state.update { it.copy(categories = items) }
        
        viewModelScope.launch {
            val sortPairs = items.mapIndexed { index, category ->
                Pair(category.id, index + 1)
            }
            val result = repository.updateSortOrder(sortPairs)
            if (result.isFailure) {
                // If backend fails, reload original order
                _state.update { it.copy(error = "Failed to update sort order") }
                loadCategories()
            }
        }
    }

    fun openCreateDialog() = _state.update { it.copy(isCreateDialogOpen = true) }
    fun closeCreateDialog() = _state.update { it.copy(isCreateDialogOpen = false) }
    
    fun openEditDialog(category: Category) = _state.update { it.copy(editCategory = category) }
    fun closeEditDialog() = _state.update { it.copy(editCategory = null) }
    
    fun dismissError() = _state.update { it.copy(error = null) }
}
