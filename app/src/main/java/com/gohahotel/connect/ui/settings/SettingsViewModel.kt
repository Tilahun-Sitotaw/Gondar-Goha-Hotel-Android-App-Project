package com.gohahotel.connect.ui.settings

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import com.gohahotel.connect.core.utils.LocaleHelper
import com.gohahotel.connect.data.remote.CloudinaryService
import com.gohahotel.connect.data.remote.FirestoreService
import com.gohahotel.connect.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class SettingsUiState(
    val selectedLanguage : String  = "en",
    val isDarkMode       : Boolean = true,
    val notificationsOn  : Boolean = true,
    val guestName        : String  = "",
    val guestEmail       : String  = "",
    val phoneNumber      : String  = "",
    val address          : String  = "",
    val profilePhotoUrl  : String  = "",
    val userRole         : String  = "GUEST",
    val appVersion       : String  = "1.0.4",
    val isLoading        : Boolean = false,
    val isUploading      : Boolean = false,
    val successMessage   : String? = null,
    val errorMessage     : String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository   : AuthRepository,
    private val firestoreService : FirestoreService,
    private val cloudinaryService: CloudinaryService,
    private val dataStore        : DataStore<Preferences>,
    @ApplicationContext private val context: Context
) : ViewModel() {

    companion object {
        val KEY_LANGUAGE      = stringPreferencesKey("language")
        val KEY_DARK_MODE     = stringPreferencesKey("dark_mode")
        val KEY_NOTIFICATIONS = booleanPreferencesKey("notifications")
    }

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadPreferences()
        loadUserInfo()
    }

    // ─── Load ─────────────────────────────────────────────────────────────────
    private fun loadPreferences() {
        viewModelScope.launch {
            dataStore.data.collect { prefs ->
                _uiState.update {
                    it.copy(
                        selectedLanguage = prefs[KEY_LANGUAGE] ?: "en",
                        isDarkMode       = (prefs[KEY_DARK_MODE] ?: "true") == "true",
                        notificationsOn  = prefs[KEY_NOTIFICATIONS] ?: true
                    )
                }
            }
        }
    }

    private fun loadUserInfo() {
        val user = authRepository.currentUser ?: return
        viewModelScope.launch {
            val role = firestoreService.getUserRole(user.uid)
            // Also load extended profile from Firestore
            try {
                val doc = FirebaseFirestore.getInstance()
                    .collection("users").document(user.uid).get().await()
                _uiState.update {
                    it.copy(
                        guestName       = user.displayName ?: doc.getString("displayName") ?: "Guest",
                        guestEmail      = user.email ?: "",
                        phoneNumber     = doc.getString("phoneNumber") ?: "",
                        address         = doc.getString("address") ?: "",
                        profilePhotoUrl = user.photoUrl?.toString()
                            ?: doc.getString("profilePhotoUrl") ?: "",
                        userRole        = role
                    )
                }
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        guestName  = user.displayName ?: "Guest",
                        guestEmail = user.email ?: "",
                        userRole   = role
                    )
                }
            }
        }
    }

    // ─── Profile photo upload ─────────────────────────────────────────────────
    fun uploadProfilePhoto(uri: Uri) {
        val user = authRepository.currentUser ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isUploading = true, errorMessage = null) }
            try {
                val url = cloudinaryService.uploadFile(uri, "profile_photos")
                // Update Firebase Auth photo
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setPhotoUri(android.net.Uri.parse(url))
                    .build()
                FirebaseAuth.getInstance().currentUser?.updateProfile(profileUpdates)?.await()
                // Update Firestore
                FirebaseFirestore.getInstance()
                    .collection("users").document(user.uid)
                    .update("profilePhotoUrl", url).await()
                _uiState.update {
                    it.copy(isUploading = false, profilePhotoUrl = url,
                        successMessage = "Profile photo updated!")
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isUploading = false,
                        errorMessage = "Photo upload failed: ${e.message}")
                }
            }
        }
    }

    // ─── Update display name ──────────────────────────────────────────────────
    fun updateDisplayName(newName: String) {
        val user = authRepository.currentUser ?: return
        if (newName.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Name cannot be empty") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(newName.trim())
                    .build()
                FirebaseAuth.getInstance().currentUser?.updateProfile(profileUpdates)?.await()
                FirebaseFirestore.getInstance()
                    .collection("users").document(user.uid)
                    .update("displayName", newName.trim()).await()
                _uiState.update {
                    it.copy(isLoading = false, guestName = newName.trim(),
                        successMessage = "Name updated successfully!")
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    // ─── Update phone ─────────────────────────────────────────────────────────
    fun updatePhone(phone: String) {
        val user = authRepository.currentUser ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                FirebaseFirestore.getInstance()
                    .collection("users").document(user.uid)
                    .update("phoneNumber", phone.trim()).await()
                _uiState.update {
                    it.copy(isLoading = false, phoneNumber = phone.trim(),
                        successMessage = "Phone number updated!")
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    // ─── Update address ───────────────────────────────────────────────────────
    fun updateAddress(addr: String) {
        val user = authRepository.currentUser ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                FirebaseFirestore.getInstance()
                    .collection("users").document(user.uid)
                    .update("address", addr.trim()).await()
                _uiState.update {
                    it.copy(isLoading = false, address = addr.trim(),
                        successMessage = "Address updated!")
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    // ─── Language ─────────────────────────────────────────────────────────────
    fun setLanguage(lang: String) {
        viewModelScope.launch {
            dataStore.edit { prefs -> prefs[KEY_LANGUAGE] = lang }
            LocaleHelper.applyLanguage(context, lang)
            _uiState.update { it.copy(selectedLanguage = lang) }
        }
    }

    // ─── Notifications toggle ─────────────────────────────────────────────────
    fun toggleNotifications(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.edit { prefs -> prefs[KEY_NOTIFICATIONS] = enabled }
            _uiState.update { it.copy(notificationsOn = enabled) }
        }
    }

    // ─── Clear messages ───────────────────────────────────────────────────────
    fun clearMessages() {
        _uiState.update { it.copy(successMessage = null, errorMessage = null) }
    }

    // ─── Logout ───────────────────────────────────────────────────────────────
    fun logout(onLogout: () -> Unit) {
        authRepository.signOut()
        onLogout()
    }
}
