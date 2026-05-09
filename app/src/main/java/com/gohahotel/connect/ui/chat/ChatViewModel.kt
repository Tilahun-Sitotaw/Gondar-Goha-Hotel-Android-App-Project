package com.gohahotel.connect.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.gohahotel.connect.data.remote.FirestoreService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatMessage(
    val id: String = "",
    val text: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val isAdmin: Boolean = false,
    val timestamp: Long = 0L,
    val isRead: Boolean = false
)

data class ChatThread(
    val guestId: String = "",
    val guestName: String = "",
    val lastMessage: String = "",
    val lastTimestamp: Long = 0L,
    val unreadCount: Int = 0
)

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val threads: List<ChatThread> = emptyList(),
    val isLoading: Boolean = false,
    val isSending: Boolean = false,
    val currentGuestId: String = "",
    val currentGuestName: String = ""
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val firestoreService: FirestoreService
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private val auth = FirebaseAuth.getInstance()

    val currentUid: String get() = auth.currentUser?.uid ?: ""
    val currentName: String get() = auth.currentUser?.displayName
        ?: auth.currentUser?.email?.substringBefore("@") ?: "Guest"
    val isAdmin: Boolean get() = auth.currentUser?.email?.lowercase() == "gohahotel34@gmail.com"

    /** Guest opens their own chat thread */
    fun openGuestChat() {
        val uid = currentUid.ifBlank { return }
        _uiState.update { it.copy(currentGuestId = uid, currentGuestName = currentName) }
        observeMessages(uid)
    }

    /** Admin opens a specific guest's chat */
    fun openAdminChat(guestId: String, guestName: String) {
        _uiState.update { it.copy(currentGuestId = guestId, currentGuestName = guestName) }
        observeMessages(guestId)
    }

    /** Admin loads all chat threads */
    fun loadAllThreads() {
        firestoreService.observeAllChatThreads()
            .onEach { rawThreads ->
                val threads = rawThreads.map { raw ->
                    ChatThread(
                        guestId       = raw["guestId"] as? String ?: "",
                        guestName     = raw["guestName"] as? String ?: "Guest",
                        lastMessage   = raw["lastMessage"] as? String ?: "",
                        lastTimestamp = raw["lastTimestamp"] as? Long ?: 0L,
                        unreadCount   = (raw["unreadCount"] as? Long)?.toInt() ?: 0
                    )
                }.sortedByDescending { it.lastTimestamp }
                _uiState.update { it.copy(threads = threads) }
            }
            .launchIn(viewModelScope)
    }

    private fun observeMessages(guestId: String) {
        firestoreService.observeChatMessages(guestId)
            .onEach { rawMessages ->
                val messages = rawMessages.map { raw ->
                    ChatMessage(
                        id         = raw["id"] as? String ?: "",
                        text       = raw["text"] as? String ?: "",
                        senderId   = raw["senderId"] as? String ?: "",
                        senderName = raw["senderName"] as? String ?: "",
                        isAdmin    = raw["isAdmin"] as? Boolean ?: false,
                        timestamp  = raw["timestamp"] as? Long ?: 0L,
                        isRead     = raw["isRead"] as? Boolean ?: false
                    )
                }
                _uiState.update { it.copy(messages = messages) }
            }
            .launchIn(viewModelScope)
    }

    fun sendMessage(text: String) {
        val guestId = _uiState.value.currentGuestId.ifBlank { return }
        val trimmed = text.trim().ifBlank { return }
        val uid = currentUid
        val name = currentName
        val admin = isAdmin
        val now = System.currentTimeMillis()

        viewModelScope.launch {
            _uiState.update { it.copy(isSending = true) }
            try {
                val msg = mapOf(
                    "text"       to trimmed,
                    "senderId"   to uid,
                    "senderName" to name,
                    "isAdmin"    to admin,
                    "timestamp"  to now,
                    "isRead"     to false
                )
                firestoreService.sendChatMessage(guestId, msg)

                // Update thread metadata
                val threadData = mapOf(
                    "guestId"       to guestId,
                    "guestName"     to _uiState.value.currentGuestName,
                    "lastMessage"   to trimmed,
                    "lastTimestamp" to now,
                    "unreadCount"   to if (admin) 0 else
                        (_uiState.value.messages.count { !it.isRead && !it.isAdmin } + 1)
                )
                firestoreService.updateChatThread(guestId, threadData)
            } catch (_: Exception) {
            } finally {
                _uiState.update { it.copy(isSending = false) }
            }
        }
    }
}
