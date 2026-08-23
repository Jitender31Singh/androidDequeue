package com.appvendor.feature_items.domain.usecase

import com.appvendor.feature_items.domain.repository.ItemRepository
import javax.inject.Inject

class DeleteItemUseCase @Inject constructor(
    private val repository: ItemRepository
) {
    suspend operator fun invoke(itemId: String) {
        repository.deleteItem(itemId)
    }
}
