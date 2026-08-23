package com.appvendor.feature_items.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.appvendor.feature_items.domain.model.Category
import com.appvendor.feature_items.data.remote.dto.CategoryFieldDto
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val fieldsJson: String // Stored as JSON for simplicity
) {
    fun toDomain(): Category {
        val type = object : TypeToken<List<CategoryFieldDto>>() {}.type
        val fieldsDto: List<CategoryFieldDto> = try {
            Gson().fromJson(fieldsJson, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
        return Category(
            id = id,
            name = name,
            description = description,
            fields = fieldsDto.map { it.toDomain() }
        )
    }
}
