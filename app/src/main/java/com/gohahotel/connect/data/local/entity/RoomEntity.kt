package com.gohahotel.connect.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rooms")
data class RoomEntity(
    @PrimaryKey val id: String,
    val name: String,
    val type: String,
    val description: String,
    val pricePerNight: Double,
    val currency: String,
    val capacity: Int,
    val bedType: String,
    val floorNumber: Int,
    val amenities: String,        // JSON array string
    val imageUrls: String,        // JSON array string
    val isAvailable: Boolean,
    val rating: Float,
    val reviewCount: Int,
    val hasView: Boolean,
    val hasMountainView: Boolean,
    val cachedAt: Long = System.currentTimeMillis()
)
