package com.secyra.ai.core.threat

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for coordinating threat intelligence checks.
 * Routes requests to configured providers and aggregates results.
 */
@Singleton
class ThreatIntelligenceService @Inject constructor(
    private val provider: ThreatIntelligenceProvider
) {
    
    /**
     * Check if a URL is malicious
     */
    suspend fun checkUrl(url: String): UrlThreatResult {
        return provider.checkUrl(url)
    }
    
    /**
     * Get reputation information for a phone number
     */
    suspend fun checkPhoneNumber(phoneNumber: String): PhoneThreatResult {
        return provider.checkPhoneNumber(phoneNumber)
    }
    
    /**
     * Report a phone number as spam
     */
    suspend fun reportSpam(phoneNumber: String, reason: String): ReportResult {
        return provider.reportSpam(phoneNumber, reason)
    }
    
    /**
     * Check if the provider is configured
     */
    fun isProviderConfigured(): Boolean {
        return provider.isConfigured
    }
    
    /**
     * Get the provider name
     */
    fun getProviderName(): String {
        return provider.providerName
    }
}
