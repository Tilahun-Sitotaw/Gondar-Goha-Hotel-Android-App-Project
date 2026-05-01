package com.gohahotel.connect.data.repository

import com.gohahotel.connect.data.local.dao.RoomDao
import com.gohahotel.connect.data.local.toDomain
import com.gohahotel.connect.data.local.toEntity
import com.gohahotel.connect.data.remote.FirestoreService
import com.gohahotel.connect.domain.model.Booking
import com.gohahotel.connect.domain.model.HotelRoom
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomRepository @Inject constructor(
    private val roomDao: RoomDao,
    private val firestoreService: FirestoreService
) {
    fun getRooms(): Flow<List<HotelRoom>> =
        roomDao.getAllRooms().map { entities -> entities.map { it.toDomain() } }

    fun getRoomsByType(type: String): Flow<List<HotelRoom>> =
        roomDao.getRoomsByType(type).map { entities -> entities.map { it.toDomain() } }

    fun getAvailableRooms(): Flow<List<HotelRoom>> =
        roomDao.getAvailableRooms().map { entities -> entities.map { it.toDomain() } }

    suspend fun getRoomById(id: String): HotelRoom? =
        roomDao.getRoomById(id)?.toDomain()

    suspend fun refreshRooms() {
        val rooms = firestoreService.fetchRooms()
        roomDao.clearAll()
        roomDao.upsertRooms(rooms.map { it.toEntity() })
    }

    suspend fun createBooking(booking: Booking): String =
        firestoreService.createBooking(booking)

    suspend fun getBookingsForGuest(guestId: String): List<Booking> =
        firestoreService.getBookingsByGuest(guestId)

    suspend fun submitInRoomRequest(
        roomNumber: String,
        guestId: String,
        requestType: String,
        notes: String
    ): String = firestoreService.submitInRoomRequest(
        mapOf(
            "roomNumber"  to roomNumber,
            "guestId"     to guestId,
            "requestType" to requestType,
            "notes"       to notes,
            "status"      to "PENDING",
            "createdAt"   to System.currentTimeMillis()
        )
    )

    suspend fun saveRoom(room: HotelRoom) = firestoreService.saveRoom(room)
    suspend fun deleteRoom(roomId: String) = firestoreService.deleteRoom(roomId)
}
