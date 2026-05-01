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
                "role" to if (email.lowercase().trim() == "tilaunsitotaw87@gmail.com") "ADMIN" else "GUEST",
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
                    "role" to if (email.lowercase().trim() == "tilaunsitotaw87@gmail.com") "ADMIN" else "GUEST",
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
