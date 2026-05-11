package com.gohahotel.connect.core.localization

import android.content.Context
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "language_preferences")

class LocalizationManager(private val context: Context) {
    
    companion object {
        private val LANGUAGE_KEY = stringPreferencesKey("selected_language")
        const val ENGLISH = "en"
        const val AMHARIC = "am"
    }

    val selectedLanguage: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[LANGUAGE_KEY] ?: ENGLISH
    }

    suspend fun setLanguage(language: String) {
        context.dataStore.edit { preferences ->
            preferences[LANGUAGE_KEY] = language
        }
    }
}

// String resources for localization
object LocalizedStrings {
    
    fun getWelcome(language: String): String = when (language) {
        "am" -> "ወደ ጎሃ ሆቴል እንኳን ደህና መጡ"
        else -> "Welcome to Goha Hotel"
    }
    
    fun getSignIn(language: String): String = when (language) {
        "am" -> "ግባ"
        else -> "Sign In"
    }
    
    fun getRegister(language: String): String = when (language) {
        "am" -> "ምዝገባ"
        else -> "Register"
    }
    
    fun getEmail(language: String): String = when (language) {
        "am" -> "ኢሜይል"
        else -> "Email"
    }
    
    fun getPassword(language: String): String = when (language) {
        "am" -> "ይለፍ ቃል"
        else -> "Password"
    }
    
    fun getFullName(language: String): String = when (language) {
        "am" -> "ሙሉ ስም"
        else -> "Full Name"
    }
    
    fun getPhoneNumber(language: String): String = when (language) {
        "am" -> "ስልክ ቁጥር"
        else -> "Phone Number"
    }
    
    fun getAddress(language: String): String = when (language) {
        "am" -> "አድራሻ"
        else -> "Address"
    }
    
    fun getCreateAccount(language: String): String = when (language) {
        "am" -> "ሂሳብ ፍጠር"
        else -> "Create Account"
    }
    
    fun getVerificationCode(language: String): String = when (language) {
        "am" -> "ማረጋገጫ ኮድ"
        else -> "Verification Code"
    }
    
    fun getMyOrders(language: String): String = when (language) {
        "am" -> "ትዕዛዞቼ"
        else -> "My Orders"
    }
    
    fun getMyReservations(language: String): String = when (language) {
        "am" -> "ስምምነቶቼ"
        else -> "My Reservations"
    }
    
    fun getMenu(language: String): String = when (language) {
        "am" -> "ምግብ ዝርዝር"
        else -> "Menu"
    }
    
    fun getRooms(language: String): String = when (language) {
        "am" -> "ክፍሎች"
        else -> "Rooms"
    }
    
    fun getSettings(language: String): String = when (language) {
        "am" -> "ቅንብሮች"
        else -> "Settings"
    }
    
    fun getLogout(language: String): String = when (language) {
        "am" -> "ውጣ"
        else -> "Logout"
    }
    
    fun getPrice(language: String): String = when (language) {
        "am" -> "ዋጋ"
        else -> "Price"
    }
    
    fun getBook(language: String): String = when (language) {
        "am" -> "ይመዝገቡ"
        else -> "Book"
    }
    
    fun getOrder(language: String): String = when (language) {
        "am" -> "ትዕዛዝ"
        else -> "Order"
    }
    
    fun getCheckIn(language: String): String = when (language) {
        "am" -> "ግባ"
        else -> "Check In"
    }
    
    fun getCheckOut(language: String): String = when (language) {
        "am" -> "ውጣ"
        else -> "Check Out"
    }
    
    fun getGuests(language: String): String = when (language) {
        "am" -> "ጎብኚዎች"
        else -> "Guests"
    }
    
    fun getLanguage(language: String): String = when (language) {
        "am" -> "ቋንቋ"
        else -> "Language"
    }
    
    fun getEnglish(language: String): String = when (language) {
        "am" -> " English"
        else -> "English"
    }
    
    fun getAmharic(language: String): String = when (language) {
        "am" -> "አማርኛ"
        else -> "Amharic"
    }
}
