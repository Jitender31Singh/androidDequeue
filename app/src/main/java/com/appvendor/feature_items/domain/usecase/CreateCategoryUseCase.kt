package com.appvendor.feature_items.domain.usecase

import com.appvendor.feature_items.domain.model.Category
import com.appvendor.feature_items.domain.repository.ItemRepository
import javax.inject.Inject

class CreateCategoryUseCase @Inject constructor(
    private val repository: ItemRepository
) {
    suspend operator fun invoke(category: Category) {
        repository.createCategory(category)
    }
}
