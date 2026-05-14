package com.gohahotel.connect.ui.kitchen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gohahotel.connect.data.repository.FoodRepository
import com.gohahotel.connect.domain.model.Order
import com.gohahotel.connect.domain.model.OrderStatus
import com.gohahotel.connect.domain.model.OrderType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class KitchenViewModel @Inject constructor(
    private val foodRepository: FoodRepository
) : ViewModel() {

    private val _foodOrders = MutableStateFlow<List<Order>>(emptyList())
    val foodOrders: StateFlow<List<Order>> = _foodOrders

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun loadFoodOrders() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Fetch all food orders (not room requests)
                val orders = foodRepository.getAllOrders()
                    .filter { it.type == OrderType.FOOD }
                    .sortedByDescending { it.createdAt }
                
                _foodOrders.value = orders
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load orders"
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateOrderStatus(orderId: String, newStatus: OrderStatus) {
        viewModelScope.launch {
            try {
                foodRepository.updateOrderStatus(orderId, newStatus)
                loadFoodOrders()
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to update order"
                e.printStackTrace()
            }
        }
    }

    fun markOrderReady(orderId: String) {
        updateOrderStatus(orderId, OrderStatus.READY)
    }

    fun markOrderDelivered(orderId: String) {
        updateOrderStatus(orderId, OrderStatus.DELIVERED)
    }

    fun cancelOrder(orderId: String) {
        updateOrderStatus(orderId, OrderStatus.CANCELLED)
    }

    fun clearError() {
        _error.value = null
    }
}
