package com.gohahotel.connect.domain.model

import java.util.Date

data class Order(
    val id: String = "",
    val guestId: String = "",
    val guestName: String = "",
    val roomNumber: String = "",
    val items: List<OrderItem> = emptyList(),
    val type: OrderType = OrderType.FOOD,
    val status: OrderStatus = OrderStatus.RECEIVED,
    val specialInstructions: String = "",
    val totalAmount: Double = 0.0,
    val currency: String = "ETB",
    val createdAt: Date = Date(),
    val estimatedDeliveryMinutes: Int = 30
)

data class OrderItem(
    val menuItemId: String = "",
    val menuItemName: String = "",
    val quantity: Int = 1,
    val unitPrice: Double = 0.0,
    val customization: String = "",
    val subtotal: Double = 0.0
)

enum class OrderType { FOOD, ROOM_REQUEST }

enum class OrderStatus(val displayName: String, val displayNameAmharic: String, val step: Int) {
    PENDING("Pending", "በመጠባበቅ ላይ", 0),
    PREPARING("Preparing", "እየተዘጋጀ ነው", 1),
    READY("Ready", "ዝግጁ ነው", 2),
    RECEIVED("Order Received", "ትዕዛዝ ተቀብሏል", 0),
    ON_THE_WAY("On the Way", "በመምጣት ላይ ነው", 2),
    DELIVERED("Delivered", "ደርሷል", 3),
    CANCELLED("Cancelled", "ተሰርዟል", -1)
}
