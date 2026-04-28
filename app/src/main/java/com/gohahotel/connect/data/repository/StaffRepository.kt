package com.gohahotel.connect.data.repository

import com.gohahotel.connect.domain.model.Order
import com.gohahotel.connect.domain.model.Booking
import com.gohahotel.connect.domain.model.OrderStatus
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StaffRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    // Observe all active food orders
    fun observeActiveOrders(): Flow<List<Order>> = callbackFlow {
        val subscription = firestore.collection("orders")
            .whereIn("status", listOf("RECEIVED", "PREPARING", "ON_THE_WAY"))
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                val orders = snapshot?.documents?.mapNotNull { it.toObject(Order::class.java) } ?: emptyList()
                trySend(orders)
            }
        awaitClose { subscription.remove() }
    }

    // Observe all today's bookings
    fun observeTodayBookings(): Flow<List<Booking>> = callbackFlow {
        val subscription = firestore.collection("bookings")
            .orderBy("checkInDate", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                val bookings = snapshot?.documents?.mapNotNull { it.toObject(Booking::class.java) } ?: emptyList()
                trySend(bookings)
            }
        awaitClose { subscription.remove() }
    }

    // Update order status (e.g., Prepared -> On the Way)
    suspend fun updateOrderStatus(orderId: String, newStatus: OrderStatus) {
        firestore.collection("orders").document(orderId)
            .update("status", newStatus.name).await()
    }

    // Update booking status
    suspend fun updateBookingStatus(bookingId: String, status: String) {
        firestore.collection("bookings").document(bookingId)
            .update("status", status).await()
    }
}
