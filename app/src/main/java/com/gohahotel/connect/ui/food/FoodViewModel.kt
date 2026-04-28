package com.gohahotel.connect.ui.food

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gohahotel.connect.data.repository.FoodRepository
import com.gohahotel.connect.domain.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

data class CartItem(val menuItem: MenuItem, val quantity: Int, val customization: String = "")

data class FoodUiState(
    val menuItems: List<MenuItem> = emptyList(),
    val featuredItems: List<MenuItem> = emptyList(),
    val cartItems: List<CartItem> = emptyList(),
    val selectedCategory: MenuCategory = MenuCategory.ETHIOPIAN,
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val activeOrderId: String? = null,
    val activeOrder: Order? = null,
    val error: String? = null
)

@HiltViewModel
class FoodViewModel @Inject constructor(
    private val foodRepository: FoodRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FoodUiState())
    val uiState = _uiState.asStateFlow()

    val cartTotal get() = _uiState.value.cartItems.sumOf { it.menuItem.price * it.quantity }
    val cartCount get() = _uiState.value.cartItems.sumOf { it.quantity }

    init { loadMenu() }

    private fun loadMenu() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            foodRepository.getMenuByCategory(MenuCategory.ETHIOPIAN).collect { items ->
                _uiState.update { it.copy(menuItems = items, isLoading = false) }
            }
        }
        viewModelScope.launch {
            foodRepository.getFeaturedItems().collect { items ->
                _uiState.update { it.copy(featuredItems = items) }
            }
        }
        viewModelScope.launch {
            try { foodRepository.refreshMenu() } catch (_: Exception) {}
        }
    }

    fun selectCategory(category: MenuCategory) {
        _uiState.update { it.copy(selectedCategory = category, searchQuery = "") }
        viewModelScope.launch {
            foodRepository.getMenuByCategory(category).collect { items ->
                _uiState.update { it.copy(menuItems = items) }
            }
        }
    }

    fun search(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        if (query.isBlank()) {
            selectCategory(_uiState.value.selectedCategory)
        } else {
            viewModelScope.launch {
                foodRepository.searchMenu(query).collect { items ->
                    _uiState.update { it.copy(menuItems = items) }
                }
            }
        }
    }

    fun addToCart(item: MenuItem, customization: String = "") {
        val current = _uiState.value.cartItems.toMutableList()
        val existing = current.indexOfFirst { it.menuItem.id == item.id && it.customization == customization }
        if (existing >= 0) {
            current[existing] = current[existing].copy(quantity = current[existing].quantity + 1)
        } else {
            current.add(CartItem(item, 1, customization))
        }
        _uiState.update { it.copy(cartItems = current) }
    }

    fun removeFromCart(item: MenuItem) {
        val current = _uiState.value.cartItems.toMutableList()
        val idx = current.indexOfFirst { it.menuItem.id == item.id }
        if (idx >= 0) {
            if (current[idx].quantity > 1) current[idx] = current[idx].copy(quantity = current[idx].quantity - 1)
            else current.removeAt(idx)
        }
        _uiState.update { it.copy(cartItems = current) }
    }

    fun clearCart() = _uiState.update { it.copy(cartItems = emptyList()) }

    fun placeOrder(roomNumber: String, guestId: String, guestName: String, instructions: String) {
        val items = _uiState.value.cartItems
        if (items.isEmpty()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val orderItems = items.map { cart ->
                    OrderItem(
                        menuItemId   = cart.menuItem.id,
                        menuItemName = cart.menuItem.name,
                        quantity     = cart.quantity,
                        unitPrice    = cart.menuItem.price,
                        customization = cart.customization,
                        subtotal     = cart.menuItem.price * cart.quantity
                    )
                }
                val orderId = foodRepository.placeOrder(
                    Order(
                        guestId             = guestId,
                        guestName           = guestName,
                        roomNumber          = roomNumber,
                        items               = orderItems,
                        totalAmount         = cartTotal,
                        currency            = "ETB",
                        specialInstructions = instructions,
                        createdAt           = Date()
                    )
                )
                _uiState.update { it.copy(isLoading = false, activeOrderId = orderId, cartItems = emptyList()) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun observeOrder(orderId: String) {
        viewModelScope.launch {
            foodRepository.observeOrderStatus(orderId).collect { order ->
                _uiState.update { it.copy(activeOrder = order) }
            }
        }
    }
}
