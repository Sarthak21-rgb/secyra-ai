package com.secyra.ai.feature.messages

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.secyra.ai.core.telecom.SmsRoleManager
import com.secyra.ai.data.repository.SmsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MessagesViewModel @Inject constructor(
    private val smsRepository: SmsRepository,
    private val smsRoleManager: SmsRoleManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(MessagesUiState())
    val uiState: StateFlow<MessagesUiState> = _uiState.asStateFlow()

    init {
        loadConversations()
        checkSmsRoleStatus()
    }

    private fun checkSmsRoleStatus() {
        val isDefault = smsRoleManager.isDefaultSmsApp()
        _uiState.value = _uiState.value.copy(isDefaultSmsApp = isDefault)
    }

    private fun loadConversations() {
        viewModelScope.launch {
            try {
                val conversations = smsRepository.getConversations()
                val unreadConversations = smsRepository.getUnreadConversations()

                _uiState.value = _uiState.value.copy(
                    conversations = conversations.map { MessageConversation.fromSmsConversation(it) },
                    unreadConversations = unreadConversations.map { MessageConversation.fromSmsConversation(it) },
                    suspiciousConversations = emptyList(), // TODO: Implement security analysis
                    blockedConversations = emptyList() // TODO: Implement blocked conversations
                )
            } catch (e: Exception) {
                // Handle error - could be permission denied
                _uiState.value = _uiState.value.copy(
                    conversations = emptyList(),
                    unreadConversations = emptyList()
                )
            }
        }
    }

    fun onTabSelected(tab: MessageTab) {
        _uiState.value = _uiState.value.copy(selectedTab = tab)
    }

    fun onConversationClick(conversation: MessageConversation) {
        // TODO: Navigate to conversation detail screen
    }
}

data class MessagesUiState(
    val selectedTab: MessageTab = MessageTab.All,
    val conversations: List<MessageConversation> = emptyList(),
    val unreadConversations: List<MessageConversation> = emptyList(),
    val suspiciousConversations: List<MessageConversation> = emptyList(),
    val blockedConversations: List<MessageConversation> = emptyList(),
    val isDefaultSmsApp: Boolean = false
)
