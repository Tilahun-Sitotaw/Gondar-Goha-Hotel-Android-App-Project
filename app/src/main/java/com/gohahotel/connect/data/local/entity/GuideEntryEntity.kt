package com.gohahotel.connect.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "guide_entries")
data class GuideEntryEntity(
    @PrimaryKey val id: String,
    val title: String,
    val titleAmharic: String,
    val titleFrench: String,
    val summary: String,
    val summaryAmharic: String,
    val summaryFrench: String,
    val content: String,
    val contentAmharic: String,
    val contentFrench: String,
    val category: String,
    val imageUrls: String,          // JSON array string
    val latitude: Double,
    val longitude: Double,
    val distanceFromHotelKm: Double,
    val openingHours: String,
    val entryFee: String,
    val tags: String                // JSON array string
)
