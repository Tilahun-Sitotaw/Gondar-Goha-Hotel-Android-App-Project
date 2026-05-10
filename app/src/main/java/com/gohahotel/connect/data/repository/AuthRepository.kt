package com.gohahotel.connect.data.repository

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

@Singleton
class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    val currentUser: FirebaseUser? get() = auth.currentUser

    val authStateFlow: Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { trySend(it.currentUser) }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    // ─── Sign In ──────────────────────────────────────────────────────────────
    suspend fun signInWithEmail(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            Result.success(result.user!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ─── Register ─────────────────────────────────────────────────────────────
    // Step 1: Create account + send Firebase verification email
    suspend fun registerWithEmail(
        email: String,
        password: String,
        displayName: String,
        phoneNumber: String,
        address: String,
        idDocumentUrl: String = "",
        idDocumentType: String = ""
    ): Result<FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user!!

            // Update Firebase Auth display name
            val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                .setDisplayName(displayName)
                .build()
            user.updateProfile(profileUpdates).await()

            // Send Firebase's built-in email verification link
            try { user.sendEmailVerification().await() } catch (_: Exception) {}

            // Save full profile to Firestore
            val role = if (email.lowercase().trim() == "gohahotel34@gmail.com") "ADMIN" else "GUEST"
            val userProfile = hashMapOf(
                "uid"             to user.uid,
                "displayName"     to displayName,
                "email"           to email,
                "phoneNumber"     to phoneNumber,
                "address"         to address,
                "role"            to role,
                "idDocumentUrl"   to idDocumentUrl,
                "idDocumentType"  to idDocumentType,
                "emailVerified"   to false,
                "createdAt"       to com.google.firebase.Timestamp.now()
            )
            firestore.collection("users").document(user.uid).set(userProfile).await()

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Step 2: Resend verification email
    suspend fun resendVerificationEmail(): Result<Unit> {
        return try {
            auth.currentUser?.sendEmailVerification()?.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Step 3: Check if email is now verified (reload user first)
    suspend fun checkEmailVerified(): Boolean {
        return try {
            auth.currentUser?.reload()?.await()
            auth.currentUser?.isEmailVerified == true
        } catch (_: Exception) { false }
    }

    // ─── Guest ────────────────────────────────────────────────────────────────
    suspend fun signInAsGuest(): Result<FirebaseUser> {
        return try {
            val result = auth.signInAnonymously().await()
            Result.success(result.user!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ─── Google ───────────────────────────────────────────────────────────────
    suspend fun signInWithGoogleCredential(credential: AuthCredential): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithCredential(credential).await()
            val user = result.user!!
            val email = user.email ?: ""

            val userDoc = firestore.collection("users").document(user.uid).get().await()
            if (!userDoc.exists()) {
                val role = if (email.lowercase().trim() == "gohahotel34@gmail.com") "ADMIN" else "GUEST"
                val userProfile = hashMapOf(
                    "uid"         to user.uid,
                    "displayName" to (user.displayName ?: "Google User"),
                    "email"       to email,
                    "phoneNumber" to (user.phoneNumber ?: ""),
                    "address"     to "",
                    "role"        to role,
                    "createdAt"   to com.google.firebase.Timestamp.now()
                )
                firestore.collection("users").document(user.uid).set(userProfile).await()
            }
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ─── Password Reset ───────────────────────────────────────────────────────
    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ─── Delete user from Firestore (admin action) ────────────────────────────
    suspend fun deleteUserFromFirestore(uid: String): Result<Unit> {
        return try {
            firestore.collection("users").document(uid).delete().await()
            Result.success(Unit)
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
