package com.gohahotel.connect.domain.model

data class GuestUser(
    val uid: String = "",
    val displayName: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val roomNumber: String = "",
    val checkInDate: String = "",
    val checkOutDate: String = "",
    val preferredLanguage: String = "en",
    val profileImageUrl: String = "",
    val isGuest: Boolean = false         // true = anonymous guest sign-in
)
