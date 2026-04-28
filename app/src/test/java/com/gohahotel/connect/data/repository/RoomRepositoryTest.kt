package com.gohahotel.connect.data.repository

import com.gohahotel.connect.data.local.dao.RoomDao
import com.gohahotel.connect.data.remote.FirestoreService
import com.gohahotel.connect.domain.model.HotelRoom
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class RoomRepositoryTest {

    private val roomDao: RoomDao = mockk(relaxed = true)
    private val firestoreService: FirestoreService = mockk(relaxed = true)
    private val repository = RoomRepository(roomDao, firestoreService)

    @Test
    fun `refreshRooms fetches from firestore and saves to local dao`() = runTest {
        val remoteRooms = listOf(
            HotelRoom(id = "101", name = "Luxury Suite")
        )
        
        // FirestoreService.fetchRooms() is a suspend function returning List<HotelRoom>
        coEvery { firestoreService.fetchRooms() } returns remoteRooms

        repository.refreshRooms()

        // RoomDao.upsertRooms() is a suspend function
        coVerify { roomDao.upsertRooms(any()) }
    }
}
