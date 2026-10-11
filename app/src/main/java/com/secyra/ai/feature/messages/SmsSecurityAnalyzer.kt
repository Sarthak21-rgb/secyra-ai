package com.secyra.ai.feature.messages

import com.secyra.ai.core.security.SecurityEngine
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Analyzer for SMS security threats.
 * Analyzes incoming messages for phishing, scams, and suspicious patterns.
 */
@Singleton
class SmsSecurityAnalyzer @Inject constructor(
    private val securityEngine: SecurityEngine
) {

    suspend fun analyzeMessage(
        phoneNumber: String,
        messageBody: String,
        timestamp: Long
    ): MessageSecurityResult {
        val riskIndicators = mutableListOf<RiskIndicator>()

        // Check for suspicious URLs
        if (containsSuspiciousUrl(messageBody)) {
            riskIndicators.add(RiskIndicator.SUSPICIOUS_URL)
        }

        // Check for urgency language
        if (containsUrgencyLanguage(messageBody)) {
            riskIndicators.add(RiskIndicator.URGENCY_LANGUAGE)
        }

        // Check for payment requests
        if (containsPaymentRequest(messageBody)) {
            riskIndicators.add(RiskIndicator.PAYMENT_REQUEST)
        }

        // Check for credential requests
        if (containsCredentialRequest(messageBody)) {
            riskIndicators.add(RiskIndicator.CREDENTIAL_REQUEST)
        }

        // Check for known scam patterns
        if (containsScamPattern(messageBody)) {
            riskIndicators.add(RiskIndicator.SCAM_PATTERN)
        }

        // Determine overall risk level
        val riskLevel = when {
            riskIndicators.contains(RiskIndicator.SCAM_PATTERN) -> SecurityLevel.Blocked
            riskIndicators.size >= 3 -> SecurityLevel.Suspicious
            riskIndicators.size >= 2 -> SecurityLevel.Suspicious
            riskIndicators.size == 1 -> SecurityLevel.Unknown
            else -> SecurityLevel.Trusted
        }

        return MessageSecurityResult(
            phoneNumber = phoneNumber,
            messageBody = messageBody,
            timestamp = timestamp,
            riskLevel = riskLevel,
            riskIndicators = riskIndicators
        )
    }

    private fun containsSuspiciousUrl(text: String): Boolean {
        val suspiciousDomains = listOf(
            "bit.ly", "tinyurl", "goo.gl", "t.co",
            "free", "gift", "claim", "win", "prize"
        )
        return suspiciousDomains.any { text.lowercase().contains(it) }
    }

    private fun containsUrgencyLanguage(text: String): Boolean {
        val urgencyWords = listOf(
            "urgent", "immediately", "act now", "expires soon",
            "limited time", "hurry", "last chance", "don't wait"
        )
        return urgencyWords.any { text.lowercase().contains(it) }
    }

    private fun containsPaymentRequest(text: String): Boolean {
        val paymentWords = listOf(
            "pay", "payment", "transfer", "bank", "account",
            "debit", "credit", "card", "otp", "pin"
        )
        return paymentWords.any { text.lowercase().contains(it) }
    }

    private fun containsCredentialRequest(text: String): Boolean {
        val credentialWords = listOf(
            "password", "username", "login", "verify", "confirm",
            "update", "security", "account access"
        )
        return credentialWords.any { text.lowercase().contains(it) }
    }

    private fun containsScamPattern(text: String): Boolean {
        val scamPatterns = listOf(
            "you have won", "congratulations", "lottery", "prize",
            "inheritance", "government grant", "free money"
        )
        return scamPatterns.any { text.lowercase().contains(it) }
    }
}

data class MessageSecurityResult(
    val phoneNumber: String,
    val messageBody: String,
    val timestamp: Long,
    val riskLevel: SecurityLevel,
    val riskIndicators: List<RiskIndicator>
)

enum class RiskIndicator {
    SUSPICIOUS_URL,
    URGENCY_LANGUAGE,
    PAYMENT_REQUEST,
    CREDENTIAL_REQUEST,
    SCAM_PATTERN
}
