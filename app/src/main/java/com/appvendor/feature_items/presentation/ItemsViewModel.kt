package com.appvendor.feature_items.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appvendor.feature_items.domain.model.Category
import com.appvendor.feature_items.domain.model.Item
import com.appvendor.feature_items.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ItemsViewModel @Inject constructor(
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val createCategoryUseCase: CreateCategoryUseCase,
    private val deleteCategoryUseCase: DeleteCategoryUseCase,
    private val getItemsUseCase: GetItemsUseCase,
    private val createItemUseCase: CreateItemUseCase,
    private val updateItemUseCase: UpdateItemUseCase,
    private val deleteItemUseCase: DeleteItemUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ItemsState())
    val state: StateFlow<ItemsState> = _state.asStateFlow()

    init {
        loadData()
    }

    fun loadData(isRefresh: Boolean = false) {
        if (isRefresh) {
            _state.update { it.copy(isRefreshing = true, error = null) }
        } else {
            _state.update { it.copy(isLoading = true, error = null) }
        }

        getCategoriesUseCase()
            .onEach { categories ->
                _state.update { it.copy(categories = categories) }
                // Load items after categories load
                loadItems(_state.value.selectedCategoryId)
            }
            .catch { e ->
                _state.update { it.copy(isLoading = false, isRefreshing = false, error = e.message ?: "Failed to load categories") }
            }
            .launchIn(viewModelScope)
    }

    private fun loadItems(categoryId: String?) {
        getItemsUseCase(categoryId)
            .onEach { items ->
                _state.update { 
                    it.copy(
                        items = items, 
                        isLoading = false,
                        isRefreshing = false
                    ) 
                }
            }
            .catch { e ->
                _state.update { 
                    it.copy(
                        isLoading = false, 
                        isRefreshing = false,
                        error = e.message ?: "Failed to load items"
                    ) 
                }
            }
            .launchIn(viewModelScope)
    }

    fun selectCategory(categoryId: String?) {
        _state.update { it.copy(selectedCategoryId = categoryId) }
        loadItems(categoryId)
    }

    fun createCategory(category: Category) {
        viewModelScope.launch {
            try {
                createCategoryUseCase(category)
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message ?: "Failed to create category") }
            }
        }
    }

    fun deleteCategory(categoryId: String) {
        viewModelScope.launch {
            try {
                deleteCategoryUseCase(categoryId)
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message ?: "Failed to delete category") }
            }
        }
    }

    fun createItem(item: Item) {
        viewModelScope.launch {
            try {
                createItemUseCase(item)
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message ?: "Failed to create item") }
            }
        }
    }

    fun updateItem(item: Item) {
        viewModelScope.launch {
            try {
                updateItemUseCase(item)
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message ?: "Failed to update item") }
            }
        }
    }

    fun deleteItem(itemId: String) {
        viewModelScope.launch {
            try {
                deleteItemUseCase(itemId)
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message ?: "Failed to delete item") }
            }
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}
