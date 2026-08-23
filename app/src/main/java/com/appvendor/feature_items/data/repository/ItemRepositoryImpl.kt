package com.appvendor.feature_items.data.repository

import com.appvendor.feature_items.data.local.ItemDao
import com.appvendor.feature_items.data.remote.ItemApiService
import com.appvendor.feature_items.data.remote.dto.CategoryFieldDto
import com.appvendor.feature_items.data.remote.dto.CreateCategoryRequest
import com.appvendor.feature_items.data.remote.dto.CreateItemRequest
import com.appvendor.feature_items.domain.model.Category
import com.appvendor.feature_items.domain.model.Item
import com.appvendor.feature_items.domain.repository.ItemRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

class ItemRepositoryImpl @Inject constructor(
    private val apiService: ItemApiService,
    private val dao: ItemDao
) : ItemRepository {

    override fun getCategories(): Flow<List<Category>> {
        return dao.getCategories().map { entities ->
            entities.map { it.toDomain() }
        }.onStart {
            try {
                val remoteCategories = apiService.getCategories().data ?: emptyList()
                dao.clearCategories()
                dao.insertCategories(remoteCategories.map { it.toEntity() })
            } catch (e: Exception) {
                // Ignore and use local cache
            }
        }
    }

    override suspend fun createCategory(category: Category) {
        val request = CreateCategoryRequest(
            name = category.name,
            description = category.description,
            fields = category.fields.map { 
                CategoryFieldDto(
                    id = it.id, 
                    name = it.name, 
                    type = it.type.name, 
                    isRequired = it.isRequired, 
                    options = it.options
                ) 
            }
        )
        val response = apiService.createCategory(request).data
        response?.let { dao.insertCategory(it.toEntity()) }
    }

    override suspend fun updateCategory(category: Category) {
        val request = CreateCategoryRequest(
            name = category.name,
            description = category.description,
            fields = category.fields.map { 
                CategoryFieldDto(
                    id = it.id, 
                    name = it.name, 
                    type = it.type.name, 
                    isRequired = it.isRequired, 
                    options = it.options
                ) 
            }
        )
        val response = apiService.updateCategory(category.id, request).data
        response?.let { dao.insertCategory(it.toEntity()) }
    }

    override suspend fun deleteCategory(categoryId: String) {
        apiService.deleteCategory(categoryId)
        dao.deleteCategory(categoryId)
    }

    override fun getItems(categoryId: String?): Flow<List<Item>> {
        val localFlow = if (categoryId == null) {
            dao.getAllItems()
        } else {
            dao.getItemsByCategory(categoryId)
        }

        return localFlow.map { entities ->
            entities.map { it.toDomain() }
        }.onStart {
            try {
                val remoteItems = apiService.getItems(categoryId).data ?: emptyList()
                if (categoryId == null) {
                    dao.clearItems()
                }
                dao.insertItems(remoteItems.map { it.toEntity() })
            } catch (e: Exception) {
                // Ignore and use local cache
            }
        }
    }

    override suspend fun createItem(item: Item) {
        val request = CreateItemRequest(
            categoryId = item.categoryId,
            name = item.name,
            description = item.description,
            price = item.price,
            stockQuantity = item.stockQuantity,
            imageUrl = item.imageUrl,
            dynamicFields = item.dynamicFields
        )
        val response = apiService.createItem(request).data
        response?.let { dao.insertItem(it.toEntity()) }
    }

    override suspend fun updateItem(item: Item) {
        val request = CreateItemRequest(
            categoryId = item.categoryId,
            name = item.name,
            description = item.description,
            price = item.price,
            stockQuantity = item.stockQuantity,
            imageUrl = item.imageUrl,
            dynamicFields = item.dynamicFields
        )
        val response = apiService.updateItem(item.id, request).data
        response?.let { dao.insertItem(it.toEntity()) }
    }

    override suspend fun deleteItem(itemId: String) {
        apiService.deleteItem(itemId)
        dao.deleteItem(itemId)
    }
}
