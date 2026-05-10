package com.gohahotel.connect.ui.auth

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gohahotel.connect.data.remote.CloudinaryService
import com.gohahotel.connect.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.gohahotel.connect.data.remote.FirestoreService

data class AuthUiState(
    val isLoading        : Boolean = false,
    val isSuccess        : Boolean = false,
    val isVerificationSent: Boolean = false,   // account created, email sent
    val error            : String? = null,
    val message          : String? = null,
    val userRole         : String  = "GUEST"
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository   : AuthRepository,
    private val firestoreService : FirestoreService,
    private val cloudinaryService: CloudinaryService
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
                        e.message?.contains("no user") == true    -> "No account found with this email"
                        e.message?.contains("network") == true    -> "Network error. Check your connection"
                        else -> e.message ?: "Sign in failed"
                    }
                    _uiState.update { it.copy(isLoading = false, error = msg) }
                }
        }
    }

    // ─── Register — 2-step: create account + send verification email ──────────
    fun register(
        email          : String,
        password       : String,
        confirmPassword: String,
        displayName    : String,
        phoneNumber    : String,
        address        : String,
        idDocumentUri  : Uri? = null,
        idDocumentType : String = ""
    ) {
        when {
            displayName.isBlank()       -> { _uiState.update { it.copy(error = "Full name is required") }; return }
            email.isBlank()             -> { _uiState.update { it.copy(error = "Email is required") }; return }
            !email.contains("@")        -> { _uiState.update { it.copy(error = "Enter a valid email address") }; return }
            phoneNumber.isBlank()       -> { _uiState.update { it.copy(error = "Phone number is required") }; return }
            address.isBlank()           -> { _uiState.update { it.copy(error = "Physical address is required") }; return }
            password.length < 6         -> { _uiState.update { it.copy(error = "Password must be at least 6 characters") }; return }
            password != confirmPassword -> { _uiState.update { it.copy(error = "Passwords do not match") }; return }
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            // Upload ID document to Cloudinary if provided
            var idDocUrl = ""
            if (idDocumentUri != null) {
                try {
                    idDocUrl = cloudinaryService.uploadFile(idDocumentUri, "identity_documents")
                } catch (_: Exception) {
                    // Non-fatal — continue registration without ID doc URL
                }
            }

            authRepository.registerWithEmail(
                email          = email,
                password       = password,
                displayName    = displayName,
                phoneNumber    = phoneNumber,
                address        = address,
                idDocumentUrl  = idDocUrl,
                idDocumentType = idDocumentType
            )
                .onSuccess { _ ->
                    _uiState.update {
                        it.copy(
                            isLoading          = false,
                            isVerificationSent = true,
                            message            = "Account created! A verification link has been sent to $email. Please check your inbox and verify before continuing."
                        )
                    }
                }
                .onFailure { e ->
                    val msg = when {
                        e.message?.contains("email address is already") == true ->
                            "This email is already registered. Try signing in."
                        e.message?.contains("badly formatted") == true ->
                            "Invalid email address format"
                        e.message?.contains("weak-password") == true ->
                            "Password is too weak. Use at least 6 characters"
                        e.message?.contains("network") == true ->
                            "Network error. Check your connection"
                        else -> e.message ?: "Registration failed"
                    }
                    _uiState.update { it.copy(isLoading = false, error = msg) }
                }
        }
    }

    // ─── Resend verification email ────────────────────────────────────────────
    fun resendVerificationEmail() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            authRepository.resendVerificationEmail()
                .onSuccess {
                    _uiState.update {
                        it.copy(isLoading = false,
                            message = "Verification email resent. Check your inbox.")
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }

    // ─── Check if email is verified then proceed ──────────────────────────────
    fun checkVerificationAndProceed() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val verified = authRepository.checkEmailVerified()
            if (verified) {
                val user = authRepository.currentUser
                val role = if (user != null) firestoreService.getUserRole(user.uid) else "GUEST"
                _uiState.update {
                    it.copy(isLoading = false, isSuccess = true, userRole = role)
                }
            } else {
                _uiState.update {
                    it.copy(isLoading = false,
                        error = "Email not verified yet. Please check your inbox and click the verification link, then tap 'I've Verified' again.")
                }
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
