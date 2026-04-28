package com.gohahotel.connect.ui.concierge

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gohahotel.connect.data.remote.GeminiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String,
    val isFromUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

data class ConciergeUiState(
    val messages: List<ChatMessage> = listOf(
        ChatMessage(
            text = "👋 Welcome to Goha Hotel! I'm your Digital Concierge.\n\n" +
                   "I can help you with:\n• Room information\n• Dining & menu\n" +
                   "• Local attractions\n• Hotel services\n• Sunset timings\n\n" +
                   "How can I assist you today?",
            isFromUser = false
        )
    ),
    val inputText: String  = "",
    val isTyping: Boolean  = false
)

@HiltViewModel
class ConciergeViewModel @Inject constructor(
    private val geminiService: GeminiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConciergeUiState())
    val uiState = _uiState.asStateFlow()

    fun onInputChange(text: String) = _uiState.update { it.copy(inputText = text) }

    fun sendMessage() {
        val text = _uiState.value.inputText.trim()
        if (text.isBlank()) return

        val userMessage = ChatMessage(text = text, isFromUser = true)
        _uiState.update {
            it.copy(
                messages  = it.messages + userMessage,
                inputText = "",
                isTyping  = true
            )
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isTyping = true) }
            
            // Try Gemini AI first
            val aiResponse = geminiService.generateResponse(text)
            
            val finalReply = if (aiResponse == "API_KEY_MISSING" || aiResponse.startsWith("ERROR:")) {
                // Fallback to rule-based engine if AI fails
                delay(1000)
                generateRuleBasedReply(text)
            } else {
                aiResponse
            }
            
            val botMessage = ChatMessage(text = finalReply, isFromUser = false)
            _uiState.update { it.copy(messages = it.messages + botMessage, isTyping = false) }
        }
    }

    private fun generateRuleBasedReply(input: String): String {
        val lower = input.lowercase()
        return when {
            // Greetings
            lower.containsAny("hello", "hi", "hey", "selam", "salam") ->
                "Hello! 😊 Welcome to Goha Hotel. How can I make your stay more comfortable today?"

            // Sunset
            lower.containsAny("sunset", "sun", "view", "terrace") ->
                "🌅 Sunset time at Goha Hill is approximately 6:30 PM – 7:00 PM.\n\n" +
                "Head to our rooftop terrace or restaurant for a breathtaking 360° view of Gondar city. " +
                "We recommend arriving 30 minutes early for the best spot!"

            // Rooms
            lower.containsAny("room", "suite", "king", "twin", "check", "book") ->
                "🏨 Goha Hotel offers several room categories:\n\n" +
                "• **Standard Rooms** – From 1,200 ETB/night\n" +
                "• **Twin Rooms** – From 1,500 ETB/night\n" +
                "• **King Rooms** – From 2,000 ETB/night\n" +
                "• **Suites** – From 3,500 ETB/night\n\n" +
                "All rooms include Wi-Fi, breakfast, and panoramic views. " +
                "Use the 'Rooms' section to book or call the front desk at extension 100."

            // Food / Restaurant
            lower.containsAny("food", "eat", "restaurant", "menu", "injera", "tibs", "doro") ->
                "🍽️ Our restaurant is open:\n• Breakfast: 7:00 – 10:00 AM\n• Lunch: 12:00 – 3:00 PM\n• Dinner: 6:00 – 10:00 PM\n\n" +
                "We serve authentic Ethiopian dishes (Injera, Tibs, Doro Wot, Kitfo) and international cuisine. " +
                "Use the 'Restaurant' tab to browse our menu and order to your room!"

            // Fasil Ghebbi / UNESCO
            lower.containsAny("fasil", "castle", "palace", "unesco", "royal") ->
                "🏛️ Fasil Ghebbi (Royal Enclosure) is just 1.2 km from the hotel!\n\n" +
                "This UNESCO World Heritage Site was built by Emperor Fasilides in 1636. " +
                "Open 8 AM – 6 PM, entry fee: 100 ETB.\n\n" +
                "Our front desk can arrange a guided tour. Shall I book one for you?"

            // Debre Berhan
            lower.containsAny("debre", "church", "berhan", "selassie") ->
                "⛪ Debre Berhan Selassie Church is 2.1 km from Goha Hotel.\n\n" +
                "Famous for its ceiling adorned with 80 painted cherub faces — one of Ethiopia's most beautiful churches.\n" +
                "Open 6 AM – 12 PM and 2 PM – 6 PM, entry fee: 50 ETB."

            // WiFi
            lower.containsAny("wifi", "wi-fi", "internet", "password") ->
                "📶 Complimentary Wi-Fi is available throughout the hotel.\n\n" +
                "• Network: **GohaHotel_Guest**\n• Password: available at the front desk\n\n" +
                "For any connectivity issues, please call extension 101."

            // Checkout / Check-in
            lower.containsAny("checkout", "check out", "check-out") ->
                "🕐 Check-out time is **12:00 PM (noon)**.\n\n" +
                "Late check-out (until 2 PM) may be available upon request — please contact the front desk (ext. 100)."
            lower.containsAny("check in", "checkin", "check-in", "arrival") ->
                "🕐 Check-in time is **2:00 PM**.\n\n" +
                "Early check-in may be available based on room availability. Our luggage storage is free!"

            // Pool / gym / spa
            lower.containsAny("pool", "swim", "gym", "fitness", "spa", "massage") ->
                "🏊 Hotel facilities:\n• Swimming pool: 7 AM – 8 PM\n• Fitness centre: 6 AM – 10 PM\n• Spa & massage: 9 AM – 7 PM (advance booking recommended)\n\n" +
                "Contact the front desk (ext. 100) to book spa treatments."

            // Airport / transport
            lower.containsAny("airport", "transport", "taxi", "car", "transfer") ->
                "🚗 Airport transfer service:\n• Gondar Airport is ~5 km from the hotel\n• Transfer fee: 300 ETB one-way\n\n" +
                "Book through the front desk (ext. 100) at least 2 hours in advance."

            // Emergency
            lower.containsAny("emergency", "help", "urgent", "doctor", "medical") ->
                "🚨 For emergencies, please:\n• Call **0** from your room phone (front desk)\n• Or dial **911** for external emergency services\n\n" +
                "Our staff is available 24/7. Your safety is our top priority."

            // Language
            lower.containsAny("amharic", "french", "language", "translate") ->
                "🌍 Our app supports **English**, **አማርኛ (Amharic)**, and **Français (French)**.\n\n" +
                "Go to Settings → Language to switch at any time."

            // Goodbye
            lower.containsAny("bye", "goodbye", "thank", "አመሰግናለሁ") ->
                "Thank you for choosing Goha Hotel! 🌟\n\n" +
                "We hope your stay is exceptional. Feel free to ask me anything anytime. " +
                "Enjoy your time in beautiful Gondar! 🇪🇹"

            // Default
            else ->
                "I'd be happy to help! Here are some things I can assist with:\n\n" +
                "• 🏨 Room bookings & services\n• 🍽️ Restaurant & food orders\n" +
                "• 🌅 Sunset timings & views\n• 🏛️ Local attractions & tours\n" +
                "• 📶 Wi-Fi & hotel facilities\n• 🚗 Transport & transfers\n\n" +
                "What would you like to know?"
        }
    }

    private fun String.containsAny(vararg keywords: String) =
        keywords.any { this.contains(it, ignoreCase = true) }
}
