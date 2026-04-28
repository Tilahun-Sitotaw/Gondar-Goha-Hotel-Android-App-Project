package com.gohahotel.connect.data.local.dao

import androidx.room.*
import com.gohahotel.connect.data.local.entity.OrderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderDao {
    @Query("SELECT * FROM orders WHERE guestId = :guestId ORDER BY createdAt DESC")
    fun getOrdersByGuest(guestId: String): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE id = :id")
    suspend fun getOrderById(id: String): OrderEntity?

    @Query("SELECT * FROM orders WHERE status NOT IN ('DELIVERED', 'CANCELLED') ORDER BY createdAt DESC")
    fun getActiveOrders(): Flow<List<OrderEntity>>

    @Upsert
    suspend fun upsertOrder(order: OrderEntity)

    @Query("UPDATE orders SET status = :status WHERE id = :orderId")
    suspend fun updateOrderStatus(orderId: String, status: String)

    @Query("DELETE FROM orders WHERE createdAt < :cutoff")
    suspend fun deleteOldOrders(cutoff: Long)

    @Query("SELECT * FROM orders ORDER BY createdAt DESC LIMIT 20")
    fun getRecentOrders(): Flow<List<OrderEntity>>
}
