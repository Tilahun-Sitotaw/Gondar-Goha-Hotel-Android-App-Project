package com.gohahotel.connect.data.local.dao

import androidx.room.*
import com.gohahotel.connect.data.local.entity.GuideEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GuideDao {
    @Query("SELECT * FROM guide_entries ORDER BY category, title")
    fun getAllEntries(): Flow<List<GuideEntryEntity>>

    @Query("SELECT * FROM guide_entries WHERE category = :category")
    fun getEntriesByCategory(category: String): Flow<List<GuideEntryEntity>>

    @Query("SELECT * FROM guide_entries WHERE id = :id")
    suspend fun getEntryById(id: String): GuideEntryEntity?

    @Query("SELECT * FROM guide_entries WHERE (title LIKE '%' || :query || '%' OR titleAmharic LIKE '%' || :query || '%' OR tags LIKE '%' || :query || '%')")
    fun searchEntries(query: String): Flow<List<GuideEntryEntity>>

    @Query("SELECT * FROM guide_entries ORDER BY distanceFromHotelKm ASC LIMIT 5")
    fun getNearbyEntries(): Flow<List<GuideEntryEntity>>

    @Upsert
    suspend fun upsertEntries(entries: List<GuideEntryEntity>)

    @Upsert
    suspend fun upsertEntry(entry: GuideEntryEntity)

    @Query("DELETE FROM guide_entries")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM guide_entries")
    suspend fun getCount(): Int
}
