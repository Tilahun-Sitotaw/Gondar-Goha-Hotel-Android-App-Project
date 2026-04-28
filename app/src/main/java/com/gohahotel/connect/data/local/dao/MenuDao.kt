package com.gohahotel.connect.data.local.dao

import androidx.room.*
import com.gohahotel.connect.data.local.entity.MenuItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MenuDao {
    @Query("SELECT * FROM menu_items ORDER BY category, name")
    fun getAllMenuItems(): Flow<List<MenuItemEntity>>

    @Query("SELECT * FROM menu_items WHERE category = :category AND isAvailable = 1")
    fun getMenuItemsByCategory(category: String): Flow<List<MenuItemEntity>>

    @Query("SELECT * FROM menu_items WHERE isFeatured = 1 AND isAvailable = 1 LIMIT 10")
    fun getFeaturedItems(): Flow<List<MenuItemEntity>>

    @Query("SELECT * FROM menu_items WHERE id = :id")
    suspend fun getMenuItemById(id: String): MenuItemEntity?

    @Query("SELECT * FROM menu_items WHERE (name LIKE '%' || :query || '%' OR nameAmharic LIKE '%' || :query || '%') AND isAvailable = 1")
    fun searchMenuItems(query: String): Flow<List<MenuItemEntity>>

    @Upsert
    suspend fun upsertMenuItems(items: List<MenuItemEntity>)

    @Upsert
    suspend fun upsertMenuItem(item: MenuItemEntity)

    @Query("DELETE FROM menu_items")
    suspend fun clearAll()
}
