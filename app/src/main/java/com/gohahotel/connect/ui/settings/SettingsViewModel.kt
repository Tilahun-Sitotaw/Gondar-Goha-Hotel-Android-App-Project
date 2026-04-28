package com.gohahotel.connect.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.gohahotel.connect.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val selectedLanguage: String = "en",
    val isDarkMode: Boolean      = true,
    val guestName: String        = "",
    val guestEmail: String       = "",
    val appVersion: String       = "1.0.0"
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val dataStore: DataStore<Preferences>
) : ViewModel() {

    companion object {
        val KEY_LANGUAGE  = stringPreferencesKey("language")
        val KEY_DARK_MODE = stringPreferencesKey("dark_mode")
    }

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadPreferences()
        loadUserInfo()
    }

    private fun loadPreferences() {
        viewModelScope.launch {
            dataStore.data.collect { prefs ->
                _uiState.update {
                    it.copy(
                        selectedLanguage = prefs[KEY_LANGUAGE] ?: "en",
                        isDarkMode       = (prefs[KEY_DARK_MODE] ?: "true") == "true"
                    )
                }
            }
        }
    }

    private fun loadUserInfo() {
        val user = authRepository.currentUser
        _uiState.update {
            it.copy(
                guestName  = user?.displayName ?: "Guest",
                guestEmail = user?.email ?: "Anonymous"
            )
        }
    }

    fun setLanguage(lang: String) {
        viewModelScope.launch {
            dataStore.edit { prefs -> prefs[KEY_LANGUAGE] = lang }
            _uiState.update { it.copy(selectedLanguage = lang) }
        }
    }

    fun logout(onLogout: () -> Unit) {
        authRepository.signOut()
        onLogout()
    }
}
