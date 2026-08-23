package com.appvendor.feature_items.domain.usecase

import com.appvendor.feature_items.domain.repository.ItemRepository
import javax.inject.Inject

class DeleteCategoryUseCase @Inject constructor(
    private val repository: ItemRepository
) {
    suspend operator fun invoke(categoryId: String) {
        repository.deleteCategory(categoryId)
    }
}
