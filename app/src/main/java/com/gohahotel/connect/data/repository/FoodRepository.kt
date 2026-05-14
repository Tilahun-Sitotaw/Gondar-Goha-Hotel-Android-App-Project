package com.gohahotel.connect.data.repository

import com.gohahotel.connect.data.local.dao.MenuDao
import com.gohahotel.connect.data.local.dao.OrderDao
import com.gohahotel.connect.data.local.entity.OrderEntity
import com.gohahotel.connect.data.local.toDomain
import com.gohahotel.connect.data.local.toEntity
import com.gohahotel.connect.data.remote.FirestoreService
import com.gohahotel.connect.domain.model.MenuItem
import com.gohahotel.connect.domain.model.MenuCategory
import com.gohahotel.connect.domain.model.Order
import com.gohahotel.connect.domain.model.OrderStatus
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FoodRepository @Inject constructor(
    private val menuDao: MenuDao,
    private val orderDao: OrderDao,
    private val firestoreService: FirestoreService
) {
    private val gson = Gson()

    // ─── Menu ──────────────────────────────────────────────────────────────
    fun getAllMenuItems(): Flow<List<MenuItem>> =
        menuDao.getAllMenuItems().map { it.map { e -> e.toDomain() } }

    fun getMenuByCategory(category: MenuCategory): Flow<List<MenuItem>> =
        menuDao.getMenuItemsByCategory(category.name).map { it.map { e -> e.toDomain() } }

    fun getFeaturedItems(): Flow<List<MenuItem>> =
        menuDao.getFeaturedItems().map { it.map { e -> e.toDomain() } }

    fun searchMenu(query: String): Flow<List<MenuItem>> =
        menuDao.searchMenuItems(query).map { it.map { e -> e.toDomain() } }

    suspend fun refreshMenu() {
        val items = firestoreService.fetchMenuItems()
        menuDao.clearAll()
        menuDao.upsertMenuItems(items.map { it.toEntity() })
    }

    // ─── Orders ────────────────────────────────────────────────────────────
    suspend fun placeOrder(order: Order): String {
        val orderId = firestoreService.placeOrder(order)
        orderDao.upsertOrder(order.copy(id = orderId).toOrderEntity())
        return orderId
    }

    fun observeOrderStatus(orderId: String): Flow<Order?> =
        firestoreService.observeOrder(orderId)

    /** Real-time stream of all orders for the current guest from Firestore */
    fun observeGuestOrders(guestId: String): Flow<List<Order>> =
        firestoreService.observeOrdersByGuest(guestId)

    fun getActiveOrders(): Flow<List<Order>> =
        orderDao.getActiveOrders().map { it.map { e -> e.toOrderDomain(gson) } }

    fun getRecentOrders(): Flow<List<Order>> =
        orderDao.getRecentOrders().map { it.map { e -> e.toOrderDomain(gson) } }

    suspend fun getAllOrders(): List<Order> {
        return firestoreService.fetchAllOrders()
    }

    suspend fun updateOrderStatus(orderId: String, status: OrderStatus) {
        firestoreService.updateOrderStatus(orderId, status.name)
        orderDao.updateOrderStatus(orderId, status.name)
    }

    suspend fun updateLocalOrderStatus(orderId: String, status: OrderStatus) {
        orderDao.updateOrderStatus(orderId, status.name)
    }

    suspend fun saveMenuItem(item: MenuItem) = firestoreService.saveMenuItem(item)
    suspend fun deleteMenuItem(itemId: String) = firestoreService.deleteMenuItem(itemId)
}

// ─── Order mappers (kept here to avoid circular imports) ──────────────────────
fun Order.toOrderEntity() = OrderEntity(
    id                      = id,
    guestId                 = guestId,
    guestName               = guestName,
    roomNumber              = roomNumber,
    itemsJson               = Gson().toJson(items),
    type                    = type.name,
    status                  = status.name,
    specialInstructions     = specialInstructions,
    totalAmount             = totalAmount,
    currency                = currency,
    createdAt               = createdAt.time,
    estimatedDeliveryMinutes = estimatedDeliveryMinutes
)

fun OrderEntity.toOrderDomain(gson: Gson): Order {
    val itemType = object : TypeToken<List<com.gohahotel.connect.domain.model.OrderItem>>() {}.type
    return Order(
        id                      = id,
        guestId                 = guestId,
        guestName               = guestName,
        roomNumber              = roomNumber,
        items                   = gson.fromJson(itemsJson, itemType),
        type                    = com.gohahotel.connect.domain.model.OrderType.valueOf(type),
        status                  = OrderStatus.valueOf(status),
        specialInstructions     = specialInstructions,
        totalAmount             = totalAmount,
        currency                = currency,
        createdAt               = java.util.Date(createdAt),
        estimatedDeliveryMinutes = estimatedDeliveryMinutes
    )
}
