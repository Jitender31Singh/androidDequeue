package com.appvendor.feature_items.domain.usecase

import com.appvendor.feature_items.domain.model.Item
import com.appvendor.feature_items.domain.repository.ItemRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetItemsUseCase @Inject constructor(
    private val repository: ItemRepository
) {
    operator fun invoke(categoryId: String? = null): Flow<List<Item>> {
        return repository.getItems(categoryId)
    }
}
