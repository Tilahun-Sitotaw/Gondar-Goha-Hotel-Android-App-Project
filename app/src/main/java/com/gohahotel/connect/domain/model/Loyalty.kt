package com.gohahotel.connect.domain.model

enum class LoyaltyTier(val displayName: String, val minPoints: Int) {
    SILVER("Silver Member", 0),
    GOLD("Gold Member", 500),
    PLATINUM("Platinum Elite", 1500)
}

data class LoyaltyInfo(
    val guestId: String = "",
    val points: Int = 0,
    val tier: LoyaltyTier = LoyaltyTier.SILVER,
    val totalSpent: Double = 0.0,
    val rewardsAvailable: List<Reward> = emptyList()
)

data class Reward(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val pointCost: Int = 0,
    val imageUrl: String = ""
)
