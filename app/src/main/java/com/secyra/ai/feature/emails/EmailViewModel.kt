package com.secyra.ai.feature.emails

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.secyra.ai.core.permissions.PermissionManager
import com.secyra.ai.data.repository.Email
import com.secyra.ai.data.repository.EmailRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EmailViewModel @Inject constructor(
    private val emailRepository: EmailRepository,
    private val permissionManager: PermissionManager,
    private val gmailOAuthManager: GmailOAuthManager,
    private val outlookOAuthManager: OutlookOAuthManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(EmailUiState())
    val uiState: StateFlow<EmailUiState> = _uiState.asStateFlow()

    init {
        checkAccountConnection()
    }

    private fun checkAccountConnection() {
        viewModelScope.launch {
            emailRepository.isAccountConnected.collect { isConnected ->
                _uiState.value = _uiState.value.copy(isAccountConnected = isConnected)
                if (isConnected) {
                    loadEmails()
                }
            }
        }
    }

    fun loadEmails() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                val emails = emailRepository.getRecentEmails(limit = 50)
                _uiState.value = _uiState.value.copy(
                    emails = emails,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    emails = emptyList(),
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    /**
     * Start Gmail OAuth authentication
     */
    fun startGmailAuth(): Intent {
        return gmailOAuthManager.getSignInIntent()
    }

    /**
     * Handle Gmail OAuth result
     */
    fun handleGmailAuthResult(data: Intent?) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                val token = gmailOAuthManager.handleSignInResult(data)
                
                // Get user email from token (simplified - in production, decode JWT)
                val email = extractEmailFromToken(token)
                
                val account = EmailAccount(
                    provider = EmailProvider.GMAIL,
                    email = email,
                    accessToken = token
                )
                
                emailRepository.saveAccount(account)
                _uiState.value = _uiState.value.copy(isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Authentication failed: ${e.message}"
                )
            }
        }
    }

    /**
     * Start Outlook OAuth authentication
     */
    suspend fun startOutlookAuth(): String {
        return try {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val token = outlookOAuthManager.authenticate()
            
            // Get user email from token (simplified - in production, decode JWT)
            val email = extractEmailFromToken(token)
            
            val account = EmailAccount(
                provider = EmailProvider.OUTLOOK,
                email = email,
                accessToken = token
            )
            
            emailRepository.saveAccount(account)
            _uiState.value = _uiState.value.copy(isLoading = false)
            token
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                error = "Authentication failed: ${e.message}"
            )
            throw e
        }
    }

    /**
     * Disconnect the current account
     */
    fun disconnectAccount() {
        viewModelScope.launch {
            emailRepository.disconnectAccount()
            _uiState.value = _uiState.value.copy(
                isAccountConnected = false,
                emails = emptyList()
            )
        }
    }

    /**
     * Send an email
     */
    fun sendEmail(to: String, subject: String, body: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isSending = true, error = null)
                emailRepository.sendEmail(to, subject, body)
                _uiState.value = _uiState.value.copy(isSending = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSending = false,
                    error = "Failed to send email: ${e.message}"
                )
            }
        }
    }

    private fun extractEmailFromToken(token: String): String {
        // Simplified - in production, decode the JWT token to get the email
        // For now, return a placeholder
        return "user@example.com"
    }
}

data class EmailUiState(
    val emails: List<Email> = emptyList(),
    val isEmailIntegrationAvailable: Boolean = false,
    val hasPermission: Boolean = false,
    val isAccountConnected: Boolean = false,
    val isLoading: Boolean = true,
    val isSending: Boolean = false,
    val error: String? = null,
    val requestPermission: Boolean = false
)
