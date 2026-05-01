package com.gohahotel.connect.domain.model

data class Promotion(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val videoUrl: String = "", // For hotel videos or cultural clips
    val type: PromotionType = PromotionType.EVENT,
    val isActive: Boolean = true,
    val date: String = "" // For events
)

enum class PromotionType {
    EVENT, PROMOTION, CULTURAL, VIDEO
}
