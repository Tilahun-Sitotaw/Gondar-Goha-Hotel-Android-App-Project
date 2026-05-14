package com.gohahotel.connect.domain.model

data class Booking(
    val id: String = "",
    val guestId: String = "",
    val guestName: String = "",
    val guestEmail: String = "",
    val guestPhone: String = "",
    val roomId: String = "",
    val roomName: String = "",
    val roomType: String = "",
    val checkInDate: String = "",
    val checkOutDate: String = "",
    val checkInTime: String = "14:00",  // 2 PM default
    val numberOfNights: Int = 1,
    val numberOfGuests: Int = 1,
    val totalPrice: Double = 0.0,
    val currency: String = "ETB",
    val status: BookingStatus = BookingStatus.PENDING,
    val specialRequests: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val maxCapacity: Int = 2,  // Room's max capacity for validation
    val roomPhotos: List<String> = emptyList(),  // Multiple room photos
    val paymentMethod: String = "",
    val paymentStatus: String = "PENDING",
    val referenceId: String = ""
)

enum class BookingStatus(val displayName: String, val color: String) {
    PENDING("Pending Confirmation", "#FFA500"),
    CONFIRMED("Confirmed", "#4CAF50"),
    CHECKED_IN("Checked In", "#2196F3"),
    CHECKED_OUT("Checked Out", "#9C27B0"),
    CANCELLED("Cancelled", "#F44336")
}
