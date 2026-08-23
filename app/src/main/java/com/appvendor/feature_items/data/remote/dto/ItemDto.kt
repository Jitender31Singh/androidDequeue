package com.appvendor.feature_items.data.remote.dto

import com.appvendor.feature_items.domain.model.Item
import com.appvendor.feature_items.data.local.ItemEntity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class ItemDto(
    val id: String,
    val categoryId: String,
    val name: String,
    val description: String?,
    val price: Double,
    val stockQuantity: Int,
    val imageUrl: String?,
    val dynamicFields: Map<String, String>?
) {
    fun toDomain(): Item {
        return Item(
            id = id,
            categoryId = categoryId,
            name = name,
            description = description ?: "",
            price = price,
            stockQuantity = stockQuantity,
            imageUrl = imageUrl,
            dynamicFields = dynamicFields ?: emptyMap()
        )
    }
    
    fun toEntity(): ItemEntity {
        return ItemEntity(
            id = id,
            categoryId = categoryId,
            name = name,
            description = description ?: "",
            price = price,
            stockQuantity = stockQuantity,
            imageUrl = imageUrl,
            dynamicFieldsJson = Gson().toJson(dynamicFields ?: emptyMap<String, String>())
        )
    }
}
