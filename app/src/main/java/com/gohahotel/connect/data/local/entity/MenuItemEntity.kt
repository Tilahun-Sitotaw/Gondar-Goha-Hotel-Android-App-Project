package com.gohahotel.connect.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "menu_items")
data class MenuItemEntity(
    @PrimaryKey val id: String,
    val name: String,
    val nameAmharic: String,
    val nameFrench: String,
    val description: String,
    val descriptionAmharic: String,
    val category: String,
    val price: Double,
    val currency: String,
    val imageUrl: String,
    val isAvailable: Boolean,
    val isVegetarian: Boolean,
    val isVegan: Boolean,
    val isGlutenFree: Boolean,
    val isSpicy: Boolean,
    val spiceLevel: Int,
    val allergens: String,          // JSON array string
    val prepTimeMinutes: Int,
    val customizations: String,     // JSON array string
    val isFeatured: Boolean,
    val rating: Float,
    val cachedAt: Long = System.currentTimeMillis()
)
