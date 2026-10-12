package com.secyra.ai.core.threat

/**
 * Abstraction for threat intelligence providers.
 * Allows switching between different providers without changing UI.
 */
interface ThreatIntelligenceProvider {
    val providerName: String
    val isConfigured: Boolean
    
    /**
     * Check if a URL is malicious
     */
    suspend fun checkUrl(url: String): UrlThreatResult
    
    /**
     * Get reputation information for a phone number
     */
    suspend fun checkPhoneNumber(phoneNumber: String): PhoneThreatResult
    
    /**
     * Report a phone number as spam
     */
    suspend fun reportSpam(phoneNumber: String, reason: String): ReportResult
}

/**
 * Result of URL threat check
 */
data class UrlThreatResult(
    val isMalicious: Boolean,
    val threatType: String?,
    val confidence: Float,
    val lastChecked: Long,
    val provider: String,
    val errorMessage: String? = null
)

/**
 * Result of phone number threat check
 */
data class PhoneThreatResult(
    val isSpam: Boolean,
    val spamScore: Float,
    val reportCount: Int,
    val lastReportDate: Long?,
    val categories: List<String>,
    val provider: String,
    val errorMessage: String? = null
)

/**
 * Result of spam report submission
 */
data class ReportResult(
    val success: Boolean,
    val reportId: String?,
    val errorMessage: String? = null
)
