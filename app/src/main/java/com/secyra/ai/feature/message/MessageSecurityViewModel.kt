package com.secyra.ai.feature.message

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
class MessageSecurityViewModel @Inject constructor(
    private val securityEngine: SecurityEngine
) : ViewModel() {

    private val _uiState = MutableStateFlow(MessageSecurityUiState())
    val uiState: StateFlow<MessageSecurityUiState> = _uiState.asStateFlow()

    fun onMessageChange(message: String) {
        _uiState.value = _uiState.value.copy(message = message, analysisResult = null)
    }

    fun analyzeMessage() {
        val message = _uiState.value.message.trim()
        if (message.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                error = "Please enter a message to analyze"
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
                val result = securityEngine.analyzeMessage(message)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    analysisResult = MessageAnalysisResult(
                        message = message,
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
                    error = "Unable to analyze message: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearResult() {
        _uiState.value = _uiState.value.copy(
            message = "",
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
            RiskLevel.LOW -> "This message appears safe based on our analysis. You can proceed with caution."
            RiskLevel.MODERATE -> "Exercise caution. Some concerning patterns were detected. Verify the sender's identity."
            RiskLevel.HIGH -> "This message shows multiple scam indicators. We recommend against responding or taking action."
            RiskLevel.CRITICAL -> "This message is highly suspicious. Do not respond, click links, or share personal information."
            RiskLevel.UNKNOWN -> "Unable to determine risk. Proceed with extreme caution and verify the source."
        }
    }
}

data class MessageSecurityUiState(
    val message: String = "",
    val isLoading: Boolean = false,
    val analysisResult: MessageAnalysisResult? = null,
    val error: String? = null
)

data class MessageAnalysisResult(
    val message: String,
    val riskLevel: RiskLevel,
    val title: String,
    val explanation: String,
    val evidence: List<String>,
    val confidence: Float,
    val recommendation: String,
    val timestamp: Long
)

