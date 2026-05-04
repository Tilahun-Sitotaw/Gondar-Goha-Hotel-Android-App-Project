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
import com.gohahotel.connect.data.remote.FirestoreService

data class AuthUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String?     = null,
    val message: String?   = null,
    val userRole: String   = "GUEST",
    val isOtpSent: Boolean = false,
    val isResetOtpSent: Boolean = false,
    val isOtpVerified: Boolean = false,
    val resetEmail: String = ""
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val firestoreService: FirestoreService
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
                .onSuccess { user ->
                    val role = firestoreService.getUserRole(user.uid)
                    _uiState.update { it.copy(isLoading = false, isSuccess = true, userRole = role) } 
                }
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
        }
    }

    fun startRegistration(email: String, displayName: String) {
        if (email.isBlank()) {
            _uiState.update { it.copy(error = "Email is required") }
            return
        }
        if (displayName.isBlank()) {
            _uiState.update { it.copy(error = "Full Name is required") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            authRepository.sendRegistrationOtp(email, displayName)
                .onSuccess { _uiState.update { it.copy(isLoading = false, isOtpSent = true, message = "OTP sent to $email") } }
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
        }
    }

    fun verifyRegistrationOtp(email: String, otp: String) {
        viewModelScope.launch {
            val isValid = authRepository.verifyRegistrationOtp(email, otp)
            if (isValid) {
                _uiState.update { it.copy(isOtpVerified = true, message = "Email verified successfully") }
            } else {
                _uiState.update { it.copy(error = "Invalid OTP") }
            }
        }
    }

    fun register(
        email: String,
        password: String,
        confirmPassword: String,
        displayName: String,
        phoneNumber: String,
        address: String,
        otp: String
    ) {
        if (email.isBlank() || password.isBlank() || displayName.isBlank() || phoneNumber.isBlank() || address.isBlank()) {
            _uiState.update { it.copy(error = "Please fill in all fields") }
            return
        }
        if (password != confirmPassword) {
            _uiState.update { it.copy(error = "Passwords do not match") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            if (authRepository.verifyRegistrationOtp(email, otp)) {
                authRepository.registerWithEmail(email, password, displayName, phoneNumber, address)
                    .onSuccess { _uiState.update { it.copy(isLoading = false, isSuccess = true) } }
                    .onFailure { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
            } else {
                _uiState.update { it.copy(isLoading = false, error = "Invalid OTP") }
            }
        }
    }

    fun requestPasswordResetOtp(email: String) {
        if (email.isBlank()) {
            _uiState.update { it.copy(error = "Please enter your email") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            authRepository.sendPasswordResetOtp(email)
                .onSuccess { 
                    _uiState.update { it.copy(isLoading = false, isResetOtpSent = true, resetEmail = email, message = "Reset code sent to $email") }
                }
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
        }
    }

    fun resetPasswordWithOtp(email: String, otp: String, newPassword: String, confirmPassword: String) {
        if (newPassword != confirmPassword) {
            _uiState.update { it.copy(error = "Passwords do not match") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            authRepository.verifyResetOtp(email, otp)
                .onSuccess { isValid ->
                    if (isValid) {
                        authRepository.updatePassword(email, newPassword)
                            .onSuccess { _uiState.update { it.copy(isLoading = false, message = "Password updated successfully. Please check your email for the final confirmation link.", isResetOtpSent = false) } }
                            .onFailure { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
                    } else {
                        _uiState.update { it.copy(isLoading = false, error = "Invalid or expired OTP") }
                    }
                }
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

    fun signInWithGoogle(credential: com.google.firebase.auth.AuthCredential) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            authRepository.signInWithGoogleCredential(credential)
                .onSuccess { _uiState.update { it.copy(isLoading = false, isSuccess = true) } }
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
        }
    }
}
