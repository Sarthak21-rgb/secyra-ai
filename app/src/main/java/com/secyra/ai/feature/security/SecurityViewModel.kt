package com.secyra.ai.feature.security

import android.content.Context
import android.os.Build
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.secyra.ai.core.permissions.PermissionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SecurityViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val permissionManager: PermissionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SecurityUiState())
    val uiState: StateFlow<SecurityUiState> = _uiState.asStateFlow()

    init {
        loadSecurityInfo()
    }

    private fun loadSecurityInfo() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val securityInfo = SecurityInfo(
                androidVersion = "${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
                securityPatch = Build.VERSION.SECURITY_PATCH,
                screenLockEnabled = isScreenLockEnabled(),
                unknownSourcesEnabled = isUnknownSourcesEnabled(),
                developerOptionsEnabled = isDeveloperOptionsEnabled(),
                adbEnabled = isAdbEnabled(),
                hasDeviceAdmin = hasDeviceAdminApps(),
                vpnConnected = isVpnConnected(),
                accessibilityServices = getAccessibilityServicesCount()
            )

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                securityInfo = securityInfo
            )
        }
    }

    private fun isScreenLockEnabled(): Boolean {
        return try {
            val lockMode = Settings.Secure.getInt(
                context.contentResolver,
                Settings.Secure.LOCK_PATTERN_ENABLED
            )
            lockMode == 1
        } catch (e: Exception) {
            false
        }
    }

    private fun isUnknownSourcesEnabled(): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Settings.Secure.getInt(
                    context.contentResolver,
                    Settings.Secure.INSTALL_NON_MARKET_APPS
                ) == 1
            } else {
                true // On older versions, this is not reliable
            }
        } catch (e: Exception) {
            false
        }
    }

    private fun isDeveloperOptionsEnabled(): Boolean {
        return try {
            Settings.Global.getInt(
                context.contentResolver,
                Settings.Global.DEVELOPMENT_SETTINGS_ENABLED
            ) == 1
        } catch (e: Exception) {
            false
        }
    }

    private fun isAdbEnabled(): Boolean {
        return try {
            Settings.Global.getInt(
                context.contentResolver,
                Settings.Global.ADB_ENABLED
            ) == 1
        } catch (e: Exception) {
            false
        }
    }

    private fun hasDeviceAdminApps(): Boolean {
        return try {
            val devicePolicyManager = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as android.app.admin.DevicePolicyManager
            devicePolicyManager.activeAdmins?.isNotEmpty() == true
        } catch (e: Exception) {
            false
        }
    }

    private fun isVpnConnected(): Boolean {
        return try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
            val network = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(network)
            capabilities != null && capabilities.hasTransport(android.net.NetworkCapabilities.TRANSPORT_VPN)
        } catch (e: Exception) {
            false
        }
    }

    private fun getAccessibilityServicesCount(): Int {
        return try {
            val accessibilityManager = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as android.view.accessibility.AccessibilityManager
            // Use the constant value directly: FEEDBACK_ALL_MASK = -1
            accessibilityManager.getEnabledAccessibilityServiceList(-1)?.size ?: 0
        } catch (e: Exception) {
            0
        }
    }

    fun onSecurityItemClicked(item: SecurityItem) {
        _uiState.value = _uiState.value.copy(selectedItem = item)
    }

    fun clearSelectedItem() {
        _uiState.value = _uiState.value.copy(selectedItem = null)
    }

    fun refreshSecurityInfo() {
        loadSecurityInfo()
    }
}

data class SecurityUiState(
    val isLoading: Boolean = false,
    val securityInfo: SecurityInfo? = null,
    val selectedItem: SecurityItem? = null
)

data class SecurityInfo(
    val androidVersion: String,
    val securityPatch: String,
    val screenLockEnabled: Boolean,
    val unknownSourcesEnabled: Boolean,
    val developerOptionsEnabled: Boolean,
    val adbEnabled: Boolean,
    val hasDeviceAdmin: Boolean,
    val vpnConnected: Boolean,
    val accessibilityServices: Int
)

data class SecurityItem(
    val id: String,
    val title: String,
    val description: String,
    val status: SecurityStatus,
    val recommendation: String
)

enum class SecurityStatus {
    SECURE,
    WARNING,
    RISK,
    UNKNOWN
}

