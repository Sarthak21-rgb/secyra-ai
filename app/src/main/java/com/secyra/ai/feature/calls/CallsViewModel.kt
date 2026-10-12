package com.secyra.ai.feature.calls

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.PhoneMissed
import android.content.Intent
import android.net.Uri
import com.secyra.ai.core.permissions.PermissionManager
import com.secyra.ai.core.security.SecurityEngine
import com.secyra.ai.core.telecom.CallManager
import com.secyra.ai.core.telecom.DefaultPhoneRoleManager
import com.secyra.ai.core.telecom.CallGroupingService
import com.secyra.ai.core.telecom.CallGroupingConfig
import com.secyra.ai.core.telecom.CallGroup
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
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class CallsViewModel @Inject constructor(
    private val securityEngine: SecurityEngine,
    private val permissionManager: PermissionManager,
    private val callLogRepository: CallLogRepository,
    private val contactRepository: ContactRepository,
    private val callManager: CallManager,
    private val defaultPhoneRoleManager: DefaultPhoneRoleManager,
    private val callGroupingService: CallGroupingService,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val sharedPrefs = context.getSharedPreferences("onboarding_prefs", Context.MODE_PRIVATE)
    
    companion object {
        private const val KEY_LAST_SHOWN_DATE = "last_onboarding_shown_date"
        private const val KEY_USER_SET_DEFAULT = "user_set_default_phone"
    }

    private val _uiState = MutableStateFlow(CallsUiState())
    val uiState: StateFlow<CallsUiState> = _uiState.asStateFlow()

    private var allCalls: List<CallItem> = emptyList()
    private var allContacts: List<Contact> = emptyList()
    private var allCallGroups: List<CallGroup> = emptyList()

    init {
        loadRecentCalls()
        loadContacts()
        loadBlockedNumbers()
        checkDefaultPhoneStatus()
    }

    fun onPageSelected(page: PageIndex) {
        _uiState.value = _uiState.value.copy(selectedPage = page)
    }

    fun onTabSelected(tab: CallTab) {
        _uiState.value = _uiState.value.copy(selectedTab = tab)
    }

    fun checkDefaultPhoneStatus() {
        defaultPhoneRoleManager.checkDefaultPhoneStatus()
        _uiState.value = _uiState.value.copy(
            isDefaultPhoneApp = defaultPhoneRoleManager.isDefaultPhoneApp.value
        )
        
        // Check if we should show onboarding banner
        _uiState.value = _uiState.value.copy(
            showOnboardingPrompt = shouldShowOnboardingBanner()
        )
    }
    
    private fun shouldShowOnboardingBanner(): Boolean {
        // Never show if user has already set as default
        if (sharedPrefs.getBoolean(KEY_USER_SET_DEFAULT, false)) {
            return false
        }
        
        // Don't show if already default
        if (defaultPhoneRoleManager.isDefaultPhoneApp.value) {
            return false
        }
        
        // Check if shown today
        val lastShownDate = sharedPrefs.getLong(KEY_LAST_SHOWN_DATE, 0)
        val today = getTodayDate()
        
        if (lastShownDate == today) {
            return false // Already shown today
        }
        
        return true // Show once per day
    }
    
    private fun getTodayDate(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
    
    fun dismissOnboardingPrompt() {
        // Record that we showed it today
        sharedPrefs.edit()
            .putLong(KEY_LAST_SHOWN_DATE, getTodayDate())
            .apply()
        
        _uiState.value = _uiState.value.copy(showOnboardingPrompt = false)
    }
    
    fun markUserSetDefault() {
        // Mark that user has set Secyra as default
        sharedPrefs.edit()
            .putBoolean(KEY_USER_SET_DEFAULT, true)
            .apply()
        
        _uiState.value = _uiState.value.copy(showOnboardingPrompt = false)
    }

    private fun loadRecentCalls() {
        viewModelScope.launch {
            try {
                // Load raw call records for grouping
                val rawCalls = callLogRepository.getRawCallRecords(limit = 1000)
                android.util.Log.d("CallsViewModel", "Total raw calls from repository: ${rawCalls.size}")

                // Group calls using the grouping service
                val groupingResult = callGroupingService.groupCalls(rawCalls)
                allCallGroups = groupingResult.groups
                android.util.Log.d("CallsViewModel", "Total call groups created: ${groupingResult.totalGroups}")

                // Convert grouped calls back to CallItem for UI compatibility
                val groupedCallItems = groupingResult.groups.flatMap { group ->
                    group.calls.map { rawCall ->
                        convertRawCallToCallItem(rawCall, group)
                    }
                }

                allCalls = groupedCallItems

                // Don't filter by time here - let the UI tabs handle filtering
                // Recent tab will filter to 3 days, All tab will filter to 7 months
                _uiState.value = _uiState.value.copy(
                    recentCalls = groupedCallItems,
                    callGroups = groupingResult.groups
                )
            } catch (e: Exception) {
                android.util.Log.e("CallsViewModel", "Error loading calls", e)
                // If call log access fails, show empty state instead of sample data
                _uiState.value = _uiState.value.copy(
                    recentCalls = emptyList(),
                    callGroups = emptyList()
                )
            }
        }
    }

    /**
     * Convert a RawCallRecord to CallItem for UI display
     */
    private fun convertRawCallToCallItem(rawCall: com.secyra.ai.core.telecom.RawCallRecord, group: CallGroup): CallItem {
        val displayName = rawCall.contactName ?: "Unknown"
        val truncatedName = if (displayName.length > 20) {
            displayName.take(20) + "..."
        } else {
            displayName
        }
        val initials = if (rawCall.contactName != null && rawCall.contactName.isNotEmpty()) {
            rawCall.contactName.first().toString().uppercase()
        } else {
            "U"
        }

        val callType = when (rawCall.callType) {
            com.secyra.ai.core.telecom.CallType.INCOMING -> CallType.INCOMING
            com.secyra.ai.core.telecom.CallType.OUTGOING -> CallType.OUTGOING
            com.secyra.ai.core.telecom.CallType.MISSED -> CallType.MISSED
            com.secyra.ai.core.telecom.CallType.REJECTED -> CallType.REJECTED
            com.secyra.ai.core.telecom.CallType.BLOCKED -> CallType.BLOCKED
            com.secyra.ai.core.telecom.CallType.VOICEMAIL -> CallType.INCOMING
            com.secyra.ai.core.telecom.CallType.UNKNOWN -> CallType.UNKNOWN
        }

        val callTypeLabel = when (callType) {
            CallType.INCOMING -> "Incoming"
            CallType.OUTGOING -> "Outgoing"
            CallType.MISSED -> "Missed"
            CallType.REJECTED -> "Declined"
            CallType.BLOCKED -> "Blocked"
            CallType.UNKNOWN -> "Call"
        }

        val callTypeIcon = when (callType) {
            CallType.INCOMING -> Icons.Default.Phone
            CallType.OUTGOING -> Icons.Default.Phone
            CallType.MISSED -> Icons.AutoMirrored.Filled.PhoneMissed
            CallType.REJECTED -> Icons.Default.PhoneDisabled
            CallType.BLOCKED -> Icons.Default.Block
            CallType.UNKNOWN -> Icons.Default.Phone
        }

        val isSpam = group.isBlocked
        val securityTag = when {
            isSpam -> SecurityTag.BLOCKED
            rawCall.contactName != null -> SecurityTag.UNKNOWN // Saved contact - will be hidden in UI
            else -> SecurityTag.UNKNOWN
        }

        return CallItem(
            id = rawCall.id.toString(),
            name = truncatedName,
            initials = initials,
            status = callTypeLabel,
            statusColor = if (isSpam) RiskHigh else SecyraTextSecondary,
            avatarColor = TiffanyBlue, // Will be computed in UI based on contact
            timestamp = formatTimestamp(rawCall.timestampMillis),
            phoneNumber = rawCall.phoneNumber,
            isSpam = isSpam,
            isTrusted = false, // Only for premium users
            callIconColor = if (isSpam) RiskHigh else TiffanyBlue,
            isSavedContact = rawCall.isSavedContact,
            contactId = null,
            callCountInHour = group.totalCallCount, // Use group count
            callTypeIcon = callTypeIcon,
            callType = callType,
            securityTag = securityTag,
            timestampMillis = rawCall.timestampMillis
        )
    }

    private fun formatTimestamp(timestampMillis: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestampMillis
        val dayInMillis = 24 * 60 * 60 * 1000L

        return when {
            diff < dayInMillis -> {
                // Today - show time
                val date = java.util.Date(timestampMillis)
                val timeFormat = java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault())
                timeFormat.format(date)
            }
            diff < 2 * dayInMillis -> "Yesterday"
            diff < 7 * dayInMillis -> {
                val date = java.util.Date(timestampMillis)
                val dayFormat = java.text.SimpleDateFormat("EEEE", java.util.Locale.getDefault())
                dayFormat.format(date)
            }
            else -> {
                val date = java.util.Date(timestampMillis)
                val dateFormat = java.text.SimpleDateFormat("d MMM yyyy", java.util.Locale.getDefault())
                dateFormat.format(date)
            }
        }
    }

    private fun loadContacts() {
        viewModelScope.launch {
            try {
                val contacts = contactRepository.searchContacts(query = "", limit = 100)
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

    // Sample data generation removed - using real call log data only
    // private fun generateSampleCalls(): List<CallItem> { ... }

    fun onSearchChange(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        
        // Filter calls based on search query
        val filteredCalls = if (query.isBlank()) {
            allCalls
        } else {
            allCalls.filter { call ->
                call.name.contains(query, ignoreCase = true) ||
                call.phoneNumber.contains(query, ignoreCase = true) ||
                call.status.contains(query, ignoreCase = true)
            }
        }
        
        // Also search contacts
        val filteredContacts = if (query.isBlank()) {
            emptyList<Contact>()
        } else {
            viewModelScope.launch {
                try {
                    val contacts = contactRepository.searchContacts(query, limit = 20)
                    _uiState.value = _uiState.value.copy(searchResults = contacts)
                } catch (e: Exception) {
                    // Ignore search errors
                }
            }
            emptyList<Contact>()
        }
        
        _uiState.value = _uiState.value.copy(
            recentCalls = filteredCalls,
            searchResults = emptyList<Contact>() // Will be populated by coroutine
        )
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

    fun onCallClick(call: CallItem) {
        // TODO: Navigate to call details screen
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

    fun onViewContactProfile(call: CallItem) {
        _uiState.value = _uiState.value.copy(selectedContactForProfile = call)
    }

    fun onContactProfileDismissed() {
        _uiState.value = _uiState.value.copy(selectedContactForProfile = null)
    }

    fun onCallContact(contact: Contact) {
        // Trigger a phone call
        val intent = Intent(Intent.ACTION_CALL).apply {
            data = Uri.parse("tel:${contact.phoneNumber}")
        }
        _uiState.value = _uiState.value.copy(
            callIntent = intent
        )
    }

    fun onViewContactProfileForContact(contact: Contact) {
        val callItem = CallItem(
            id = contact.id,
            name = contact.name,
            initials = if (contact.name.isNotEmpty()) contact.name.first().toString().uppercase() else "C",
            status = "Contact",
            statusColor = SecyraTextSecondary,
            avatarColor = TiffanyBlue.copy(alpha = 0.15f),
            timestamp = "",
            phoneNumber = contact.phoneNumber,
            isSpam = false,
            isTrusted = true,
            callIconColor = TiffanyBlue,
            isSavedContact = true,
            contactId = contact.id,
            callCountInHour = 0,
            callTypeIcon = Icons.Default.Phone,
            callType = CallType.UNKNOWN,
            securityTag = SecurityTag.TRUSTED
        )
        _uiState.value = _uiState.value.copy(selectedContactForProfile = callItem)
    }

    fun onBlockNumber(phoneNumber: String) {
        viewModelScope.launch {
            // Add to blocked numbers list
            val blockedNumber = BlockedNumber(
                id = phoneNumber,
                number = phoneNumber,
                displayName = _uiState.value.selectedContactForProfile?.name ?: "Unknown",
                blockedAt = System.currentTimeMillis().toString()
            )
            val currentBlocked = _uiState.value.blockedNumbers.toMutableList()
            currentBlocked.add(blockedNumber)
            _uiState.value = _uiState.value.copy(blockedNumbers = currentBlocked)

            // Update recent calls to mark as blocked
            val updatedCalls = _uiState.value.recentCalls.map { call ->
                if (call.phoneNumber == phoneNumber) {
                    call.copy(isSpam = true)
                } else {
                    call
                }
            }
            _uiState.value = _uiState.value.copy(recentCalls = updatedCalls)
        }
    }

    fun onUnblockNumber(phoneNumber: String) {
        viewModelScope.launch {
            // Remove from blocked numbers list
            val currentBlocked = _uiState.value.blockedNumbers.toMutableList()
            currentBlocked.removeAll { it.number == phoneNumber }
            _uiState.value = _uiState.value.copy(blockedNumbers = currentBlocked)

            // Update recent calls to remove blocked status
            val updatedCalls = _uiState.value.recentCalls.map { call ->
                if (call.phoneNumber == phoneNumber) {
                    call.copy(isSpam = false)
                } else {
                    call
                }
            }
            _uiState.value = _uiState.value.copy(recentCalls = updatedCalls)
        }
    }
}

data class CallsUiState(
    val searchQuery: String = "",
    val selectedTab: CallTab = CallTab.Recent,
    val selectedPage: PageIndex = PageIndex.Calls,
    val recentCalls: List<CallItem> = emptyList(),
    val contacts: List<Contact> = emptyList(),
    val blockedNumbers: List<BlockedNumber> = emptyList(),
    val searchResults: List<Contact> = emptyList(),
    val callIntent: Intent? = null,
    val isDefaultPhoneApp: Boolean = false,
    val showDefaultPhoneRequest: Boolean = false,
    val showOnboardingPrompt: Boolean = true,
    val selectedContactForProfile: CallItem? = null,
    val callGroups: List<com.secyra.ai.core.telecom.CallGroup> = emptyList()
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
    val isSavedContact: Boolean = false,
    val contactId: String? = null,
    val callCountInHour: Int = 0,
    val callTypeIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val callType: CallType,
    val securityTag: SecurityTag,
    val timestampMillis: Long = System.currentTimeMillis()
)

enum class CallType {
    INCOMING,
    OUTGOING,
    MISSED,
    REJECTED,
    BLOCKED,
    UNKNOWN
}

enum class SecurityTag {
    TRUSTED,
    UNKNOWN,
    SUSPICIOUS,
    SPAM,
    BLOCKED
}

enum class CallTab(val displayName: String) {
    All("All"),
    Recent("Recent"),
    Contacts("Contacts"),
    Blocked("Blocked"),
    Spam("Spam")
}

enum class PageIndex {
    Calls,
    Messages,
    Emails
}

data class BlockedNumber(
    val id: String,
    val number: String,
    val displayName: String? = null,
    val blockedAt: String
)
