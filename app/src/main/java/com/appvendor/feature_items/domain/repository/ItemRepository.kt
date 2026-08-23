package com.appvendor.feature_items.domain.repository

import com.appvendor.feature_items.domain.model.Category
import com.appvendor.feature_items.domain.model.Item
import kotlinx.coroutines.flow.Flow

interface ItemRepository {
    fun getCategories(): Flow<List<Category>>
    suspend fun createCategory(category: Category)
    suspend fun updateCategory(category: Category)
    suspend fun deleteCategory(categoryId: String)

    fun getItems(categoryId: String? = null): Flow<List<Item>>
    suspend fun createItem(item: Item)
    suspend fun updateItem(item: Item)
    suspend fun deleteItem(itemId: String)
}
