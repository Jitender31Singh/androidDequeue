package com.appvendor.feature_items.data.remote.dto

import com.appvendor.feature_items.domain.model.Category
import com.appvendor.feature_items.domain.model.CategoryField
import com.appvendor.feature_items.domain.model.FieldType
import com.appvendor.feature_items.data.local.CategoryEntity
import com.google.gson.Gson

data class CategoryDto(
    val id: String,
    val name: String,
    val description: String?,
    val fields: List<CategoryFieldDto>?
) {
    fun toDomain(): Category {
        return Category(
            id = id,
            name = name,
            description = description ?: "",
            fields = fields?.map { it.toDomain() } ?: emptyList()
        )
    }
    
    fun toEntity(): CategoryEntity {
        return CategoryEntity(
            id = id,
            name = name,
            description = description ?: "",
            fieldsJson = Gson().toJson(fields ?: emptyList<CategoryFieldDto>())
        )
    }
}

data class CategoryFieldDto(
    val id: String,
    val name: String,
    val type: String,
    val isRequired: Boolean,
    val options: List<String>?
) {
    fun toDomain(): CategoryField {
        return CategoryField(
            id = id,
            name = name,
            type = try { FieldType.valueOf(type) } catch(e: Exception) { FieldType.TEXT },
            isRequired = isRequired,
            options = options
        )
    }
}
