package com.gohahotel.connect.core.localization

import android.content.Context
import androidx.datastore.preferences.core.edit
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

// Localized string resources
@Suppress("unused")
object LocalizedStrings {
    
    @Suppress("unused")
    fun getWelcome(language: String): String = when (language) {
        "am" -> "ወደ ጎሃ ሆቴል እንኳን ደህና መጡ"
        else -> "Welcome to Goha Hotel"
    }
    
    @Suppress("unused")
    fun getSignIn(language: String): String = when (language) {
        "am" -> "ግባ"
        else -> "Sign In"
    }
    
    @Suppress("unused")
    fun getRegister(language: String): String = when (language) {
        "am" -> "ምዝገባ"
        else -> "Register"
    }
    
    @Suppress("unused")
    fun getEmail(language: String): String = when (language) {
        "am" -> "ኢሜይል"
        else -> "Email"
    }
    
    @Suppress("unused")
    fun getPassword(language: String): String = when (language) {
        "am" -> "ይለፍ ቃል"
        else -> "Password"
    }
    
    @Suppress("unused")
    fun getFullName(language: String): String = when (language) {
        "am" -> "ሙሉ ስም"
        else -> "Full Name"
    }
    
    @Suppress("unused")
    fun getPhoneNumber(language: String): String = when (language) {
        "am" -> "ስልክ ቁጥር"
        else -> "Phone Number"
    }
    
    @Suppress("unused")
    fun getAddress(language: String): String = when (language) {
        "am" -> "አድራሻ"
        else -> "Address"
    }
    
    @Suppress("unused")
    fun getCreateAccount(language: String): String = when (language) {
        "am" -> "ሂሳብ ፍጠር"
        else -> "Create Account"
    }
    
    @Suppress("unused")
    fun getVerificationCode(language: String): String = when (language) {
        "am" -> "ማረጋገጫ ኮድ"
        else -> "Verification Code"
    }
    
    @Suppress("unused")
    fun getMyOrders(language: String): String = when (language) {
        "am" -> "ትዕዛዞቼ"
        else -> "My Orders"
    }
    
    @Suppress("unused")
    fun getMyReservations(language: String): String = when (language) {
        "am" -> "ስምምነቶቼ"
        else -> "My Reservations"
    }
    
    @Suppress("unused")
    fun getMenu(language: String): String = when (language) {
        "am" -> "ምግብ ዝርዝር"
        else -> "Menu"
    }
    
    @Suppress("unused")
    fun getRooms(language: String): String = when (language) {
        "am" -> "ክፍሎች"
        else -> "Rooms"
    }
    
    @Suppress("unused")
    fun getSettings(language: String): String = when (language) {
        "am" -> "ቅንብሮች"
        else -> "Settings"
    }
    
    @Suppress("unused")
    fun getLogout(language: String): String = when (language) {
        "am" -> "ውጣ"
        else -> "Logout"
    }
    
    @Suppress("unused")
    fun getPrice(language: String): String = when (language) {
        "am" -> "ዋጋ"
        else -> "Price"
    }
    
    @Suppress("unused")
    fun getBook(language: String): String = when (language) {
        "am" -> "ይመዝገቡ"
        else -> "Book"
    }
    
    @Suppress("unused")
    fun getOrder(language: String): String = when (language) {
        "am" -> "ትዕዛዝ"
        else -> "Order"
    }
    
    @Suppress("unused")
    fun getCheckIn(language: String): String = when (language) {
        "am" -> "ግባ"
        else -> "Check In"
    }
    
    @Suppress("unused")
    fun getCheckOut(language: String): String = when (language) {
        "am" -> "ውጣ"
        else -> "Check Out"
    }
    
    @Suppress("unused")
    fun getGuests(language: String): String = when (language) {
        "am" -> "ጎብኚዎች"
        else -> "Guests"
    }
    
    @Suppress("unused")
    fun getLanguage(language: String): String = when (language) {
        "am" -> "ቋንቋ"
        else -> "Language"
    }
    
    @Suppress("unused")
    fun getEnglish(language: String): String = when (language) {
        "am" -> "English"
        else -> "English"
    }
    
    @Suppress("unused")
    fun getAmharic(language: String): String = when (language) {
        "am" -> "አማርኛ"
        else -> "Amharic"
    }
    
    @Suppress("unused")
    fun getConfirmPassword(language: String): String = when (language) {
        "am" -> "ይለፍ ቃል ያረጋግጡ"
        else -> "Confirm Password"
    }
    
    @Suppress("unused")
    fun getIdentityDocument(language: String): String = when (language) {
        "am" -> "የመታወቂያ ሰነድ"
        else -> "Identity Document"
    }
    
    @Suppress("unused")
    fun getUploadDocument(language: String): String = when (language) {
        "am" -> "ሰነድ ይጫኑ"
        else -> "Upload Document"
    }
    
    @Suppress("unused")
    fun getDocumentUploaded(language: String): String = when (language) {
        "am" -> "ሰነድ ተጫነ ✓"
        else -> "Document Uploaded ✓"
    }
    
    @Suppress("unused")
    fun getForgotPassword(language: String): String = when (language) {
        "am" -> "ይለፍ ቃል ረስተዋል?"
        else -> "Forgot Password?"
    }
    
    @Suppress("unused")
    fun getResetPassword(language: String): String = when (language) {
        "am" -> "ይለፍ ቃል ዳግም ያስተካክሉ"
        else -> "Reset Password"
    }
    
    @Suppress("unused")
    fun getGuestDetails(language: String): String = when (language) {
        "am" -> "የጎብኚ ዝርዝር"
        else -> "Guest Details"
    }
    
    @Suppress("unused")
    fun getRoomBookings(language: String): String = when (language) {
        "am" -> "የክፍል ስምምነቶች"
        else -> "Room Bookings"
    }
    
    @Suppress("unused")
    fun getFoodOrders(language: String): String = when (language) {
        "am" -> "የምግብ ትዕዛዞች"
        else -> "Food Orders"
    }
    
    @Suppress("unused")
    fun getStatus(language: String): String = when (language) {
        "am" -> "ሁኔታ"
        else -> "Status"
    }
    
    @Suppress("unused")
    fun getPending(language: String): String = when (language) {
        "am" -> "በመጠባበቅ ላይ"
        else -> "Pending"
    }
    
    @Suppress("unused")
    fun getConfirmed(language: String): String = when (language) {
        "am" -> "ተረጋግጧል"
        else -> "Confirmed"
    }
    
    @Suppress("unused")
    fun getCompleted(language: String): String = when (language) {
        "am" -> "ተጠናቅቋል"
        else -> "Completed"
    }
    
    @Suppress("unused")
    fun getCancelled(language: String): String = when (language) {
        "am" -> "ተሰርዟል"
        else -> "Cancelled"
    }
    
    @Suppress("unused")
    fun getTotalAmount(language: String): String = when (language) {
        "am" -> "ጠቅላላ መጠን"
        else -> "Total Amount"
    }
    
    @Suppress("unused")
    fun getDownload(language: String): String = when (language) {
        "am" -> "ያውርዱ"
        else -> "Download"
    }
    
    @Suppress("unused")
    fun getBack(language: String): String = when (language) {
        "am" -> "ተመለስ"
        else -> "Back"
    }
    
    @Suppress("unused")
    fun getNext(language: String): String = when (language) {
        "am" -> "ቀጣይ"
        else -> "Next"
    }
    
    @Suppress("unused")
    fun getSubmit(language: String): String = when (language) {
        "am" -> "ያስገቡ"
        else -> "Submit"
    }
    
    @Suppress("unused")
    fun getCancel(language: String): String = when (language) {
        "am" -> "ይሰርዙ"
        else -> "Cancel"
    }
    
    @Suppress("unused")
    fun getSuccess(language: String): String = when (language) {
        "am" -> "ስኬት"
        else -> "Success"
    }
    
    @Suppress("unused")
    fun getError(language: String): String = when (language) {
        "am" -> "ስህተት"
        else -> "Error"
    }
    
    @Suppress("unused")
    fun getLoading(language: String): String = when (language) {
        "am" -> "በመጫን ላይ..."
        else -> "Loading..."
    }
    
    @Suppress("unused")
    fun getNoResults(language: String): String = when (language) {
        "am" -> "ውጤት አልተገኘም"
        else -> "No Results"
    }
    
    @Suppress("unused")
    fun getAbout(language: String): String = when (language) {
        "am" -> "ስለ"
        else -> "About"
    }
    
    @Suppress("unused")
    fun getContact(language: String): String = when (language) {
        "am" -> "ያግኙን"
        else -> "Contact"
    }
    
    @Suppress("unused")
    fun getPrivacy(language: String): String = when (language) {
        "am" -> "ግላዊነት"
        else -> "Privacy"
    }
    
    @Suppress("unused")
    fun getTerms(language: String): String = when (language) {
        "am" -> "ውል"
        else -> "Terms"
    }
    
    @Suppress("unused")
    fun getCopyright(language: String): String = when (language) {
        "am" -> "© ጎሃ ሆቴል · ታሪካዊ ጎንደር ከተማ ላይ ከፍ ያለ"
        else -> "© Goha Hotel · High Above the Historic City of Gondar"
    }
}
