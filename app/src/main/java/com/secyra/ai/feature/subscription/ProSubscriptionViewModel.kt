package com.secyra.ai.feature.subscription

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProSubscriptionViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(ProSubscriptionUiState())
    val uiState: StateFlow<ProSubscriptionUiState> = _uiState.asStateFlow()

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

    fun purchaseMonthlyIndia() {
        // TODO: Implement when RevenueCat is configured
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            purchaseResult = PurchaseResult.Error("RevenueCat not configured. Cannot process purchase.")
        )
    }

    fun purchaseAnnualIndia() {
        // TODO: Implement when RevenueCat is configured
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            purchaseResult = PurchaseResult.Error("RevenueCat not configured. Cannot process purchase.")
        )
    }

    fun purchaseMonthlyGlobal() {
        // TODO: Implement when RevenueCat is configured
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            purchaseResult = PurchaseResult.Error("RevenueCat not configured. Cannot process purchase.")
        )
    }

    fun purchaseAnnualGlobal() {
        // TODO: Implement when RevenueCat is configured
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            purchaseResult = PurchaseResult.Error("RevenueCat not configured. Cannot process purchase.")
        )
    }

    fun restorePurchases() {
        // TODO: Implement when RevenueCat is configured
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            purchaseResult = PurchaseResult.Error("RevenueCat not configured. Cannot restore purchases.")
        )
    }

    fun clearPurchaseResult() {
        _uiState.value = _uiState.value.copy(purchaseResult = null)
    }
}

data class ProSubscriptionUiState(
    val isProUser: Boolean = false,
    val isLoading: Boolean = false,
    val purchaseResult: PurchaseResult? = null
)

sealed class PurchaseResult {
    object Success : PurchaseResult()
    object Restored : PurchaseResult()
    data class Error(val message: String) : PurchaseResult()
}

