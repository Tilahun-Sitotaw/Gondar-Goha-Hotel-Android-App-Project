package com.gohahotel.connect.domain.model

data class GuideEntry(
    val id: String = "",
    val title: String = "",
    val titleAmharic: String = "",
    val titleFrench: String = "",
    val summary: String = "",
    val summaryAmharic: String = "",
    val summaryFrench: String = "",
    val content: String = "",
    val contentAmharic: String = "",
    val contentFrench: String = "",
    val category: GuideCategory = GuideCategory.HERITAGE,
    val imageUrls: List<String> = emptyList(),
    val latitude: Double = 12.6030,
    val longitude: Double = 37.4667,
    val distanceFromHotelKm: Double = 0.0,
    val openingHours: String = "",
    val entryFee: String = "",
    val tags: List<String> = emptyList()
)

enum class GuideCategory(val displayName: String, val icon: String) {
    HERITAGE("Heritage Sites", "🏛️"),
    CHURCHES("Churches", "⛪"),
    MARKETS("Markets & Bazaars", "🛍️"),
    NATURE("Nature & Scenery", "🌄"),
    HISTORY("History & Culture", "📜"),
    DINING("Local Dining", "🍽️")
}
