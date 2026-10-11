package com.secyra.ai.feature.emails

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
    private val permissionManager: PermissionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(EmailUiState())
    val uiState: StateFlow<EmailUiState> = _uiState.asStateFlow()

    init {
        loadEmails()
    }

    fun loadEmails() {
        viewModelScope.launch {
            try {
                val emails = emailRepository.getRecentEmails(limit = 50)
                val isAvailable = emailRepository.isEmailIntegrationAvailable()
                val hasPermission = permissionManager.isPermissionGranted(
                    com.secyra.ai.core.permissions.PermissionManager.Permissions.READ_EXTERNAL_STORAGE
                )

                _uiState.value = _uiState.value.copy(
                    emails = emails,
                    isEmailIntegrationAvailable = isAvailable,
                    hasPermission = hasPermission,
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

    fun onRequestPermission() {
        // This would trigger a permission request in the UI
        _uiState.value = _uiState.value.copy(requestPermission = true)
    }

    fun onPermissionResult(granted: Boolean) {
        _uiState.value = _uiState.value.copy(requestPermission = false)
        if (granted) {
            loadEmails()
        }
    }
}

data class EmailUiState(
    val emails: List<Email> = emptyList(),
    val isEmailIntegrationAvailable: Boolean = false,
    val hasPermission: Boolean = false,
    val isLoading: Boolean = true,
    val error: String? = null,
    val requestPermission: Boolean = false
)
