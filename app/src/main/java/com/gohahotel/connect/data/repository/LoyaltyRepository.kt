package com.gohahotel.connect.data.repository

import com.gohahotel.connect.domain.model.LoyaltyInfo
import com.gohahotel.connect.domain.model.LoyaltyTier
import com.gohahotel.connect.domain.model.Reward
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoyaltyRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    suspend fun getLoyaltyInfo(guestId: String): LoyaltyInfo {
        return try {
            val doc = firestore.collection("loyalty").document(guestId).get().await()
            doc.toObject(LoyaltyInfo::class.java) ?: LoyaltyInfo(guestId = guestId)
        } catch (e: Exception) {
            LoyaltyInfo(guestId = guestId)
        }
    }

    suspend fun addPoints(guestId: String, pointsToAdd: Int) {
        val current = getLoyaltyInfo(guestId)
        val newPoints = current.points + pointsToAdd
        val newTier = when {
            newPoints >= LoyaltyTier.PLATINUM.minPoints -> LoyaltyTier.PLATINUM
            newPoints >= LoyaltyTier.GOLD.minPoints -> LoyaltyTier.GOLD
            else -> LoyaltyTier.SILVER
        }
        
        firestore.collection("loyalty").document(guestId).set(
            current.copy(points = newPoints, tier = newTier)
        ).await()
    }

    fun getAvailableRewards(): List<Reward> = listOf(
        Reward("1", "Sunset Drink", "Free cocktail at the terrace", 100),
        Reward("2", "Late Checkout", "Stay until 4:00 PM", 300),
        Reward("3", "Spa Discount", "20% off any massage", 500),
        Reward("4", "Airport Transfer", "Complimentary luxury shuttle", 1000)
    )
}
