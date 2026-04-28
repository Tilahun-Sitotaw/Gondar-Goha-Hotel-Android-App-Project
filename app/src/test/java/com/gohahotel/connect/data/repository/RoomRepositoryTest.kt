package com.gohahotel.connect.data.repository

import com.gohahotel.connect.data.local.dao.RoomDao
import com.gohahotel.connect.data.local.entity.RoomEntity
import com.gohahotel.connect.data.remote.FirestoreService
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test

class RoomRepositoryTest {

    private val roomDao: RoomDao = mockk(relaxed = true)
    private val firestoreService: FirestoreService = mockk(relaxed = true)
    private val repository = RoomRepository(roomDao, firestoreService)

    @Test
    fun `refreshRooms fetches from firestore and saves to local dao`() = runTest {
        val remoteRooms = listOf(
            mockk<com.gohahotel.connect.domain.model.HotelRoom>(relaxed = true) {
                every { id } returns "101"
            }
        )
        coEvery { firestoreService.getRooms() } returns flowOf(remoteRooms)

        repository.refreshRooms()

        coVerify { roomDao.insertRooms(any()) }
    }
}
