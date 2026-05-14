package com.gohahotel.connect.domain.model

data class RoomAvailability(
    val roomId: String = "",
    val roomName: String = "",
    val status: RoomStatus = RoomStatus.FREE,
    val currentGuest: String = "",
    val checkInDate: String = "",
    val checkOutDate: String = "",
    val nextAvailableDate: String = "",
    val occupancyPercentage: Int = 0,
    val daysUntilAvailable: Int = 0  // Days until room becomes available
)

enum class RoomStatus(val displayName: String, val color: String, val userFriendlyName: String) {
    FREE("Free", "#4CAF50", "Available Now"),
    RESERVED("Reserved", "#FF9800", "Reserved"),
    OCCUPIED("Occupied", "#2196F3", "Guest Checked In"),
    MAINTENANCE("Maintenance", "#9C27B0", "Under Maintenance"),
    CLEANING("Cleaning", "#00BCD4", "Being Cleaned")
}
