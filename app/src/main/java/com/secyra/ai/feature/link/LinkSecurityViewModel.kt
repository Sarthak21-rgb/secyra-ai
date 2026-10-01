package com.secyra.ai.feature.link

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.secyra.ai.core.security.SecurityEngine
import com.secyra.ai.domain.model.RiskLevel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LinkSecurityViewModel @Inject constructor(
    private val securityEngine: SecurityEngine
) : ViewModel() {

    private val _uiState = MutableStateFlow(LinkSecurityUiState())
    val uiState: StateFlow<LinkSecurityUiState> = _uiState.asStateFlow()

    fun onUrlChange(url: String) {
        _uiState.value = _uiState.value.copy(url = url, analysisResult = null)
    }

    fun analyzeUrl() {
        val url = _uiState.value.url.trim()
        if (url.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                error = "Please enter a URL to analyze"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null,
                analysisResult = null
            )

            try {
                val result = securityEngine.analyzeUrl(url)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    analysisResult = LinkAnalysisResult(
                        url = url,
                        riskLevel = result.riskLevel,
                        title = getRiskTitle(result.riskLevel),
                        explanation = result.reason,
                        evidence = result.evidence,
                        confidence = result.confidence,
                        recommendation = getRecommendation(result.riskLevel),
                        timestamp = System.currentTimeMillis()
                    )
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Unable to analyze URL: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearResult() {
        _uiState.value = _uiState.value.copy(
            url = "",
            analysisResult = null,
            error = null
        )
    }

    private fun getRiskTitle(riskLevel: RiskLevel): String {
        return when (riskLevel) {
            RiskLevel.LOW -> "Low Risk"
            RiskLevel.MODERATE -> "Moderate Risk"
            RiskLevel.HIGH -> "High Risk"
            RiskLevel.CRITICAL -> "Critical Risk"
            RiskLevel.UNKNOWN -> "Unknown Risk"
        }
    }

    private fun getRecommendation(riskLevel: RiskLevel): String {
        return when (riskLevel) {
            RiskLevel.LOW -> "This URL appears safe based on our analysis. You can proceed with caution."
            RiskLevel.MODERATE -> "Exercise caution. Some concerns were detected. Verify the source before proceeding."
            RiskLevel.HIGH -> "This URL shows multiple concerning indicators. We recommend against proceeding."
            RiskLevel.CRITICAL -> "This URL is highly suspicious. Do not proceed."
            RiskLevel.UNKNOWN -> "Unable to determine risk. Proceed with extreme caution and verify the source."
        }
    }
}

data class LinkSecurityUiState(
    val url: String = "",
    val isLoading: Boolean = false,
    val analysisResult: LinkAnalysisResult? = null,
    val error: String? = null
)

data class LinkAnalysisResult(
    val url: String,
    val riskLevel: RiskLevel,
    val title: String,
    val explanation: String,
    val evidence: List<String>,
    val confidence: Float,
    val recommendation: String,
    val timestamp: Long
)

