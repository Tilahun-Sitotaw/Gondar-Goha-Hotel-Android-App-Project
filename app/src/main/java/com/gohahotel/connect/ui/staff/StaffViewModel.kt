package com.gohahotel.connect.ui.staff

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gohahotel.connect.data.repository.StaffRepository
import com.gohahotel.connect.domain.model.Booking
import com.gohahotel.connect.domain.model.Order
import com.gohahotel.connect.domain.model.OrderStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StaffUiState(
    val orders: List<Order> = emptyList(),
    val bookings: List<Booking> = emptyList(),
    val isLoading: Boolean = false,
    val selectedTab: Int = 0,
    val error: String? = null
)

@HiltViewModel
class StaffViewModel @Inject constructor(
    private val staffRepository: StaffRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StaffUiState())
    val uiState = _uiState.asStateFlow()

    init {
        observeOrders()
        observeBookings()
    }

    private fun observeOrders() {
        viewModelScope.launch {
            staffRepository.observeActiveOrders().collect { orders ->
                _uiState.update { it.copy(orders = orders) }
            }
        }
    }

    private fun observeBookings() {
        viewModelScope.launch {
            staffRepository.observeTodayBookings().collect { bookings ->
                _uiState.update { it.copy(bookings = bookings) }
            }
        }
    }

    fun selectTab(index: Int) {
        _uiState.update { it.copy(selectedTab = index) }
    }

    fun updateOrderStatus(orderId: String, currentStatus: OrderStatus) {
        val nextStatus = when (currentStatus) {
            OrderStatus.RECEIVED -> OrderStatus.PREPARING
            OrderStatus.PREPARING -> OrderStatus.ON_THE_WAY
            OrderStatus.ON_THE_WAY -> OrderStatus.DELIVERED
            else -> OrderStatus.DELIVERED
        }
        
        viewModelScope.launch {
            try {
                staffRepository.updateOrderStatus(orderId, nextStatus)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun updateBookingStatus(bookingId: String, status: String) {
        viewModelScope.launch {
            try {
                staffRepository.updateBookingStatus(bookingId, status)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }
}
