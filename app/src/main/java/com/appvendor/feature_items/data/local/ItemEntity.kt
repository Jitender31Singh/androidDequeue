package com.appvendor.feature_items.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.appvendor.feature_items.domain.model.Item
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Entity(tableName = "items")
data class ItemEntity(
    @PrimaryKey val id: String,
    val categoryId: String,
    val name: String,
    val description: String,
    val price: Double,
    val stockQuantity: Int,
    val imageUrl: String?,
    val dynamicFieldsJson: String
) {
    fun toDomain(): Item {
        val type = object : TypeToken<Map<String, String>>() {}.type
        val dynamicFields: Map<String, String> = try {
            Gson().fromJson(dynamicFieldsJson, type) ?: emptyMap()
        } catch (e: Exception) {
            emptyMap()
        }
        return Item(
            id = id,
            categoryId = categoryId,
            name = name,
            description = description,
            price = price,
            stockQuantity = stockQuantity,
            imageUrl = imageUrl,
            dynamicFields = dynamicFields
        )
    }
}
