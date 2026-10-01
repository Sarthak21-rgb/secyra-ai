package com.secyra.ai.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import com.secyra.ai.core.security.SecurityEngine
import com.secyra.ai.core.ui.theme.RiskCritical
import com.secyra.ai.core.ui.theme.RiskHigh
import com.secyra.ai.core.ui.theme.RiskLow
import com.secyra.ai.core.ui.theme.RiskModerate
import com.secyra.ai.core.ui.theme.SecyraTextMuted
import com.secyra.ai.core.ui.theme.TiffanyBlue
import com.secyra.ai.domain.model.RiskLevel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val securityEngine: SecurityEngine
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadSecurityStatus()
        loadRecentActivity()
    }

    private fun loadSecurityStatus() {
        viewModelScope.launch {
            // In a real implementation, this would calculate from actual security signals
            // For now, return a truthful state since we don't have real data
            _uiState.value = _uiState.value.copy(
                securityScore = 0, // 0 indicates no real score calculated yet
                securityStatus = SecurityStatus(
                    displayText = "Security check available",
                    description = "Run a security check to assess your device protection.",
                    subText = ""
                ),
                hasSecurityAlert = false, // No real alerts yet
                alertCount = 0
            )
        }
    }

    private fun loadRecentActivity() {
        viewModelScope.launch {
            // In a real implementation, this would load from actual event storage
            // For now, return empty list as we don't have real activity data
            _uiState.value = _uiState.value.copy(
                recentActivity = emptyList()
            )
        }
    }

    fun onNotificationClick() {
        _uiState.value = _uiState.value.copy(
            navigationEvent = NavigationEvent.NavigateToNotifications
        )
    }

    fun onProfileClick() {
        _uiState.value = _uiState.value.copy(
            navigationEvent = NavigationEvent.NavigateToProfile
        )
    }

    fun onViewSecurityCenter() {
        _uiState.value = _uiState.value.copy(
            navigationEvent = NavigationEvent.NavigateToSecurity
        )
    }

    fun onViewAlerts() {
        _uiState.value = _uiState.value.copy(
            navigationEvent = NavigationEvent.NavigateToSecurity
        )
    }

    fun onQuickActionClick(action: QuickAction) {
        val event = when (action) {
            QuickAction.CheckLink -> NavigationEvent.NavigateToLinkSecurity
            QuickAction.CheckWeb -> NavigationEvent.NavigateToScan // Web check in Scan
            QuickAction.CheckNumber -> NavigationEvent.NavigateToCalls
            QuickAction.AnalyzeMessage -> NavigationEvent.NavigateToMessageSecurity
            QuickAction.ScanQR -> NavigationEvent.NavigateToScan
            QuickAction.TalkToSecyra -> NavigationEvent.NavigateToAI
            QuickAction.SecurityCheck -> NavigationEvent.NavigateToSecurity
            QuickAction.SecuritySession -> NavigationEvent.NavigateToAI
        }
        _uiState.value = _uiState.value.copy(navigationEvent = event)
    }

    fun onViewAllActions() {
        _uiState.value = _uiState.value.copy(
            navigationEvent = NavigationEvent.NavigateToAllActions
        )
    }

    fun onActivityClick(activity: SecurityActivityItem) {
        // Navigate to appropriate detail screen based on activity type
        // For now, navigate to security center
        _uiState.value = _uiState.value.copy(
            navigationEvent = NavigationEvent.NavigateToSecurity
        )
    }

    fun onViewActivityHistory() {
        _uiState.value = _uiState.value.copy(
            navigationEvent = NavigationEvent.NavigateToActivityHistory
        )
    }

    fun onNavigationEventHandled() {
        _uiState.value = _uiState.value.copy(navigationEvent = null)
    }
}

data class HomeUiState(
    val userName: String = "Sarthak", // TODO: Load from user profile
    val securityScore: Int = 0,
    val securityStatus: SecurityStatus = SecurityStatus(
        displayText = "Security check available",
        description = "Run a security check to assess your device protection.",
        subText = ""
    ),
    val hasSecurityAlert: Boolean = false,
    val alertCount: Int = 0,
    val recentActivity: List<SecurityActivityItem> = emptyList(),
    val navigationEvent: NavigationEvent? = null
)

data class SecurityStatus(
    val displayText: String,
    val description: String,
    val subText: String = ""
)

data class SecurityActivityItem(
    val id: String,
    val type: String,
    val description: String,
    val timestamp: String,
    val riskLevel: RiskLevel
)

sealed class NavigationEvent {
    data object NavigateToLinkSecurity : NavigationEvent()
    data object NavigateToSecurity : NavigationEvent()
    data object NavigateToAI : NavigationEvent()
    data object NavigateToProSubscription : NavigationEvent()
    data object NavigateToScan : NavigationEvent()
    data object NavigateToCalls : NavigationEvent()
    data object NavigateToMessageSecurity : NavigationEvent()
    data object NavigateToNotifications : NavigationEvent()
    data object NavigateToProfile : NavigationEvent()
    data object NavigateToAllActions : NavigationEvent()
    data object NavigateToActivityHistory : NavigationEvent()
}

enum class QuickAction {
    CheckLink,
    CheckWeb,
    CheckNumber,
    AnalyzeMessage,
    ScanQR,
    TalkToSecyra,
    SecurityCheck,
    SecuritySession
}
