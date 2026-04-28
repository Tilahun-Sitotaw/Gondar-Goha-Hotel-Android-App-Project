package com.gohahotel.connect.ui.rooms

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gohahotel.connect.data.repository.RoomRepository
import com.gohahotel.connect.domain.model.Booking
import com.gohahotel.connect.domain.model.HotelRoom
import com.gohahotel.connect.domain.model.RoomType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RoomsUiState(
    val rooms: List<HotelRoom>       = emptyList(),
    val selectedRoom: HotelRoom?     = null,
    val selectedFilter: RoomType?    = null,
    val isLoading: Boolean           = false,
    val isBookingSuccess: Boolean    = false,
    val error: String?               = null,
    val requestSubmitted: Boolean    = false
)

@HiltViewModel
class RoomViewModel @Inject constructor(
    private val roomRepository: RoomRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RoomsUiState())
    val uiState = _uiState.asStateFlow()

    init { loadRooms() }

    private fun loadRooms() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            roomRepository.getAvailableRooms().collect { rooms ->
                _uiState.update { it.copy(rooms = rooms, isLoading = false) }
            }
        }
        viewModelScope.launch {
            try { roomRepository.refreshRooms() } catch (_: Exception) { /* offline ok */ }
        }
    }

    fun selectFilter(type: RoomType?) {
        _uiState.update { it.copy(selectedFilter = type) }
        viewModelScope.launch {
            val flow = if (type == null) roomRepository.getAvailableRooms()
                       else roomRepository.getRoomsByType(type.name)
            flow.collect { rooms -> _uiState.update { it.copy(rooms = rooms) } }
        }
    }

    fun loadRoomDetail(roomId: String) {
        viewModelScope.launch {
            val room = roomRepository.getRoomById(roomId)
            _uiState.update { it.copy(selectedRoom = room) }
        }
    }

    fun bookRoom(
        guestId: String, guestName: String, guestEmail: String,
        roomId: String, roomName: String, roomType: String,
        checkIn: String, checkOut: String, nights: Int, guests: Int,
        totalPrice: Double, specialRequests: String
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                roomRepository.createBooking(
                    Booking(
                        guestId        = guestId,
                        guestName      = guestName,
                        guestEmail     = guestEmail,
                        roomId         = roomId,
                        roomName       = roomName,
                        roomType       = roomType,
                        checkInDate    = checkIn,
                        checkOutDate   = checkOut,
                        numberOfNights = nights,
                        numberOfGuests = guests,
                        totalPrice     = totalPrice,
                        specialRequests = specialRequests
                    )
                )
                _uiState.update { it.copy(isLoading = false, isBookingSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun submitInRoomRequest(roomNumber: String, guestId: String, type: String, notes: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                roomRepository.submitInRoomRequest(roomNumber, guestId, type, notes)
                _uiState.update { it.copy(isLoading = false, requestSubmitted = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun resetBookingState() = _uiState.update { it.copy(isBookingSuccess = false, requestSubmitted = false) }
}
