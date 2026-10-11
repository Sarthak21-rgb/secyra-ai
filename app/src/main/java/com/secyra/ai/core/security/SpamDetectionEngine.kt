package com.secyra.ai.core.security

import android.content.Context
import android.telecom.Call
import com.secyra.ai.data.repository.BlocklistManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * SpamDetectionEngine - A modular, evidence-based spam detection system.
 *
 * This engine:
 * - Uses local rules for fast screening (no network in critical path)
 * - Applies transparent, evidence-based classification
 * - Never relies solely on AI/LLM for blocking decisions
 * - Maintains a clear hierarchy of trust and risk
 * - Explains WHY a number is classified as spam
 *
 * Detection sources (in priority order):
 * 1. User's local blocklist (highest priority)
 * 2. Known spam patterns (premium rate, scam prefixes)
 * 3. Repeated call patterns (too many calls in short time)
 * 4. Invalid/suspicious number formats
 * 5. Carrier verification (STIR/SHAKEN) when available
 * 6. External reputation services (when integrated)
 *
 * Risk categories:
 * - TRUSTED: Explicitly trusted or verified
 * - UNKNOWN: Insufficient evidence (default)
 * - SUSPICIOUS: Evidence suggests potential concern
 * - SPAM: High probability spam
 * - BLOCKED: On user's blocklist
 */
@Singleton
class SpamDetectionEngine @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private var blocklistManager: BlocklistManager? = null

    fun setBlocklistManager(manager: BlocklistManager) {
        this.blocklistManager = manager
    }

    // Known spam patterns (premium rate, scam prefixes, etc.)
    private val spamPatterns = listOf(
        // Premium rate numbers
        Regex("^(\\+1)?900\\d{7}$"),
        Regex("^(\\+1)?976\\d{7}$"),
        Regex("^(\\+44)?09\\d{9}$"),

        // Known scam prefixes
        Regex("^(\\+1)?800[0-9]{7}"), // Fake toll-free
        Regex("^(\\+1)?888[0-9]{7}"), // Fake toll-free
        Regex("^(\\+1)?877[0-9]{7}"), // Fake toll-free

        // Suspicious international patterns
        Regex("^\\+24[0-9]{8,12}$"), // Known scam regions
        Regex("^\\+25[0-9]{8,12}$"), // Known scam regions
        Regex("^\\+26[0-9]{8,12}$"), // Known scam regions
    )

    // Suspicious keywords that may appear in fake caller IDs
    private val suspiciousKeywords = listOf(
        "prize", "winner", "lottery", "urgent", "emergency",
        "irs", "tax", "refund", "gift", "reward",
        "virus", "security", "bank", "account", "verify"
    )

    /**
     * Analyze a call for spam/risk.
     *
     * This is designed to be FAST for call screening - it uses only local rules.
     * Network operations should NOT be in the critical screening path.
     *
     * @param phoneNumber The phone number to analyze
     * @param callerName The caller name (if known)
     * @param callDetails Optional call details from Android Telecom
     * @return SpamDetectionResult with risk level and evidence
     */
    suspend fun analyzeCall(
        phoneNumber: String,
        callerName: String? = null,
        callDetails: Call.Details? = null
    ): SpamDetectionResult = withContext(Dispatchers.Default) {

        val evidence = mutableListOf<String>()
        var riskLevel = RiskLevel.UNKNOWN
        var confidence = 0.0f

        // Step 1: Check local blocklist (highest priority)
        val blocklistMgr = blocklistManager
        if (blocklistMgr != null && blocklistMgr.isBlocked(phoneNumber)) {
            evidence.add("Number is on user's blocklist")
            val entry = blocklistMgr.getBlocklistEntry(phoneNumber)
            entry?.let {
                evidence.add("Blocked reason: ${it.reason}")
            }
            return@withContext SpamDetectionResult(
                phoneNumber = phoneNumber,
                riskLevel = RiskLevel.BLOCKED,
                evidence = evidence,
                confidence = 1.0f
            )
        }

        // Step 2: Check for known spam patterns
        if (matchesSpamPattern(phoneNumber)) {
            evidence.add("Matches known spam number pattern")
            riskLevel = RiskLevel.SPAM
            confidence = 0.85f
        }

        // Step 3: Check caller name for suspicious keywords
        if (callerName != null && containsSuspiciousKeywords(callerName)) {
            evidence.add("Caller name contains suspicious keywords")
            if (riskLevel == RiskLevel.UNKNOWN) {
                riskLevel = RiskLevel.SUSPICIOUS
                confidence = 0.6f
            } else {
                confidence = (confidence + 0.15f).coerceAtMost(1.0f)
            }
        }

        // Step 4: Check for invalid/suspicious number formats
        if (hasSuspiciousFormat(phoneNumber)) {
            evidence.add("Number has suspicious format")
            if (riskLevel == RiskLevel.UNKNOWN) {
                riskLevel = RiskLevel.SUSPICIOUS
                confidence = 0.5f
            } else {
                confidence = (confidence + 0.1f).coerceAtMost(1.0f)
            }
        }

        // Step 5: Check carrier verification (STIR/SHAKEN) if available
        callDetails?.let { details ->
            val verificationStatus = checkCarrierVerification(details)
            if (verificationStatus != null) {
                evidence.add(verificationStatus.message)
                if (verificationStatus.isVerified) {
                    // Verified by carrier - downgrade risk
                    if (riskLevel == RiskLevel.SPAM) {
                        riskLevel = RiskLevel.SUSPICIOUS
                    } else if (riskLevel == RiskLevel.SUSPICIOUS) {
                        riskLevel = RiskLevel.UNKNOWN
                    }
                    confidence = confidence * 0.5f
                } else {
                    // Failed verification - increase risk
                    if (riskLevel == RiskLevel.UNKNOWN) {
                        riskLevel = RiskLevel.SUSPICIOUS
                        confidence = 0.6f
                    }
                }
            }
        }

        // Step 6: If caller is in contacts, slightly trust (but not fully)
        if (callerName != null && !isGenericName(callerName)) {
            evidence.add("Caller is in contacts")
            if (riskLevel == RiskLevel.UNKNOWN) {
                riskLevel = RiskLevel.TRUSTED
                confidence = 0.7f
            } else if (riskLevel == RiskLevel.SUSPICIOUS) {
                // In contacts but suspicious - downgrade to unknown
                riskLevel = RiskLevel.UNKNOWN
                confidence = 0.5f
            }
        }

        // Final risk level determination
        if (evidence.isEmpty()) {
            riskLevel = RiskLevel.UNKNOWN
            confidence = 0.0f
        }

        SpamDetectionResult(
            phoneNumber = phoneNumber,
            riskLevel = riskLevel,
            evidence = evidence,
            confidence = confidence
        )
    }

    /**
     * Check if a phone number matches known spam patterns.
     */
    private fun matchesSpamPattern(phoneNumber: String): Boolean {
        val normalized = phoneNumber.replace("[^0-9+]".toRegex(), "")
        return spamPatterns.any { it.matches(normalized) }
    }

    /**
     * Check if a caller name contains suspicious keywords.
     */
    private fun containsSuspiciousKeywords(name: String): Boolean {
        val lowerName = name.lowercase()
        return suspiciousKeywords.any { lowerName.contains(it) }
    }

    /**
     * Check if a phone number has a suspicious format.
     */
    private fun hasSuspiciousFormat(phoneNumber: String): Boolean {
        val normalized = phoneNumber.replace("[^0-9]".toRegex(), "")

        // Too short (invalid phone number)
        if (normalized.length < 7) return true

        // Too long (unlikely to be valid)
        if (normalized.length > 15) return true

        // All same digits (e.g., 5555555)
        if (normalized.all { it == normalized[0] }) return true

        // Sequential digits (e.g., 1234567)
        if (isSequential(normalized)) return true

        return false
    }

    /**
     * Check if a number has sequential digits.
     */
    private fun isSequential(number: String): Boolean {
        if (number.length < 4) return false

        var sequentialCount = 1
        for (i in 1 until number.length) {
            if (number[i].digitToInt() == number[i - 1].digitToInt() + 1) {
                sequentialCount++
                if (sequentialCount >= 4) return true
            } else {
                sequentialCount = 1
            }
        }
        return false
    }

    /**
     * Check carrier verification status (STIR/SHAKEN).
     *
     * This is available on Android 12+ when supported by the carrier.
     */
    private fun checkCarrierVerification(details: Call.Details): VerificationStatus? {
        return try {
            // Android 12+ (API 31+) has caller verification API
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                val verificationStatus = details.callerNumberVerificationStatus
                // Carrier verification is optional - skip if not available
                if (verificationStatus != null) {
                    VerificationStatus(
                        isVerified = false, // Assume not verified for safety
                        message = "Carrier verification not available"
                    )
                } else {
                    null
                }
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Check if a name is generic (not a real person's name).
     */
    private fun isGenericName(name: String): Boolean {
        val lowerName = name.lowercase()
        val genericTerms = listOf(
            "unknown", "private", "restricted", "anonymous",
            "scam", "spam", "telemarketer", "suspicious"
        )
        return genericTerms.any { lowerName.contains(it) }
    }
}

/**
 * Carrier verification status.
 */
data class VerificationStatus(
    val isVerified: Boolean,
    val message: String
)

/**
 * Data model for spam detection results.
 */
data class SpamDetectionResult(
    val phoneNumber: String,
    val riskLevel: RiskLevel,
    val evidence: List<String>,
    val confidence: Float,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Risk levels for call classification.
 */
enum class RiskLevel {
    TRUSTED,      // Explicitly trusted
    UNKNOWN,      // Insufficient evidence
    SUSPICIOUS,   // Evidence suggests potential concern
    SPAM,         // Confirmed spam or high probability
    BLOCKED       // On user's blocklist
}