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
    val userRole: String   = "GUEST"
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val firestoreService: FirestoreService
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState = _uiState.asStateFlow()

    // ─── Sign In ──────────────────────────────────────────────────────────────
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
                .onFailure { e ->
                    val msg = when {
                        e.message?.contains("password") == true ||
                        e.message?.contains("credential") == true -> "Incorrect email or password"
                        e.message?.contains("no user") == true -> "No account found with this email"
                        e.message?.contains("network") == true -> "Network error. Check your connection"
                        else -> e.message ?: "Sign in failed"
                    }
                    _uiState.update { it.copy(isLoading = false, error = msg) }
                }
        }
    }

    // ─── Register — direct, no OTP ───────────────────────────────────────────
    fun register(
        email: String,
        password: String,
        confirmPassword: String,
        displayName: String,
        phoneNumber: String,
        address: String
    ) {
        when {
            displayName.isBlank() -> { _uiState.update { it.copy(error = "Full name is required") }; return }
            email.isBlank()       -> { _uiState.update { it.copy(error = "Email is required") }; return }
            !email.contains("@") -> { _uiState.update { it.copy(error = "Enter a valid email address") }; return }
            phoneNumber.isBlank() -> { _uiState.update { it.copy(error = "Phone number is required") }; return }
            address.isBlank()     -> { _uiState.update { it.copy(error = "Physical address is required") }; return }
            password.length < 6   -> { _uiState.update { it.copy(error = "Password must be at least 6 characters") }; return }
            password != confirmPassword -> { _uiState.update { it.copy(error = "Passwords do not match") }; return }
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            authRepository.registerWithEmail(email, password, displayName, phoneNumber, address)
                .onSuccess { user ->
                    val role = firestoreService.getUserRole(user.uid)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isSuccess = true,
                            userRole  = role,
                            message   = "Account created! A verification email has been sent to $email"
                        )
                    }
                }
                .onFailure { e ->
                    val msg = when {
                        e.message?.contains("email address is already") == true -> "This email is already registered. Try signing in."
                        e.message?.contains("badly formatted") == true -> "Invalid email address format"
                        e.message?.contains("weak-password") == true -> "Password is too weak. Use at least 6 characters"
                        e.message?.contains("network") == true -> "Network error. Check your connection"
                        else -> e.message ?: "Registration failed"
                    }
                    _uiState.update { it.copy(isLoading = false, error = msg) }
                }
        }
    }

    // ─── Guest ────────────────────────────────────────────────────────────────
    fun signInAsGuest() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            authRepository.signInAsGuest()
                .onSuccess { _uiState.update { it.copy(isLoading = false, isSuccess = true, userRole = "GUEST") } }
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
        }
    }

    // ─── Google ───────────────────────────────────────────────────────────────
    fun signInWithGoogle(credential: com.google.firebase.auth.AuthCredential) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            authRepository.signInWithGoogleCredential(credential)
                .onSuccess { user ->
                    val role = firestoreService.getUserRole(user.uid)
                    _uiState.update { it.copy(isLoading = false, isSuccess = true, userRole = role) }
                }
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
        }
    }

    // ─── Password Reset ───────────────────────────────────────────────────────
    fun resetPassword(email: String) {
        if (email.isBlank()) {
            _uiState.update { it.copy(error = "Please enter your email") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, message = null) }
            authRepository.sendPasswordResetEmail(email)
                .onSuccess { _uiState.update { it.copy(isLoading = false, message = "Reset link sent to $email", error = null) } }
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, error = e.message, message = null) } }
        }
    }

    /** Reset all UI state — call when LoginScreen is shown after logout */
    fun resetState() {
        _uiState.value = AuthUiState()
    }
}
