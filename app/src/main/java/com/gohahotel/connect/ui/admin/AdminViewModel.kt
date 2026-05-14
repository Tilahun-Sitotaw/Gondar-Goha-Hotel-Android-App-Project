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
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val firestoreService: FirestoreService,
    private val foodRepository: FoodRepository,
    private val roomRepository: RoomRepository,
    private val cloudinaryService: com.gohahotel.connect.data.remote.CloudinaryService
) : ViewModel() {

    private val _menuItems = MutableStateFlow<List<MenuItem>>(emptyList())
    val menuItems: StateFlow<List<MenuItem>> = _menuItems.asStateFlow()

    private val _rooms = MutableStateFlow<List<HotelRoom>>(emptyList())
    val rooms: StateFlow<List<HotelRoom>> = _rooms.asStateFlow()

    private val _guideEntries = MutableStateFlow<List<com.gohahotel.connect.domain.model.GuideEntry>>(emptyList())
    val guideEntries: StateFlow<List<com.gohahotel.connect.domain.model.GuideEntry>> = _guideEntries.asStateFlow()

    private val _promotions = MutableStateFlow<List<Promotion>>(emptyList())
    val promotions: StateFlow<List<Promotion>> = _promotions.asStateFlow()

    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> = _orders.asStateFlow()

    private val _users = MutableStateFlow<List<Map<String, Any>>>(emptyList())
    val users: StateFlow<List<Map<String, Any>>> = _users.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Separate upload state so it doesn't get clobbered by fetch operations
    private val _isUploading = MutableStateFlow(false)
    val isUploading: StateFlow<Boolean> = _isUploading.asStateFlow()

    private val _uploadError = MutableStateFlow<String?>(null)
    val uploadError: StateFlow<String?> = _uploadError.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    val liveOrdersCount = _orders.map { list ->
        list.count { it.status != OrderStatus.DELIVERED && it.status != OrderStatus.CANCELLED }
    }

    val occupancyRate = _rooms.map { list ->
        if (list.isEmpty()) 0 else {
            val occupied = list.count { !it.isAvailable }
            (occupied.toFloat() / list.size * 100).toInt()
        }
    }

    private val _allBookings = MutableStateFlow<List<com.gohahotel.connect.domain.model.Booking>>(emptyList())
    val allBookings: StateFlow<List<com.gohahotel.connect.domain.model.Booking>> = _allBookings.asStateFlow()

    private val _allOrders = MutableStateFlow<List<Order>>(emptyList())
    val allOrders: StateFlow<List<Order>> = _allOrders.asStateFlow()

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
        fetchGuideEntries()
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

    fun fetchGuideEntries() {
        viewModelScope.launch {
            try {
                _guideEntries.value = firestoreService.fetchGuideEntries()
            } catch (e: Exception) {}
        }
    }

    fun saveGuideEntry(entry: com.gohahotel.connect.domain.model.GuideEntry) {
        viewModelScope.launch {
            try {
                // Assuming firestoreService has saveGuideEntry or similar
                // If not, I'll add it.
                val id = entry.id.ifBlank { com.google.firebase.firestore.FirebaseFirestore.getInstance().collection("guide").document().id }
                com.google.firebase.firestore.FirebaseFirestore.getInstance().collection("guide").document(id).set(entry.copy(id = id)).await()
                fetchGuideEntries()
            } catch (e: Exception) {}
        }
    }

    fun deleteGuideEntry(id: String) {
        viewModelScope.launch {
            try {
                com.google.firebase.firestore.FirebaseFirestore.getInstance().collection("guide").document(id).delete().await()
                fetchGuideEntries()
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
            _isLoading.value = true
            try {
                firestoreService.savePromotion(promotion)
                fetchPromotions()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to save event: ${e.message}"
            } finally {
                _isLoading.value = false
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
        // Start real-time listener — replaces one-shot fetch
        firestoreService.observeAllOrders()
            .catch { /* Firestore error — fall back silently */ }
            .onEach { 
                _orders.value = it
                _allOrders.value = it
            }
            .launchIn(viewModelScope)
    }

    fun loadAllBookings() {
        viewModelScope.launch {
            try {
                val bookings = firestoreService.getAllBookings()
                _allBookings.value = bookings
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun loadAllOrders() {
        viewModelScope.launch {
            try {
                val orders = firestoreService.fetchAllOrders()
                _allOrders.value = orders
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updateOrderStatus(orderId: String, status: OrderStatus) {
        viewModelScope.launch {
            try {
                firestoreService.updateOrderStatus(orderId, status.name)
                // No need to re-fetch — real-time listener will update automatically
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

    fun deleteUser(uid: String) {
        viewModelScope.launch {
            try {
                // Remove from Firestore users collection
                com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    .collection("users").document(uid).delete().await()
                
                // Note: We cannot delete users from Firebase Auth from client-side Android app
                // This requires Firebase Admin SDK which runs on server-side
                // For now, we only delete from Firestore
                // The user can still sign in with their email/password, but won't have profile data
                
                fetchUsers()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to remove user: ${e.message}"
            }
        }
    }

    fun uploadImage(uri: android.net.Uri, folder: String, onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            _isUploading.value = true
            _uploadError.value = null  // clear previous error
            try {
                val url = cloudinaryService.uploadFile(uri, folder)
                onSuccess(url)
            } catch (e: Exception) {
                _uploadError.value = "Upload failed: ${e.message}"
                _errorMessage.value = "Image upload failed: ${e.message}"
            } finally {
                _isUploading.value = false
            }
        }
    }

    fun uploadVideo(uri: android.net.Uri, folder: String, onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            _isUploading.value = true
            _uploadError.value = null
            try {
                val url = cloudinaryService.uploadFile(uri, folder)
                onSuccess(url)
            } catch (e: Exception) {
                _uploadError.value = "Upload failed: ${e.message}"
                _errorMessage.value = "Video upload failed: ${e.message}"
            } finally {
                _isUploading.value = false
            }
        }
    }
}
