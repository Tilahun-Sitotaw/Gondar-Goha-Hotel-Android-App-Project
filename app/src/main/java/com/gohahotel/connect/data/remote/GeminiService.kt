package com.gohahotel.connect.data.remote

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeminiService @Inject constructor() {

    private val config = "gemini-1.5-flash"
    
    // NOTE: You must provide your own Gemini API Key here or in BuildConfig
    private val apiKey = "YOUR_GEMINI_API_KEY_HERE"

    private val model = GenerativeModel(
        modelName = config,
        apiKey = apiKey,
        systemInstruction = content {
            text("You are the Goha Hotel Digital Concierge in Gondar, Ethiopia. " +
                 "Be professional, welcoming, and helpful. You have expert knowledge of Goha Hotel, " +
                 "Gondar's history (Fasilides Castle, Debre Berhan Selassie), and Ethiopian hospitality. " +
                 "Always encourage guests to enjoy the sunset from the hilltop terrace. " +
                 "If asked about rooms or food, provide general info and suggest using the app's 'Rooms' or 'Restaurant' tabs. " +
                 "You can speak English, Amharic, and French.")
        }
    )

    private val chat = model.startChat()

    suspend fun generateResponse(prompt: String): String {
        return try {
            if (apiKey == "YOUR_GEMINI_API_KEY_HERE") {
                return "API_KEY_MISSING"
            }
            val response = chat.sendMessage(prompt)
            response.text ?: "I'm sorry, I couldn't process that request."
        } catch (e: Exception) {
            "ERROR: ${e.message}"
        }
    }
}
