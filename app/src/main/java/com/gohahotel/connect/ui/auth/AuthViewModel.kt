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
    val isLoading           : Boolean = false,
    val isSuccess           : Boolean = false,
    val isOtpSent           : Boolean = false,   // OTP sent to email
    val error               : String? = null,
    val message             : String? = null,
    val userRole            : String  = "GUEST",
    val generatedOtp        : String  = "",      // Store OTP for verification
    val userEmail           : String  = ""       // Store email for OTP verification
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository   : AuthRepository,
    private val firestoreService : FirestoreService,
    private val cloudinaryService: CloudinaryService,
    private val emailService     : com.gohahotel.connect.data.remote.EmailService
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

    // ─── Register — Step 1: Generate OTP and send to email ───────────────────
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

            // Generate 6-digit OTP
            val otp = (100000..999999).random().toString()

            // Upload ID document to Cloudinary if provided
            var idDocUrl = ""
            if (idDocumentUri != null) {
                try {
                    idDocUrl = cloudinaryService.uploadFile(idDocumentUri, "identity_documents")
                } catch (_: Exception) {
                    // Non-fatal — continue registration without ID doc URL
                }
            }

            // Try to send OTP email
            val emailResult = try {
                emailService.sendOtpEmail(email, otp, displayName).getOrThrow()
                true
            } catch (e: Exception) {
                // Email failed, but we'll proceed anyway and show OTP in UI
                android.util.Log.e("AuthViewModel", "Email send failed: ${e.message}", e)
                false
            }

            // Store registration data temporarily for verification
            _uiState.update {
                it.copy(
                    isLoading    = false,
                    isOtpSent    = true,
                    generatedOtp = otp,
                    userEmail    = email,
                    message      = if (emailResult) {
                        "A 6-digit verification code has been sent to $email. Please check your inbox."
                    } else {
                        "⚠️ Email service unavailable. Your verification code is: $otp\n\nPlease enter this code to continue."
                    }
                )
            }
            
            // Store other registration data in a temporary map for later use
            tempRegistrationData = mapOf(
                "email" to email,
                "password" to password,
                "displayName" to displayName,
                "phoneNumber" to phoneNumber,
                "address" to address,
                "idDocumentUrl" to idDocUrl,
                "idDocumentType" to idDocumentType
            )
        }
    }

    // Temporary storage for registration data
    private var tempRegistrationData: Map<String, String> = emptyMap()

    // ─── Register — Step 2: Verify OTP and create account ────────────────────
    fun verifyOtpAndRegister(enteredOtp: String) {
        if (enteredOtp.isBlank()) {
            _uiState.update { it.copy(error = "Please enter the verification code") }
            return
        }

        if (enteredOtp != _uiState.value.generatedOtp) {
            _uiState.update { it.copy(error = "Invalid verification code. Please try again.") }
            return
        }

        // OTP is correct, proceed with account creation
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val data = tempRegistrationData
            authRepository.registerWithEmail(
                email          = data["email"] ?: "",
                password       = data["password"] ?: "",
                displayName    = data["displayName"] ?: "",
                phoneNumber    = data["phoneNumber"] ?: "",
                address        = data["address"] ?: "",
                idDocumentUrl  = data["idDocumentUrl"] ?: "",
                idDocumentType = data["idDocumentType"] ?: ""
            )
                .onSuccess { user ->
                    val role = firestoreService.getUserRole(user.uid)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isSuccess = true,
                            userRole  = role,
                            message   = "Account created successfully! Welcome to Goha Hotel."
                        )
                    }
                    tempRegistrationData = emptyMap() // Clear temp data
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

    // ─── Resend OTP email ─────────────────────────────────────────────────────
    fun resendOtp() {
        val email = _uiState.value.userEmail
        val displayName = tempRegistrationData["displayName"] ?: "Guest"
        
        if (email.isBlank()) {
            _uiState.update { it.copy(error = "Email not found. Please start registration again.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            // Generate new OTP
            val otp = (100000..999999).random().toString()
            
            emailService.sendOtpEmail(email, otp, displayName)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            generatedOtp = otp,
                            message = "New verification code sent to $email"
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = "Failed to resend code: ${e.message}") }
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
