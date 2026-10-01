package com.secyra.ai.feature.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.secyra.ai.core.security.AIProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AIViewModel @Inject constructor(
    private val aiProvider: AIProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(AIUiState())
    val uiState: StateFlow<AIUiState> = _uiState.asStateFlow()

    init {
        // TODO: Enable subscription check when RevenueCat is properly configured
        // checkSubscriptionStatus()
        _uiState.value = _uiState.value.copy(isProUser = false) // Default to free
    }

    private fun checkSubscriptionStatus() {
        viewModelScope.launch {
            // TODO: Re-enable when RevenueCat is available
            // revenueCatConfig.isProUser { isPro ->
            //     _uiState.value = _uiState.value.copy(isProUser = isPro)
            // }
        }
    }

    fun sendMessage(message: String) {
        if (message.isBlank()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null
            )

            try {
                // Add user message
                val userMessage = ChatMessage(
                    role = "user",
                    content = message,
                    timestamp = System.currentTimeMillis()
                )
                _uiState.value = _uiState.value.copy(
                    messages = _uiState.value.messages + userMessage,
                    inputText = ""
                )

                // Get AI response
                val request = AIProvider.AIAnalysisRequest(
                    content = message,
                    context = mapOf("type" to "security_chat")
                )

                val response = aiProvider.chatWithAI(
                    conversation = _uiState.value.messages.map { 
                        AIProvider.AIChatMessage(it.role, it.content, it.timestamp)
                    },
                    newMessage = message
                )

                response.fold(
                    onSuccess = { aiMessage ->
                        val assistantMessage = ChatMessage(
                            role = "assistant",
                            content = aiMessage.content,
                            timestamp = aiMessage.timestamp
                        )
                        _uiState.value = _uiState.value.copy(
                            messages = _uiState.value.messages + assistantMessage,
                            isLoading = false
                        )
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "AI service unavailable: ${error.message}"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Unable to process request: ${e.message}"
                )
            }
        }
    }

    fun onInputChange(text: String) {
        _uiState.value = _uiState.value.copy(inputText = text)
    }

    fun clearChat() {
        _uiState.value = _uiState.value.copy(
            messages = emptyList(),
            error = null
        )
    }

    fun navigateToProSubscription() {
        _uiState.value = _uiState.value.copy(
            shouldNavigateToPro = true
        )
    }

    fun onNavigationHandled() {
        _uiState.value = _uiState.value.copy(shouldNavigateToPro = false)
    }
}

data class AIUiState(
    val isProUser: Boolean = false,
    val messages: List<ChatMessage> = emptyList(),
    val inputText: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val shouldNavigateToPro: Boolean = false
)

data class ChatMessage(
    val role: String, // "user" or "assistant"
    val content: String,
    val timestamp: Long
)

