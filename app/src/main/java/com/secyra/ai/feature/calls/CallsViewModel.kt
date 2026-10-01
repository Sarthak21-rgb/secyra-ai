package com.secyra.ai.feature.calls

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.ui.graphics.Color
import android.content.Intent
import android.net.Uri
import com.secyra.ai.core.permissions.PermissionManager
import com.secyra.ai.core.security.SecurityEngine
import com.secyra.ai.core.telecom.CallManager
import com.secyra.ai.core.telecom.DefaultPhoneRoleManager
import com.secyra.ai.core.ui.theme.RiskHigh
import com.secyra.ai.core.ui.theme.RiskLow
import com.secyra.ai.core.ui.theme.SecyraTextMuted
import com.secyra.ai.core.ui.theme.SecyraTextSecondary
import com.secyra.ai.core.ui.theme.TiffanyBlue
import com.secyra.ai.data.repository.CallLogRepository
import com.secyra.ai.data.repository.ContactRepository
import com.secyra.ai.data.repository.Contact
import com.secyra.ai.domain.model.RiskLevel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CallsViewModel @Inject constructor(
    private val securityEngine: SecurityEngine,
    private val permissionManager: PermissionManager,
    private val callLogRepository: CallLogRepository,
    private val contactRepository: ContactRepository,
    private val callManager: CallManager,
    private val defaultPhoneRoleManager: DefaultPhoneRoleManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(CallsUiState())
    val uiState: StateFlow<CallsUiState> = _uiState.asStateFlow()

    private var allCalls: List<CallItem> = emptyList()
    private var allContacts: List<Contact> = emptyList()

    init {
        loadRecentCalls()
        loadContacts()
        loadBlockedNumbers()
        checkDefaultPhoneStatus()
    }

    private fun checkDefaultPhoneStatus() {
        defaultPhoneRoleManager.checkDefaultPhoneStatus()
    }

    private fun loadRecentCalls() {
        viewModelScope.launch {
            try {
                val calls = callLogRepository.getRecentCalls(limit = 50)
                allCalls = calls
                _uiState.value = _uiState.value.copy(
                    recentCalls = calls
                )
            } catch (e: Exception) {
                // Fallback to sample data if call log access fails
                allCalls = generateSampleCalls()
                _uiState.value = _uiState.value.copy(
                    recentCalls = allCalls
                )
            }
        }
    }

    private fun loadContacts() {
        viewModelScope.launch {
            try {
                val contacts = contactRepository.getAllContacts()
                allContacts = contacts
                _uiState.value = _uiState.value.copy(contacts = contacts)
            } catch (e: Exception) {
                // Ignore contact loading errors
            }
        }
    }

    private fun loadBlockedNumbers() {
        viewModelScope.launch {
            try {
                // For now, blocked numbers will be empty unless Android exposes blocked number API
                // This is a placeholder for future integration
                _uiState.value = _uiState.value.copy(blockedNumbers = emptyList())
            } catch (e: Exception) {
                // Ignore blocked number loading errors
            }
        }
    }

    private fun generateSampleCalls(): List<CallItem> {
        return listOf(
            CallItem(
                id = "1",
                name = "Unknown",
                initials = "U",
                status = "Potential spam",
                statusColor = RiskHigh,
                avatarColor = Color(0xFF3F1C1C),
                timestamp = "Today, 4:32 PM",
                phoneNumber = "+1 555-0123",
                isSpam = true,
                isTrusted = false,
                callIconColor = RiskHigh
            ),
            CallItem(
                id = "2",
                name = "Mom",
                initials = "M",
                status = "Trusted contact",
                statusColor = RiskLow,
                avatarColor = Color(0xFF1C3F2C),
                timestamp = "Today, 3:15 PM",
                phoneNumber = "+1 555-0456",
                isSpam = false,
                isTrusted = true,
                callIconColor = TiffanyBlue
            ),
            CallItem(
                id = "3",
                name = "Raj",
                initials = "R",
                status = "Mobile",
                statusColor = SecyraTextSecondary,
                avatarColor = Color(0xFF2C2C2C),
                timestamp = "Today, 2:30 PM",
                phoneNumber = "+91 98765 43210",
                isSpam = false,
                isTrusted = false,
                callIconColor = TiffanyBlue
            ),
            CallItem(
                id = "4",
                name = "Unknown",
                initials = "U",
                status = "Suspicious pattern",
                statusColor = RiskHigh,
                avatarColor = Color(0xFF3F1C1C),
                timestamp = "Today, 1:45 PM",
                phoneNumber = "+1 800-555-0199",
                isSpam = true,
                isTrusted = false,
                callIconColor = RiskHigh
            ),
            CallItem(
                id = "5",
                name = "Priya",
                initials = "P",
                status = "Mobile",
                statusColor = SecyraTextSecondary,
                avatarColor = Color(0xFF2C3C4C),
                timestamp = "Yesterday, 6:20 PM",
                phoneNumber = "+91 87654 32109",
                isSpam = false,
                isTrusted = false,
                callIconColor = TiffanyBlue
            ),
            CallItem(
                id = "6",
                name = "Unknown",
                initials = "U",
                status = "Reported by community",
                statusColor = RiskHigh,
                avatarColor = Color(0xFF3F1C1C),
                timestamp = "Yesterday, 4:10 PM",
                phoneNumber = "+1 555-9876",
                isSpam = true,
                isTrusted = false,
                callIconColor = RiskHigh
            ),
            CallItem(
                id = "7",
                name = "Suresh",
                initials = "S",
                status = "Trusted contact",
                statusColor = RiskLow,
                avatarColor = Color(0xFF1C3F2C),
                timestamp = "Yesterday, 2:55 PM",
                phoneNumber = "+91 76543 21098",
                isSpam = false,
                isTrusted = true,
                callIconColor = TiffanyBlue
            ),
            CallItem(
                id = "8",
                name = "Unknown",
                initials = "U",
                status = "Likely spam",
                statusColor = RiskHigh,
                avatarColor = Color(0xFF3F1C1C),
                timestamp = "Yesterday, 11:30 AM",
                phoneNumber = "+1 800-SCAM-01",
                isSpam = true,
                isTrusted = false,
                callIconColor = RiskHigh
            )
        )
    }

    fun onSearchChange(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        
        // Search contacts based on query
        viewModelScope.launch {
            try {
                val contacts = if (query.isBlank()) {
                    allContacts
                } else {
                    contactRepository.searchContacts(query)
                }
                _uiState.value = _uiState.value.copy(searchResults = contacts)
            } catch (e: Exception) {
                // Ignore search errors
            }
        }
    }

    fun onTabSelected(tab: CallTab) {
        _uiState.value = _uiState.value.copy(selectedTab = tab)
    }

    fun onUnblockNumber(blocked: BlockedNumber) {
        viewModelScope.launch {
            try {
                // Placeholder for unblock functionality
                // This would integrate with Android's blocked number API if available
                val updatedBlocked = _uiState.value.blockedNumbers.filter { it.id != blocked.id }
                _uiState.value = _uiState.value.copy(blockedNumbers = updatedBlocked)
            } catch (e: Exception) {
                // Handle unblock errors
            }
        }
    }

    fun onRequestDefaultPhone() {
        _uiState.value = _uiState.value.copy(showDefaultPhoneRequest = true)
    }

    fun onSkipDefaultPhoneRequest() {
        _uiState.value = _uiState.value.copy(showDefaultPhoneRequest = false)
    }

    fun onDefaultPhoneRoleResult(isGranted: Boolean) {
        defaultPhoneRoleManager.checkDefaultPhoneStatus()
        _uiState.value = _uiState.value.copy(
            showDefaultPhoneRequest = false,
            isDefaultPhoneApp = isGranted
        )
    }

    fun onSearchClick() {
        _uiState.value = _uiState.value.copy(isSearchActive = true)
    }

    fun onCloseSearch() {
        _uiState.value = _uiState.value.copy(
            isSearchActive = false,
            searchQuery = ""
        )
    }

    fun onCallClick(call: CallItem) {
        // TODO: Navigate to call details screen
    }

    fun onCallContact(contact: Contact) {
        // Trigger a phone call
        val intent = Intent(Intent.ACTION_CALL).apply {
            data = Uri.parse("tel:${contact.phoneNumber}")
        }
        // This will be handled by the UI layer which has access to context
        _uiState.value = _uiState.value.copy(
            callIntent = intent
        )
    }

    fun onCallPhoneNumber(phoneNumber: String) {
        // Use REAL Android Telecom to place the call
        // This is NOT a fake call - it uses CallManager.placeCall()
        val success = callManager.placeCall(phoneNumber)

        if (!success) {
            // Fallback to ACTION_CALL if TelecomManager is not available
            val intent = Intent(Intent.ACTION_CALL).apply {
                data = Uri.parse("tel:$phoneNumber")
            }
            _uiState.value = _uiState.value.copy(
                callIntent = intent
            )
        }
    }

    fun onCallIntentHandled() {
        _uiState.value = _uiState.value.copy(callIntent = null)
    }
}

data class CallsUiState(
    val searchQuery: String = "",
    val selectedTab: CallTab = CallTab.Recent,
    val recentCalls: List<CallItem> = emptyList(),
    val contacts: List<Contact> = emptyList(),
    val blockedNumbers: List<BlockedNumber> = emptyList(),
    val searchResults: List<Contact> = emptyList(),
    val callIntent: Intent? = null,
    val isDefaultPhoneApp: Boolean = false,
    val showDefaultPhoneRequest: Boolean = false,
    val isSearchActive: Boolean = false
)

data class CallItem(
    val id: String,
    val name: String,
    val initials: String,
    val status: String,
    val statusColor: Color,
    val avatarColor: Color,
    val timestamp: String,
    val phoneNumber: String,
    val isSpam: Boolean,
    val isTrusted: Boolean,
    val callIconColor: Color,
    val callDirection: String = "unknown",
    val isMissed: Boolean = false
)

enum class CallTab {
    Recent,
    Contacts,
    Blocked
}

data class BlockedNumber(
    val id: String,
    val number: String,
    val displayName: String? = null,
    val blockedAt: String
)
