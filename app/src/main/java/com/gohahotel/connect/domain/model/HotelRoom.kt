package com.gohahotel.connect.domain.model

data class HotelRoom(
    val id: String = "",
    val name: String = "",
    val type: RoomType = RoomType.STANDARD,
    val description: String = "",
    val pricePerNight: Double = 0.0,
    val currency: String = "ETB",
    val capacity: Int = 2,
    val bedType: String = "",
    val floorNumber: Int = 1,
    val amenities: List<String> = emptyList(),
    val imageUrls: List<String> = emptyList(),
    val isAvailable: Boolean = true,
    val rating: Float = 0f,
    val reviewCount: Int = 0,
    val hasView: Boolean = false,
    val hasMountainView: Boolean = false
)

enum class RoomType(val displayName: String) {
    STANDARD("Standard"),
    TWIN("Twin"),
    KING("King"),
    SUITE("Suite"),
    PRESIDENTIAL("Presidential Suite")
}
