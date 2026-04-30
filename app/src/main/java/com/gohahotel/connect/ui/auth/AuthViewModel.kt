package com.gohahotel.connect.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gohahotel.connect.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String?     = null,
    val message: String?   = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState = _uiState.asStateFlow()

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.update { it.copy(error = "Please fill in all fields") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            authRepository.signInWithEmail(email, password)
                .onSuccess { _uiState.update { it.copy(isLoading = false, isSuccess = true) } }
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
        }
    }

    fun register(
        email: String,
        password: String,
        confirmPassword: String,
        displayName: String,
        phoneNumber: String,
        address: String
    ) {
        if (email.isBlank() || password.isBlank() || displayName.isBlank() || phoneNumber.isBlank() || address.isBlank()) {
            _uiState.update { it.copy(error = "Please fill in all fields") }
            return
        }
        if (password != confirmPassword) {
            _uiState.update { it.copy(error = "Passwords do not match") }
            return
        }
        if (password.length < 6) {
            _uiState.update { it.copy(error = "Password must be at least 6 characters") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            authRepository.registerWithEmail(email, password, displayName)
                .onSuccess { _uiState.update { it.copy(isLoading = false, isSuccess = true) } }
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
        }
    }

    fun signInAsGuest() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            authRepository.signInAsGuest()
                .onSuccess { _uiState.update { it.copy(isLoading = false, isSuccess = true) } }
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
        }
    }

    fun resetPassword(email: String) {
        if (email.isBlank()) {
            _uiState.update { it.copy(error = "Please enter your email to reset password", message = null) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, message = null) }
            authRepository.sendPasswordResetEmail(email)
                .onSuccess { 
                    _uiState.update { it.copy(isLoading = false, message = "Reset link sent to $email", error = null) }
                }
                .onFailure { e -> 
                    _uiState.update { it.copy(isLoading = false, error = e.message, message = null) }
                }
        }
    }

    fun signInWithGoogle() {
        // This typically triggers the Google Identity / One Tap UI
        _uiState.update { it.copy(error = "Google Sign-In initialized...") }
    }
}
