package com.gohahotel.connect.data.remote

import com.gohahotel.connect.domain.model.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreService @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    // ─── Collections ─────────────────────────────────────────────────────────
    private val roomsCol    get() = firestore.collection("rooms")
    private val menuCol     get() = firestore.collection("menu")
    private val ordersCol   get() = firestore.collection("orders")
    private val bookingsCol get() = firestore.collection("bookings")
    private val guideCol    get() = firestore.collection("guide")

    // ─── Rooms ────────────────────────────────────────────────────────────────
    suspend fun fetchRooms(): List<HotelRoom> {
        return roomsCol.get().await().documents.mapNotNull { doc ->
            doc.toObject(HotelRoom::class.java)?.copy(id = doc.id)
        }
    }

    suspend fun fetchRoomById(roomId: String): HotelRoom? {
        return roomsCol.document(roomId).get().await()
            .toObject(HotelRoom::class.java)?.copy(id = roomId)
    }

    // ─── Menu ─────────────────────────────────────────────────────────────────
    suspend fun fetchMenuItems(): List<MenuItem> {
        return menuCol.get().await().documents.mapNotNull { doc ->
            doc.toObject(MenuItem::class.java)?.copy(id = doc.id)
        }
    }

    suspend fun fetchMenuByCategory(category: String): List<MenuItem> {
        return menuCol.whereEqualTo("category", category)
            .whereEqualTo("isAvailable", true)
            .get().await()
            .documents.mapNotNull { doc ->
                doc.toObject(MenuItem::class.java)?.copy(id = doc.id)
            }
    }

    // ─── Orders ───────────────────────────────────────────────────────────────
    suspend fun placeOrder(order: Order): String {
        val ref = ordersCol.add(order).await()
        return ref.id
    }

    fun observeOrder(orderId: String): Flow<Order?> = callbackFlow {
        val listener = ordersCol.document(orderId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val order = snapshot?.toObject(Order::class.java)?.copy(id = orderId)
                trySend(order)
            }
        awaitClose { listener.remove() }
    }

    suspend fun getOrdersByGuest(guestId: String): List<Order> {
        return ordersCol.whereEqualTo("guestId", guestId)
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(20)
            .get().await()
            .documents.mapNotNull { doc ->
                doc.toObject(Order::class.java)?.copy(id = doc.id)
            }
    }

    suspend fun submitInRoomRequest(request: Map<String, Any>): String {
        val ref = firestore.collection("room_requests").add(request).await()
        return ref.id
    }

    // ─── Bookings ─────────────────────────────────────────────────────────────
    suspend fun createBooking(booking: Booking): String {
        val ref = bookingsCol.add(booking).await()
        return ref.id
    }

    suspend fun getBookingsByGuest(guestId: String): List<Booking> {
        return bookingsCol.whereEqualTo("guestId", guestId)
            .get().await()
            .documents.mapNotNull { doc ->
                doc.toObject(Booking::class.java)?.copy(id = doc.id)
            }
    }

    // ─── Cultural Guide ───────────────────────────────────────────────────────
    suspend fun fetchGuideEntries(): List<GuideEntry> {
        return guideCol.get().await().documents.mapNotNull { doc ->
            doc.toObject(GuideEntry::class.java)?.copy(id = doc.id)
        }
    }
}
