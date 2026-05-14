package com.gohahotel.connect.domain.model

data class RoomAvailability(
    val roomId: String = "",
    val roomName: String = "",
    val status: RoomStatus = RoomStatus.FREE,
    val currentGuest: String = "",
    val checkInDate: String = "",
    val checkOutDate: String = "",
    val nextAvailableDate: String = "",
    val occupancyPercentage: Int = 0
)

enum class RoomStatus(val displayName: String, val color: String) {
    FREE("Free", "#4CAF50"),
    RESERVED("Reserved", "#FF9800"),
    OCCUPIED("Occupied", "#2196F3"),
    MAINTENANCE("Maintenance", "#9C27B0"),
    CLEANING("Cleaning", "#00BCD4")
}
