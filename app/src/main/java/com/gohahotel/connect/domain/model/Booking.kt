package com.gohahotel.connect.domain.model

data class Booking(
    val id: String = "",
    val guestId: String = "",
    val guestName: String = "",
    val guestEmail: String = "",
    val roomId: String = "",
    val roomName: String = "",
    val roomType: String = "",
    val checkInDate: String = "",
    val checkOutDate: String = "",
    val numberOfNights: Int = 1,
    val numberOfGuests: Int = 1,
    val totalPrice: Double = 0.0,
    val currency: String = "ETB",
    val status: BookingStatus = BookingStatus.PENDING,
    val specialRequests: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

enum class BookingStatus(val displayName: String) {
    PENDING("Pending Confirmation"),
    CONFIRMED("Confirmed"),
    CHECKED_IN("Checked In"),
    CHECKED_OUT("Checked Out"),
    CANCELLED("Cancelled")
}
