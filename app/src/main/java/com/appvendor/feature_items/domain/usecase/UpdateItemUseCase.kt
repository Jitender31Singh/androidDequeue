package com.appvendor.feature_items.domain.usecase

import com.appvendor.feature_items.domain.model.Item
import com.appvendor.feature_items.domain.repository.ItemRepository
import javax.inject.Inject

class UpdateItemUseCase @Inject constructor(
    private val repository: ItemRepository
) {
    suspend operator fun invoke(item: Item) {
        repository.updateItem(item)
    }
}
