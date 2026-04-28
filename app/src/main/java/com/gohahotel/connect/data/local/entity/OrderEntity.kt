package com.gohahotel.connect.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey val id: String,
    val guestId: String,
    val guestName: String,
    val roomNumber: String,
    val itemsJson: String,          // JSON serialized list of OrderItem
    val type: String,
    val status: String,
    val specialInstructions: String,
    val totalAmount: Double,
    val currency: String,
    val createdAt: Long,
    val estimatedDeliveryMinutes: Int
)
