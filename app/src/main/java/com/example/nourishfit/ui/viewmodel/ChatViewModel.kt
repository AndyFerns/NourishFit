package com.example.nourishfit.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.nourishfit.R
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
// import com.google.ai.client.generativeai.type.text
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ChatMessage(
    val message: String,
    val role: String, // "user" or "model"
    val isLoading: Boolean = false
)

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private val _chatHistory = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatHistory: StateFlow<List<ChatMessage>> = _chatHistory.asStateFlow()

    private val generativeModel: GenerativeModel

    init {
        val apiKey = application.applicationContext.getString(R.string.gemini_api_key)

        generativeModel = GenerativeModel(
            modelName = "gemini-2.5-flash-preview-09-2025",
            apiKey = apiKey,
            systemInstruction = content(role = "system") {
                // This 'text' function is now correctly resolved
                // because it's part of the 'content' builder's scope.
                text(
                    "You are a friendly and encouraging fitness and diet coach named Nourish. " +
                            "Your goal is to provide simple, actionable diet plans and workout routines. " +
                            "Keep your answers concise and easy to understand."
                )
            }
        )
    }

    fun sendMessage(userPrompt: String) {
        _chatHistory.value = _chatHistory.value + ChatMessage(userPrompt, "user")
        _chatHistory.value = _chatHistory.value + ChatMessage("Nourish is typing...", "model", isLoading = true)

        viewModelScope.launch {
            try {
                val response = generativeModel.generateContent(userPrompt)
                _chatHistory.value = _chatHistory.value.filterNot { it.isLoading }
                _chatHistory.value = _chatHistory.value + ChatMessage(response.text ?: "Sorry, I had trouble thinking.", "model")

            } catch (e: Exception) {
                _chatHistory.value = _chatHistory.value.filterNot { it.isLoading }
                _chatHistory.value = _chatHistory.value + ChatMessage("Error: ${e.message}", "model")
            }
        }
    }
}

