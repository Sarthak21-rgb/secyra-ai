package com.secyra.ai.core.security

import com.secyra.ai.domain.model.RiskLevel
import javax.inject.Inject
import javax.inject.Singleton

/**
 * SecurityEngine provides deterministic security analysis.
 * This layer should NOT rely on AI - it should use:
 * - Known patterns
 * - Security rules
 * - Threat intelligence
 * - Local validation
 * 
 * AI can be used later to EXPLAIN the findings from this engine.
 */
@Singleton
class SecurityEngine @Inject constructor() {

    data class SecurityAnalysis(
        val riskLevel: RiskLevel,
        val evidence: List<String>,
        val confidence: Float, // 0.0 to 1.0
        val reason: String
    )

    /**
     * Analyze a URL for security risks
     */
    fun analyzeUrl(url: String): SecurityAnalysis {
        val evidence = mutableListOf<String>()
        var riskLevel = RiskLevel.UNKNOWN
        var confidence = 0.5f

        // Basic URL validation
        if (!isValidUrl(url)) {
            return SecurityAnalysis(
                riskLevel = RiskLevel.HIGH,
                evidence = listOf("Invalid URL format"),
                confidence = 1.0f,
                reason = "URL format is invalid"
            )
        }

        // Check for suspicious patterns
        if (hasSuspiciousDomain(url)) {
            evidence.add("Suspicious domain detected")
            riskLevel = RiskLevel.HIGH
            confidence = 0.8f
        }

        // Check for typosquatting
        if (isTyposquatting(url)) {
            evidence.add("Possible typosquatting attempt")
            riskLevel = if (riskLevel == RiskLevel.HIGH) RiskLevel.CRITICAL else RiskLevel.HIGH
            confidence = 0.7f
        }

        // Check for HTTP instead of HTTPS
        if (url.startsWith("http://")) {
            evidence.add("Uses insecure HTTP protocol")
            riskLevel = if (riskLevel == RiskLevel.UNKNOWN) RiskLevel.MODERATE else riskLevel
            confidence = 0.9f
        }

        return SecurityAnalysis(
            riskLevel = riskLevel,
            evidence = evidence,
            confidence = confidence,
            reason = if (evidence.isEmpty()) "No obvious security issues detected" else "Security concerns identified"
        )
    }

    /**
     * Analyze a phone number for security risks
     */
    fun analyzePhoneNumber(phoneNumber: String): SecurityAnalysis {
        val evidence = mutableListOf<String>()
        var riskLevel = RiskLevel.UNKNOWN
        var confidence = 0.5f

        // Basic phone number validation
        if (!isValidPhoneNumber(phoneNumber)) {
            return SecurityAnalysis(
                riskLevel = RiskLevel.MODERATE,
                evidence = listOf("Invalid phone number format"),
                confidence = 0.8f,
                reason = "Phone number format is invalid"
            )
        }

        // Check for international number
        if (isInternationalNumber(phoneNumber)) {
            evidence.add("International number")
            confidence = 0.6f
        }

        // Check for premium rate patterns
        if (isPremiumRateNumber(phoneNumber)) {
            evidence.add("Premium rate number pattern detected")
            riskLevel = RiskLevel.HIGH
            confidence = 0.8f
        }

        return SecurityAnalysis(
            riskLevel = riskLevel,
            evidence = evidence,
            confidence = confidence,
            reason = if (evidence.isEmpty()) "No obvious security issues detected" else "Security concerns identified"
        )
    }

    /**
     * Analyze a message for security risks
     */
    fun analyzeMessage(message: String): SecurityAnalysis {
        val evidence = mutableListOf<String>()
        var riskLevel = RiskLevel.UNKNOWN
        var confidence = 0.5f

        // Check for urgency indicators
        if (hasUrgencyIndicators(message)) {
            evidence.add("Contains urgency indicators")
            riskLevel = RiskLevel.MODERATE
            confidence = 0.7f
        }

        // Check for suspicious links
        if (containsSuspiciousLinks(message)) {
            evidence.add("Contains suspicious links")
            riskLevel = RiskLevel.HIGH
            confidence = 0.8f
        }

        // Check for payment requests
        if (containsPaymentRequest(message)) {
            evidence.add("Contains payment request")
            riskLevel = RiskLevel.HIGH
            confidence = 0.75f
        }

        // Check for phishing patterns
        if (hasPhishingPatterns(message)) {
            evidence.add("Contains phishing patterns")
            riskLevel = RiskLevel.HIGH
            confidence = 0.7f
        }

        return SecurityAnalysis(
            riskLevel = riskLevel,
            evidence = evidence,
            confidence = confidence,
            reason = if (evidence.isEmpty()) "No obvious security issues detected" else "Security concerns identified"
        )
    }

    // Helper methods for security checks

    private fun isValidUrl(url: String): Boolean {
        // Basic URL validation
        return url.matches(Regex("^(https?://)?([\\da-z\\.-]+)\\.([a-z\\.]{2,6})([/\\w \\.-]*)*/?$"))
    }

    private fun hasSuspiciousDomain(url: String): Boolean {
        // Check against known suspicious domain patterns
        val suspiciousPatterns = listOf(
            ".*\\.tk$",
            ".*\\.ml$",
            ".*\\.ga$",
            ".*\\.cf$",
            ".*\\.xyz$",
            ".*[0-9]{3,}.*" // Domains with many numbers
        )
        return suspiciousPatterns.any { url.matches(Regex(it, RegexOption.IGNORE_CASE)) }
    }

    private fun isTyposquatting(url: String): Boolean {
        // Basic typosquatting detection
        // This is a simplified check - production should be more sophisticated
        val commonBrands = listOf("google", "facebook", "amazon", "apple", "microsoft", "netflix")
        val domain = extractDomain(url).lowercase()
        
        return commonBrands.any { brand ->
            domain.contains(brand) && domain != brand && !domain.startsWith("$brand.")
        }
    }

    private fun isValidPhoneNumber(phoneNumber: String): Boolean {
        // Basic phone number validation
        return phoneNumber.matches(Regex("^\\+?[0-9]{10,15}$"))
    }

    private fun isInternationalNumber(phoneNumber: String): Boolean {
        return phoneNumber.startsWith("+")
    }

    private fun isPremiumRateNumber(phoneNumber: String): Boolean {
        // Check for premium rate number patterns
        val premiumPatterns = listOf(
            "^\\+1[0-9]{3}900", // US premium
            "^\\+44[0-9]{3}90", // UK premium
            "^\\+91[0-9]{3}90"  // India premium
        )
        return premiumPatterns.any { phoneNumber.matches(Regex(it)) }
    }

    private fun hasUrgencyIndicators(message: String): Boolean {
        val urgencyWords = listOf(
            "urgent", "immediately", "act now", "limited time", "expire", 
            "hurry", "now", "today", "before", "deadline"
        )
        val lowerMessage = message.lowercase()
        return urgencyWords.any { lowerMessage.contains(it) }
    }

    private fun containsSuspiciousLinks(message: String): Boolean {
        // Extract URLs from message
        val urlPattern = Regex("(https?://)?([\\da-z\\.-]+)\\.([a-z\\.]{2,6})([/\\w \\.-]*)*/?")
        val urls = urlPattern.findAll(message).map { it.value }.toList()
        
        return urls.any { hasSuspiciousDomain(it) }
    }

    private fun containsPaymentRequest(message: String): Boolean {
        val paymentWords = listOf(
            "payment", "pay", "transfer", "money", "bank", "account",
            "card", "credit", "debit", "invoice", "bill"
        )
        val lowerMessage = message.lowercase()
        return paymentWords.any { lowerMessage.contains(it) }
    }

    private fun hasPhishingPatterns(message: String): Boolean {
        val phishingPatterns = listOf(
            "verify your account", "confirm your identity", "update your information",
            "suspended account", "unusual activity", "security alert",
            "click here", "login immediately", "password expired"
        )
        val lowerMessage = message.lowercase()
        return phishingPatterns.any { lowerMessage.contains(it) }
    }

    private fun extractDomain(url: String): String {
        val domainPattern = Regex("https?://([\\da-z\\.-]+)\\.([a-z\\.]{2,6})")
        val match = domainPattern.find(url)
        return match?.groupValues?.get(1) ?: ""
    }
}
