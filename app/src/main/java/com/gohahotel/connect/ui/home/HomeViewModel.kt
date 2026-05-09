package com.gohahotel.connect.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.*
import com.gohahotel.connect.core.notifications.SunsetAlertWorker
import com.gohahotel.connect.data.repository.AuthRepository
import com.gohahotel.connect.data.repository.RoomRepository
import com.gohahotel.connect.data.remote.FirestoreService
import com.gohahotel.connect.domain.model.Booking
import com.gohahotel.connect.domain.model.Promotion
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import android.content.Context
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject

data class HomeUiState(
    val guestName: String           = "Valued Guest",
    val userRole: String            = "GUEST",
    val isGuest: Boolean            = false,
    val roomNumber: String          = "",
    val checkIn: String             = "",
    val checkOut: String            = "",
    val greeting: String            = "Day",
    val showSunsetAlert: Boolean    = false,
    val sunsetMinutes: Int          = 30,
    val promotions: List<Promotion> = emptyList(),
    val activeBooking: Booking?     = null,
    val isLoading: Boolean          = false
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val firestoreService: FirestoreService,
    private val roomRepository: RoomRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadUserInfo()
        updateGreeting()
        loadPromotions()
    }

    private fun loadUserInfo() {
        val user = authRepository.currentUser
        val uid  = user?.uid
        val email = user?.email
        val isAnon = user?.isAnonymous == true

        _uiState.update {
            it.copy(
                guestName = if (isAnon) "Valued Guest"
                            else user?.displayName?.ifBlank { "Valued Guest" } ?: "Valued Guest",
                isGuest = isAnon
            )
        }

        if (uid != null && !isAnon) {
            viewModelScope.launch {
                val role = if (email?.lowercase()?.trim() == "gohahotel34@gmail.com") "ADMIN"
                           else firestoreService.getUserRole(uid)
                _uiState.update { it.copy(userRole = role) }
                // Load active booking for this guest
                loadActiveBooking(uid)
            }
        }
    }

    private fun loadActiveBooking(guestId: String) {
        viewModelScope.launch {
            try {
                val bookings = roomRepository.getBookingsForGuest(guestId)
                val active = bookings.firstOrNull { it.status.name == "CONFIRMED" || it.status.name == "CHECKED_IN" }
                if (active != null) {
                    _uiState.update {
                        it.copy(
                            activeBooking = active,
                            roomNumber    = active.roomName,
                            checkIn       = active.checkInDate,
                            checkOut      = active.checkOutDate
                        )
                    }
                }
            } catch (_: Exception) {}
        }
    }

    private fun loadPromotions() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val promos = firestoreService.fetchPromotions().filter { it.isActive }
                _uiState.update { it.copy(promotions = promos, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun updateGreeting() {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val greeting = when {
            hour < 12 -> "Morning"
            hour < 17 -> "Afternoon"
            else      -> "Evening"
        }
        _uiState.update { it.copy(greeting = greeting) }
    }

    fun scheduleSunsetAlert() {
        val workRequest = OneTimeWorkRequestBuilder<SunsetAlertWorker>()
            .setInitialDelay(0, TimeUnit.MILLISECONDS)
            .build()
        WorkManager.getInstance(context)
            .enqueueUniqueWork("sunset_alert", ExistingWorkPolicy.REPLACE, workRequest)
        _uiState.update { it.copy(showSunsetAlert = true, sunsetMinutes = 30) }
    }

    fun dismissSunsetAlert() {
        _uiState.update { it.copy(showSunsetAlert = false) }
    }
}
