package com.gohahotel.connect.data.repository

import com.gohahotel.connect.data.remote.EmailService
import com.gohahotel.connect.domain.model.GuestUser
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val emailService: EmailService
) {
    private var pendingOtp: String? = null
    private var pendingUserEmail: String? = null

    val currentUser: FirebaseUser? get() = auth.currentUser

    // ... (rest of existing code)

    fun generateOtp(): String {
        return (100000..999999).random().toString()
    }

    suspend fun sendRegistrationOtp(email: String, displayName: String): Result<String> {
        val otp = generateOtp()
        return emailService.sendOtpEmail(email, otp, displayName).map { 
            pendingOtp = otp
            pendingUserEmail = email
            otp 
        }
    }

    suspend fun verifyRegistrationOtp(email: String, otp: String): Boolean {
        return email == pendingUserEmail && otp == pendingOtp
    }

    suspend fun sendPasswordResetOtp(email: String): Result<String> {
        val otp = generateOtp()
        // Store OTP in Firestore with timestamp for security
        return try {
            firestore.collection("password_resets").document(email).set(
                mapOf(
                    "otp" to otp,
                    "timestamp" to com.google.firebase.Timestamp.now()
                )
            ).await()
            emailService.sendPasswordResetOtp(email, otp).map { otp }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun verifyResetOtp(email: String, otp: String): Result<Boolean> {
        return try {
            val doc = firestore.collection("password_resets").document(email).get().await()
            if (doc.exists()) {
                val storedOtp = doc.getString("otp")
                val timestamp = doc.getTimestamp("timestamp")
                // Check if OTP matches and is not older than 10 minutes
                val isValid = storedOtp == otp && 
                    (com.google.firebase.Timestamp.now().seconds - (timestamp?.seconds ?: 0)) < 600
                Result.success(isValid)
            } else {
                Result.success(false)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updatePassword(email: String, newPassword: String): Result<Unit> {
        return try {
            // Note: Firebase Auth doesn't allow direct password update for another user by email without re-authentication.
            // However, since we verified OTP, we can use the sendPasswordResetEmail as a fallback or if we have a custom backend.
            // For a pure Firebase approach without a custom server, we might still need to use the link-based reset
            // OR we can sign in the user temporarily if we have their credentials (but we don't for reset).
            // A common workaround is to use a Firebase Cloud Function.
            // For now, we'll implement the "Reset via Email Link" as the primary secure method but structure the UI for OTP.
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    val authStateFlow: Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    suspend fun signInWithEmail(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            Result.success(result.user!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun registerWithEmail(
        email: String,
        password: String,
        displayName: String,
        phoneNumber: String,
        address: String
    ): Result<FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user!!
            
            // 1. Update Firebase Auth Profile
            val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                .setDisplayName(displayName)
                .build()
            user.updateProfile(profileUpdates).await()

            // 2. Save Extended Profile to Firestore
            val userProfile = hashMapOf(
                "uid" to user.uid,
                "displayName" to displayName,
                "email" to email,
                "phoneNumber" to phoneNumber,
                "address" to address,
                "role" to if (email.lowercase().trim() == "gohahotel34@gmail.com") "ADMIN" else "GUEST",
                "createdAt" to com.google.firebase.Timestamp.now()
            )
            firestore.collection("users").document(user.uid).set(userProfile).await()

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signInAsGuest(): Result<FirebaseUser> {
        return try {
            val result = auth.signInAnonymously().await()
            Result.success(result.user!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signInWithGoogleCredential(credential: AuthCredential): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithCredential(credential).await()
            val user = result.user!!
            
            // Check if user exists in Firestore, if not create
            val userDoc = firestore.collection("users").document(user.uid).get().await()
            val email = user.email ?: ""
            if (!userDoc.exists()) {
                val userProfile = hashMapOf(
                    "uid" to user.uid,
                    "displayName" to (user.displayName ?: "Google User"),
                    "email" to email,
                    "phoneNumber" to (user.phoneNumber ?: ""),
                    "address" to "",
                "role" to if (email.lowercase().trim() == "gohahotel34@gmail.com") "ADMIN" else "GUEST",
                    "createdAt" to com.google.firebase.Timestamp.now()
                )
                firestore.collection("users").document(user.uid).set(userProfile).await()
            }
            
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun signOut() = auth.signOut()

    fun isSignedIn() = auth.currentUser != null

    fun toGuestUser(firebaseUser: FirebaseUser) = GuestUser(
        uid         = firebaseUser.uid,
        displayName = firebaseUser.displayName ?: "Guest",
        email       = firebaseUser.email ?: "",
        isGuest     = firebaseUser.isAnonymous
    )
}
