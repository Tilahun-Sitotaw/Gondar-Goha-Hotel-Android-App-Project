package com.gohahotel.connect.data.local.dao

import androidx.room.*
import com.gohahotel.connect.data.local.entity.RoomEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RoomDao {
    @Query("SELECT * FROM rooms ORDER BY pricePerNight ASC")
    fun getAllRooms(): Flow<List<RoomEntity>>

    @Query("SELECT * FROM rooms WHERE type = :type ORDER BY pricePerNight ASC")
    fun getRoomsByType(type: String): Flow<List<RoomEntity>>

    @Query("SELECT * FROM rooms WHERE id = :id")
    suspend fun getRoomById(id: String): RoomEntity?

    @Query("SELECT * FROM rooms WHERE isAvailable = 1")
    fun getAvailableRooms(): Flow<List<RoomEntity>>

    @Upsert
    suspend fun upsertRooms(rooms: List<RoomEntity>)

    @Upsert
    suspend fun upsertRoom(room: RoomEntity)

    @Query("DELETE FROM rooms")
    suspend fun clearAll()

    @Query("SELECT * FROM rooms WHERE cachedAt < :expiry")
    suspend fun getStaleRooms(expiry: Long): List<RoomEntity>
}
