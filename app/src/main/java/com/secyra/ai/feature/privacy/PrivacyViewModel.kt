package com.secyra.ai.feature.privacy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.secyra.ai.core.permissions.PermissionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PrivacyViewModel @Inject constructor(
    private val permissionManager: PermissionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(PrivacyUiState())
    val uiState: StateFlow<PrivacyUiState> = _uiState.asStateFlow()

    init {
        loadPrivacyInfo()
    }

    private fun loadPrivacyInfo() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val privacyInfo = PrivacyInfo(
                cameraPermissionGranted = permissionManager.isPermissionGranted(
                    PermissionManager.Permissions.CAMERA
                ),
                microphonePermissionGranted = permissionManager.isPermissionGranted(
                    PermissionManager.Permissions.RECORD_AUDIO
                ),
                locationPermissionGranted = permissionManager.isPermissionGranted(
                    PermissionManager.Permissions.ACCESS_FINE_LOCATION
                ),
                contactsPermissionGranted = permissionManager.isPermissionGranted(
                    PermissionManager.Permissions.READ_CONTACTS
                ),
                storagePermissionGranted = permissionManager.isPermissionGranted(
                    PermissionManager.Permissions.READ_EXTERNAL_STORAGE
                ),
                historyRetentionEnabled = true, // Default to enabled
                analyticsEnabled = false, // Default to disabled
                crashReportingEnabled = true // Default to enabled
            )

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                privacyInfo = privacyInfo
            )
        }
    }

    fun toggleHistoryRetention(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(
            privacyInfo = _uiState.value.privacyInfo?.copy(
                historyRetentionEnabled = enabled
            )
        )
    }

    fun toggleAnalytics(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(
            privacyInfo = _uiState.value.privacyInfo?.copy(
                analyticsEnabled = enabled
            )
        )
    }

    fun toggleCrashReporting(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(
            privacyInfo = _uiState.value.privacyInfo?.copy(
                crashReportingEnabled = enabled
            )
        )
    }

    fun clearHistory() {
        _uiState.value = _uiState.value.copy(
            shouldClearHistory = true
        )
    }

    fun onHistoryCleared() {
        _uiState.value = _uiState.value.copy(
            shouldClearHistory = false
        )
    }

    fun exportData() {
        _uiState.value = _uiState.value.copy(
            shouldExportData = true
        )
    }

    fun onDataExported() {
        _uiState.value = _uiState.value.copy(
            shouldExportData = false
        )
    }

    fun refreshPrivacyInfo() {
        loadPrivacyInfo()
    }
}

data class PrivacyUiState(
    val isLoading: Boolean = false,
    val privacyInfo: PrivacyInfo? = null,
    val shouldClearHistory: Boolean = false,
    val shouldExportData: Boolean = false
)

data class PrivacyInfo(
    val cameraPermissionGranted: Boolean,
    val microphonePermissionGranted: Boolean,
    val locationPermissionGranted: Boolean,
    val contactsPermissionGranted: Boolean,
    val storagePermissionGranted: Boolean,
    val historyRetentionEnabled: Boolean,
    val analyticsEnabled: Boolean,
    val crashReportingEnabled: Boolean
)

