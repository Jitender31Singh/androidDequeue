package com.appvendor.feature_orders.data.repository

import com.appvendor.core.network.ApiResult
import com.appvendor.core.network.safeApiCall
import com.appvendor.feature_orders.data.remote.OrderApiService
import com.appvendor.feature_orders.data.remote.dto.OrderResponseDto
import com.appvendor.feature_orders.data.remote.dto.OrderSummaryDto
import com.appvendor.feature_orders.data.remote.dto.UpdateOrderStatusRequest
import com.appvendor.feature_orders.domain.model.Order
import com.appvendor.feature_orders.domain.model.OrderItem
import com.appvendor.feature_orders.domain.model.OrderStatus
import com.appvendor.feature_orders.domain.model.OrderSummary
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OrderRepository @Inject constructor(
    private val api: OrderApiService
) {

    suspend fun getActiveOrders(): ApiResult<List<OrderSummary>> {
        return when (val result = safeApiCall { api.getActiveOrders() }) {
            is ApiResult.Success -> ApiResult.Success(
                result.data.data?.map { it.toSummary() } ?: emptyList()
            )
            is ApiResult.Error -> result
            is ApiResult.Loading -> result
        }
    }

    suspend fun getOrderHistory(
        status: String? = null,
        startDate: String? = null,
        endDate: String? = null,
        queueNumber: String? = null,
        page: Int = 0
    ): ApiResult<List<OrderSummary>> {
        return when (val result = safeApiCall {
            api.getOrderHistory(status, startDate, endDate, queueNumber, page)
        }) {
            is ApiResult.Success -> ApiResult.Success(
                result.data.data?.content?.map { it.toSummary() } ?: emptyList()
            )
            is ApiResult.Error -> result
            is ApiResult.Loading -> result
        }
    }

    suspend fun getOrderById(id: String): ApiResult<Order> {
        return when (val result = safeApiCall { api.getOrderById(id) }) {
            is ApiResult.Success -> {
                val dto = result.data.data
                if (dto != null) ApiResult.Success(dto.toDomain())
                else ApiResult.Error("Order not found")
            }
            is ApiResult.Error -> result
            is ApiResult.Loading -> result
        }
    }

    suspend fun updateOrderStatus(id: String, status: OrderStatus, note: String? = null): ApiResult<Order> {
        val backendStatus = status.name
        return when (val result = safeApiCall {
            api.updateOrderStatus(id, UpdateOrderStatusRequest(backendStatus, note))
        }) {
            is ApiResult.Success -> {
                val dto = result.data.data
                if (dto != null) ApiResult.Success(dto.toDomain())
                else ApiResult.Error("No data returned")
            }
            is ApiResult.Error -> result
            is ApiResult.Loading -> result
        }
    }
}

// ── Mappers ────────────────────────────────────────────────────────────────────

private fun String.toOrderStatus(): OrderStatus {
    if (this == "CONFIRMED") return OrderStatus.ACCEPTED
    return runCatching { OrderStatus.valueOf(this) }.getOrDefault(OrderStatus.PENDING)
}

fun OrderSummaryDto.toSummary() = OrderSummary(
    id = id,
    queueNumber = queueNumber,
    totalAmount = totalAmount,
    status = status.toOrderStatus(),
    itemCount = if (itemCount > 0) itemCount else (items?.sumOf { it.quantity } ?: 0),
    metadata = metadata,
    customFields = customFields,
    createdAt = createdAt
)

fun OrderResponseDto.toDomain() = Order(
    id = id,
    queueNumber = queueNumber,
    status = status.toOrderStatus(),
    totalAmount = totalAmount,
    customerNote = customerNote,
    items = items.map {
        OrderItem(
            menuItemId = it.menuItemId,
            menuItemName = it.menuItemName,
            quantity = it.quantity,
            unitPrice = it.unitPrice,
            totalPrice = it.totalPrice,
            specialInstructions = it.specialInstructions,
            selectedCustomizations = it.selectedCustomizations?.map { cust ->
                com.appvendor.feature_orders.domain.model.SelectedCustomization(
                    customizationId = cust.customizationId,
                    name = cust.name,
                    selectedOptions = cust.selectedOptions.map { opt ->
                        com.appvendor.feature_orders.domain.model.SelectedOption(
                            id = opt.id,
                            name = opt.name,
                            additionalPrice = opt.additionalPrice
                        )
                    }
                )
            } ?: emptyList()
        )
    },
    metadata = metadata,
    customFields = customFields,
    couponCode = couponCode,
    couponDiscount = couponDiscount,
    taxName = taxName,
    taxAmount = taxAmount,
    serviceChargeName = serviceChargeName,
    serviceChargeAmount = serviceChargeAmount,
    createdAt = createdAt,
    updatedAt = updatedAt
)
