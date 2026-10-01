package com.secyra.ai.feature.scan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScanViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(ScanUiState())
    val uiState: StateFlow<ScanUiState> = _uiState.asStateFlow()

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

    fun selectScanType(scanType: ScanType) {
        _uiState.value = _uiState.value.copy(selectedScanType = scanType)
    }

    fun onNavigationHandled() {
        _uiState.value = _uiState.value.copy(selectedScanType = null)
    }
}

data class ScanUiState(
    val isProUser: Boolean = false,
    val selectedScanType: ScanType? = null
)

enum class ScanType {
    LINK,
    QR,
    MESSAGE,
    SCREENSHOT,
    FILE,
    PHONE
}
