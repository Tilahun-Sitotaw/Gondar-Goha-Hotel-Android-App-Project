package com.gohahotel.connect.ui.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gohahotel.connect.data.remote.FirestoreService
import com.gohahotel.connect.data.repository.AuthRepository
import com.gohahotel.connect.domain.model.Promotion
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class EventUiState(
    val event: Promotion?        = null,
    val isLoading: Boolean       = false,
    val isBooked: Boolean        = false,
    val registrationCount: Int   = 0,
    val error: String?           = null
)

@HiltViewModel
class EventViewModel @Inject constructor(
    private val firestoreService: FirestoreService,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EventUiState())
    val uiState = _uiState.asStateFlow()

    val isGuest: Boolean get() = authRepository.currentUser?.isAnonymous == true

    fun loadEvent(eventId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val doc = FirebaseFirestore.getInstance()
                    .collection("promotions")
                    .document(eventId)
                    .get().await()
                val event = doc.toObject(Promotion::class.java)?.copy(id = doc.id)

                // Count registrations
                val regCount = try {
                    FirebaseFirestore.getInstance()
                        .collection("event_registrations")
                        .whereEqualTo("eventId", eventId)
                        .get().await().size()
                } catch (_: Exception) { 0 }

                // Check if current user already registered
                val uid = authRepository.currentUser?.uid
                val alreadyBooked = if (uid != null && !isGuest) {
                    try {
                        FirebaseFirestore.getInstance()
                            .collection("event_registrations")
                            .whereEqualTo("eventId", eventId)
                            .whereEqualTo("guestId", uid)
                            .get().await()
                            .isEmpty.not()
                    } catch (_: Exception) { false }
                } else false

                _uiState.update {
                    it.copy(
                        event = event,
                        isLoading = false,
                        registrationCount = regCount,
                        isBooked = alreadyBooked
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun registerForEvent(eventId: String) {
        val user = authRepository.currentUser ?: return
        if (user.isAnonymous) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val registration = hashMapOf(
                    "eventId"     to eventId,
                    "guestId"     to user.uid,
                    "guestName"   to (user.displayName ?: "Guest"),
                    "guestEmail"  to (user.email ?: ""),
                    "registeredAt" to com.google.firebase.Timestamp.now()
                )
                FirebaseFirestore.getInstance()
                    .collection("event_registrations")
                    .add(registration).await()

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isBooked = true,
                        registrationCount = it.registrationCount + 1
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Registration failed: ${e.message}") }
            }
        }
    }
}
