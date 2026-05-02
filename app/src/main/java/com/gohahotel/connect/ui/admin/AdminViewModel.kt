package com.gohahotel.connect.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gohahotel.connect.data.remote.FirestoreService
import com.gohahotel.connect.data.repository.FoodRepository
import com.gohahotel.connect.data.repository.RoomRepository
import com.gohahotel.connect.domain.model.HotelRoom
import com.gohahotel.connect.domain.model.MenuItem
import com.gohahotel.connect.domain.model.Order
import com.gohahotel.connect.domain.model.OrderStatus
import com.gohahotel.connect.domain.model.Promotion
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val firestoreService: FirestoreService,
    private val foodRepository: FoodRepository,
    private val roomRepository: RoomRepository
) : ViewModel() {

    private val _menuItems = MutableStateFlow<List<MenuItem>>(emptyList())
    val menuItems: StateFlow<List<MenuItem>> = _menuItems.asStateFlow()

    private val _rooms = MutableStateFlow<List<HotelRoom>>(emptyList())
    val rooms: StateFlow<List<HotelRoom>> = _rooms.asStateFlow()

    private val _promotions = MutableStateFlow<List<Promotion>>(emptyList())
    val promotions: StateFlow<List<Promotion>> = _promotions.asStateFlow()

    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> = _orders.asStateFlow()

    private val _users = MutableStateFlow<List<Map<String, Any>>>(emptyList())
    val users: StateFlow<List<Map<String, Any>>> = _users.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _uploadProgress = MutableStateFlow<Float?>(null)
    val uploadProgress = _uploadProgress.asStateFlow()

    fun clearError() { _errorMessage.value = null }

    init {
        refreshAll()
    }

    fun refreshAll() {
        fetchPromotions()
        fetchOrders()
        fetchUsers()
        fetchMenuItems()
        fetchRooms()
    }

    fun fetchRooms() {
        viewModelScope.launch {
            try {
                _rooms.value = firestoreService.fetchRooms()
            } catch (e: Exception) {}
        }
    }

    fun saveRoom(room: HotelRoom) {
        viewModelScope.launch {
            try {
                roomRepository.saveRoom(room)
                fetchRooms()
            } catch (e: Exception) {}
        }
    }

    fun deleteRoom(roomId: String) {
        viewModelScope.launch {
            try {
                roomRepository.deleteRoom(roomId)
                fetchRooms()
            } catch (e: Exception) {}
        }
    }

    fun fetchMenuItems() {
        viewModelScope.launch {
            try {
                _menuItems.value = firestoreService.fetchMenuItems()
            } catch (e: Exception) {}
        }
    }

    fun saveMenuItem(item: MenuItem) {
        viewModelScope.launch {
            try {
                foodRepository.saveMenuItem(item)
                fetchMenuItems()
            } catch (e: Exception) {}
        }
    }

    fun deleteMenuItem(itemId: String) {
        viewModelScope.launch {
            try {
                foodRepository.deleteMenuItem(itemId)
                fetchMenuItems()
            } catch (e: Exception) {}
        }
    }

    fun fetchPromotions() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _promotions.value = firestoreService.fetchPromotions()
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun savePromotion(promotion: Promotion) {
        viewModelScope.launch {
            try {
                firestoreService.savePromotion(promotion)
                fetchPromotions()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun deletePromotion(promotionId: String) {
        viewModelScope.launch {
            try {
                firestoreService.deletePromotion(promotionId)
                fetchPromotions()
            } catch (e: Exception) {
            }
        }
    }

    fun fetchOrders() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _orders.value = firestoreService.fetchAllOrders()
            } catch (e: Exception) {
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateOrderStatus(orderId: String, status: OrderStatus) {
        viewModelScope.launch {
            try {
                firestoreService.updateOrderStatus(orderId, status.name)
                fetchOrders()
            } catch (e: Exception) {
            }
        }
    }

    fun fetchUsers() {
        viewModelScope.launch {
            try {
                _users.value = firestoreService.fetchAllUsers()
            } catch (e: Exception) {
            }
        }
    }

    fun updateUserRole(uid: String, role: String) {
        viewModelScope.launch {
            try {
                firestoreService.updateUserRole(uid, role)
                fetchUsers()
            } catch (e: Exception) {}
        }
    }

    fun uploadImage(uri: android.net.Uri, folder: String, onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val fileName = "img_${System.currentTimeMillis()}.jpg"
                val url = firestoreService.uploadFile(uri, "$folder/$fileName")
                onSuccess(url)
            } catch (e: Exception) {
                _errorMessage.value = "Image upload failed: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun uploadVideo(uri: android.net.Uri, folder: String, onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val fileName = "vid_${System.currentTimeMillis()}.mp4"
                val url = firestoreService.uploadFile(uri, "$folder/$fileName")
                onSuccess(url)
            } catch (e: Exception) {
                _errorMessage.value = "Video upload failed: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
