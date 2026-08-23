package com.appvendor.feature_dashboard.data.repository

import com.appvendor.feature_dashboard.data.local.OrderDao
import com.appvendor.feature_dashboard.data.local.OrderEntity
import com.appvendor.feature_dashboard.data.remote.OrderApiService
import com.appvendor.feature_dashboard.data.remote.dto.OrderDto
import com.appvendor.feature_dashboard.data.remote.dto.UpdateStatusRequest
import com.appvendor.feature_dashboard.domain.model.Order
import com.appvendor.feature_dashboard.domain.model.OrderItem
import com.appvendor.feature_dashboard.domain.model.OrderStatus
import com.appvendor.feature_dashboard.domain.repository.OrderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject

class OrderRepositoryImpl @Inject constructor(
    private val api: OrderApiService,
    private val dao: OrderDao
) : OrderRepository {

    override fun getActiveOrder(vendorId: String): Flow<Order?> {
        return dao.getActiveOrders().map { entities ->
            entities.firstOrNull()?.toDomain()
        }
    }

    override fun getPendingOrders(vendorId: String): Flow<List<Order>> {
        return dao.getPendingOrders().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun updateOrderStatus(orderId: String, status: OrderStatus): Result<Order> {
        return try {
            val response = api.updateOrderStatus(orderId, UpdateStatusRequest(status.name)).data
            if (response != null) {
                dao.upsertOrder(response.toEntity())
                Result.success(response.toDomain())
            } else {
                Result.failure(Exception("No data returned"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun refreshOrders(vendorId: String): Result<Unit> {
        return try {
            // Fetch active
            val activeOrder = api.getActiveOrder(vendorId).data
            activeOrder?.let { dao.upsertOrder(it.toEntity()) }
            
            // Fetch pending
            val pendingOrdersResponse = api.getPendingOrders(vendorId, 0, 50).data
            pendingOrdersResponse?.content?.let { pendingOrders ->
                dao.upsertOrders(pendingOrders.map { it.toEntity() })
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Mappers
fun OrderDto.toEntity(): OrderEntity {
    val itemsJson = JSONArray()
    items.forEach { item ->
        val obj = JSONObject()
        obj.put("id", item.id)
        obj.put("name", item.name)
        obj.put("quantity", item.quantity)
        obj.put("price", item.price)
        obj.put("notes", item.notes)
        itemsJson.put(obj)
    }
    return OrderEntity(
        id = id,
        customerName = customerName,
        customerPhone = customerPhone,
        items = itemsJson.toString(),
        status = status,
        totalAmount = totalAmount,
        createdAt = createdAt,
        updatedAt = updatedAt,
        notes = notes
    )
}

fun OrderDto.toDomain(): Order {
    return Order(
        id = id,
        customerName = customerName,
        customerPhone = customerPhone,
        items = items.map { OrderItem(it.id, it.name, it.quantity, it.price, it.notes) },
        status = OrderStatus.valueOf(status),
        totalAmount = totalAmount,
        createdAt = createdAt,
        updatedAt = updatedAt,
        notes = notes
    )
}

fun OrderEntity.toDomain(): Order {
    val itemsList = mutableListOf<OrderItem>()
    if (items.isNotEmpty()) {
        val jsonArray = JSONArray(items)
        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            itemsList.add(
                OrderItem(
                    id = obj.getString("id"),
                    name = obj.getString("name"),
                    quantity = obj.getInt("quantity"),
                    price = obj.getDouble("price"),
                    notes = if (obj.has("notes")) obj.getString("notes") else null
                )
            )
        }
    }
    return Order(
        id = id,
        customerName = customerName,
        customerPhone = customerPhone,
        items = itemsList,
        status = OrderStatus.valueOf(status),
        totalAmount = totalAmount,
        createdAt = createdAt,
        updatedAt = updatedAt,
        notes = notes
    )
}
