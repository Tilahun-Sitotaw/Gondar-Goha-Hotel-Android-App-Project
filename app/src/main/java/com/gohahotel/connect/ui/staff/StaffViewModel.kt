package com.gohahotel.connect.ui.staff

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gohahotel.connect.data.repository.RoomRepository
import com.gohahotel.connect.data.repository.FoodRepository
import com.gohahotel.connect.domain.model.Booking
import com.gohahotel.connect.domain.model.BookingStatus
import com.gohahotel.connect.domain.model.Order
import com.gohahotel.connect.domain.model.OrderStatus
import com.gohahotel.connect.domain.model.RoomAvailability
import com.gohahotel.connect.domain.model.RoomStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StaffUiState(
    val userRole: String = "RECEPTION",
    val bookings: List<Booking> = emptyList(),
    val orders: List<Order> = emptyList(),
    val selectedTab: Int = 0,
    val isLoading: Boolean = false
)

@HiltViewModel
class StaffViewModel @Inject constructor(
    private val roomRepository: RoomRepository,
    private val foodRepository: FoodRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StaffUiState())
    val uiState: StateFlow<StaffUiState> = _uiState.asStateFlow()

    private val _allBookings = MutableStateFlow<List<Booking>>(emptyList())
    val allBookings: StateFlow<List<Booking>> = _allBookings

    private val _roomStatuses = MutableStateFlow<List<RoomAvailability>>(emptyList())
    val roomStatuses: StateFlow<List<RoomAvailability>> = _roomStatuses

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun loadAllBookings() {
        viewModelScope.launch {
            _isLoading.value = true
            _uiState.update { it.copy(isLoading = true) }
            try {
                // Fetch all bookings from repository
                val bookings = roomRepository.getAllBookings()
                _allBookings.value = bookings
                _uiState.update { it.copy(bookings = bookings) }
                
                // Also load orders if this is the staff dashboard
                val orders = foodRepository.getAllOrders()
                _uiState.update { it.copy(orders = orders) }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun loadRoomStatuses() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Calculate room availability based on bookings
                val bookings = roomRepository.getAllBookings()
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
                // Get the booking and update its status
                val booking = _allBookings.value.find { it.id == bookingId }
                if (booking != null) {
                    roomRepository.updateBooking(booking.copy(status = newStatus))
                    loadAllBookings()
                    loadRoomStatuses()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updateOrderStatus(orderId: String, newStatus: OrderStatus) {
        viewModelScope.launch {
            try {
                foodRepository.updateOrderStatus(orderId, newStatus)
                loadAllBookings() // Re-loads both bookings and orders
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun selectTab(index: Int) {
        _uiState.update { it.copy(selectedTab = index) }
    }

    fun setUserRole(role: String) {
        _uiState.update { it.copy(userRole = role) }
    }
}
