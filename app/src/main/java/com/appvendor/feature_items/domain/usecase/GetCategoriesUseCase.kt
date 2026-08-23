package com.appvendor.feature_items.domain.usecase

import com.appvendor.feature_items.domain.model.Category
import com.appvendor.feature_items.domain.repository.ItemRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCategoriesUseCase @Inject constructor(
    private val repository: ItemRepository
) {
    operator fun invoke(): Flow<List<Category>> {
        return repository.getCategories()
    }
}
