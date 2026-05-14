package com.gohahotel.connect.ui.staff

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gohahotel.connect.data.repository.BookingRepository
import com.gohahotel.connect.domain.model.Booking
import com.gohahotel.connect.domain.model.BookingStatus
import com.gohahotel.connect.domain.model.RoomAvailability
import com.gohahotel.connect.domain.model.RoomStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StaffViewModel @Inject constructor(
    private val bookingRepository: BookingRepository
) : ViewModel() {

    private val _allBookings = MutableStateFlow<List<Booking>>(emptyList())
    val allBookings: StateFlow<List<Booking>> = _allBookings

    private val _roomStatuses = MutableStateFlow<List<RoomAvailability>>(emptyList())
    val roomStatuses: StateFlow<List<RoomAvailability>> = _roomStatuses

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun loadAllBookings() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Fetch all bookings from repository
                val bookings = bookingRepository.getAllBookings()
                _allBookings.value = bookings
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadRoomStatuses() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Calculate room availability based on bookings
                val bookings = bookingRepository.getAllBookings()
                val roomStatuses = calculateRoomStatuses(bookings)
                _roomStatuses.value = roomStatuses
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun calculateRoomStatuses(bookings: List<Booking>): List<RoomAvailability> {
        val today = java.text.SimpleDateFormat("yyyy-MM-dd").format(java.util.Date())
        
        return bookings.groupBy { it.roomId }
            .map { (roomId, roomBookings) ->
                val activeBooking = roomBookings.find { booking ->
                    booking.checkInDate <= today && booking.checkOutDate >= today
                }

                val nextBooking = roomBookings.filter { it.checkInDate > today }
                    .minByOrNull { it.checkInDate }

                RoomAvailability(
                    roomId = roomId,
                    roomName = roomBookings.first().roomName,
                    status = when {
                        activeBooking != null -> RoomStatus.OCCUPIED
                        nextBooking != null -> RoomStatus.RESERVED
                        else -> RoomStatus.FREE
                    },
                    currentGuest = activeBooking?.guestName ?: "",
                    checkInDate = activeBooking?.checkInDate ?: "",
                    checkOutDate = activeBooking?.checkOutDate ?: "",
                    nextAvailableDate = nextBooking?.checkInDate ?: "",
                    occupancyPercentage = if (activeBooking != null) 100 else 0
                )
            }
    }

    fun updateBookingStatus(bookingId: String, newStatus: BookingStatus) {
        viewModelScope.launch {
            try {
                bookingRepository.updateBookingStatus(bookingId, newStatus)
                loadAllBookings()
                loadRoomStatuses()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
