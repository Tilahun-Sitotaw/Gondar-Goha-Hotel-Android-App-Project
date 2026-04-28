package com.gohahotel.connect.ui.home

import android.content.Context
import com.gohahotel.connect.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseUser
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private lateinit var viewModel: HomeViewModel
    private val authRepository: AuthRepository = mockk(relaxed = true)
    private val context: Context = mockk(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadUserInfo updates UI state with guest name`() = runTest {
        val mockUser: FirebaseUser = mockk()
        every { mockUser.displayName } returns "Tilahun"
        every { authRepository.currentUser } returns mockUser

        viewModel = HomeViewModel(authRepository, context)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("Tilahun", viewModel.uiState.value.guestName)
    }

    @Test
    fun `loadUserInfo sets default name when user is anonymous`() = runTest {
        every { authRepository.currentUser } returns null

        viewModel = HomeViewModel(authRepository, context)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("Valued Guest", viewModel.uiState.value.guestName)
    }
}
