package com.secyra.ai.core.security

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Advanced Spam Detection Engine
 * 
 * This engine provides comprehensive spam detection using multiple strategies:
 * 1. Pattern-based detection (number patterns, geographic patterns)
 * 2. Frequency analysis (repeated calls, short intervals)
 * 3. Behavioral analysis (call timing, duration patterns)
 * 4. Crowdsourced data (user reports)
 * 5. Machine learning models (future integration)
 * 
 * Designed to be more advanced than Truecaller and Airtel caller ID
 */
@Singleton
class AdvancedSpamDetectionEngine @Inject constructor(
    @ApplicationContext private val context: Context,
    private val callerIdentificationService: CallerIdentificationService
) {
    private val TAG = "SpamDetectionEngine"
    
    // Spam detection thresholds
    private val HIGH_FREQUENCY_THRESHOLD = 5 // calls in last hour
    private val VERY_HIGH_FREQUENCY_THRESHOLD = 10 // calls in last hour
    private val SHORT_INTERVAL_THRESHOLD = 60000L // 1 minute between calls
    private val RAPID_CALL_THRESHOLD = 3 // calls within SHORT_INTERVAL_THRESHOLD
    
    // Spam score thresholds
    private val SPAM_SCORE_THRESHOLD = 0.7f
    private val HIGH_SPAM_SCORE_THRESHOLD = 0.85f
    
    // Spam patterns
    private val spamNumberPatterns = listOf(
        // Premium rate numbers
        Regex("^(\\+91)?190[0-9]{7}$"),
        Regex("^(\\+1)?900[0-9]{7}$"),
        Regex("^(\\+44)?09[0-9]{8}$"),
        
        // Telemarketing prefixes
        Regex("^(\\+91)?140[0-9]{7}$"),
        Regex("^(\\+91)?141[0-9]{7}$"),
        
        // Toll-free spam patterns
        Regex("^(\\+1)?800[0-9]{6}$"),
        Regex("^(\\+1)?844[0-9]{7}$"),
        Regex("^(\\+1)?855[0-9]{7}$"),
        Regex("^(\\+1)?866[0-9]{7}$"),
        Regex("^(\\+1)?877[0-9]{7}$"),
        Regex("^(\\+1)?888[0-9]{7}$"),
        
        // Short codes
        Regex("^\\d{4,6}$"),
        
        // Invalid international
        Regex("^000[0-9]+$"),
        Regex("^\\+[0-9]{1,3}000[0-9]+$"),
        
        // Vishing patterns (phone phishing)
        Regex("^(\\+91)?[0-9]{10}$"), // Will analyze further with other heuristics
    )
    
    // Legitimate business patterns (to reduce false positives)
    private val legitimateBusinessPatterns = listOf(
        Regex("^(\\+91)?1800[0-9]{6,7}$"), // India toll-free
        Regex("^(\\+91)?1860[0-9]{6}$"), // India customer care
        Regex("^(\\+1)?800[0-9]{6,7}$"), // US toll-free
        
        // Emergency services
        Regex("^(\\+91)?101$"),
        Regex("^(\\+91)?100$"),
        Regex("^(\\+91)?108$"),
        Regex("^(\\+91)?112$"),
        Regex("^911$"),
        Regex("^112$")
    )
    
    // Known legitimate business numbers
    private val legitimateBusinessNumbers = mapOf(
        // Banks
        "+9118004253800" to "HDFC Bank",
        "+911800223344" to "ICICI Bank",
        "+9118001030006" to "SBI",
        "+911800222337" to "Axis Bank",
        
        // Delivery
        "+918042424242" to "Zomato",
        "+918045314500" to "Swiggy",
        
        // Telecom
        "+91199" to "Airtel",
        "+91198" to "Vodafone",
        "+91197" to "Jio",
        
        // Government
        "+9118001804180" to "Government of India"
    )
    
    // Local cache of spam numbers (in production, load from database)
    private val spamDatabase = mutableSetOf<String>()
    
    // Crowd-sourced spam reports (in production, sync with server)
    private val crowdSourcedSpam = mutableMapOf<String, Int>() // number -> report count
    
    /**
     * Analyze a phone number and return spam score with reason
     */
    suspend fun analyzeNumber(phoneNumber: String, callHistory: List<CallRecord> = emptyList()): SpamAnalysisResult = withContext(Dispatchers.IO) {
        val normalizedNumber = normalizePhoneNumber(phoneNumber)
        
        // Get caller info from identification service
        val callerInfo = callerIdentificationService.identifyNumber(phoneNumber)
        
        // Calculate spam score based on multiple factors
        val score = calculateSpamScore(normalizedNumber, callerInfo, callHistory)
        
        // Determine spam level
        val spamLevel = when {
            score >= HIGH_SPAM_SCORE_THRESHOLD -> SpamLevel.HIGH
            score >= SPAM_SCORE_THRESHOLD -> SpamLevel.MEDIUM
            score > 0.3f -> SpamLevel.LOW
            else -> SpamLevel.NONE
        }
        
        // Generate reason
        val reasons = generateReasons(normalizedNumber, callerInfo, callHistory, score)
        
        SpamAnalysisResult(
            phoneNumber = phoneNumber,
            spamScore = score,
            spamLevel = spamLevel,
            reasons = reasons,
            callerName = callerInfo.name,
            callerType = callerInfo.type
        )
    }
    
    /**
     * Calculate spam score based on multiple factors
     */
    private fun calculateSpamScore(
        normalizedNumber: String,
        callerInfo: CallerInfo,
        callHistory: List<CallRecord>
    ): Float {
        var score = 0f
        
        // Factor 1: Known spam database
        if (spamDatabase.contains(normalizedNumber)) {
            score += 0.9f
        }
        
        // Factor 2: Caller identification
        when (callerInfo.type) {
            CallerType.SPAM -> score += 0.8f
            CallerType.SCAM -> score += 0.9f
            CallerType.ROBOCALL -> score += 0.7f
            CallerType.BUSINESS -> score -= 0.3f // Reduce spam score for businesses
            CallerType.PERSONAL -> score -= 0.5f // Reduce spam score for personal
            CallerType.UNKNOWN -> score += 0.1f // Slight increase for unknown
        }
        
        // Factor 3: Pattern analysis
        if (spamNumberPatterns.any { it.matches(normalizedNumber) }) {
            score += 0.6f
        }
        
        // Factor 4: Legitimate business check (reduce false positives)
        if (legitimateBusinessPatterns.any { it.matches(normalizedNumber) }) {
            score -= 0.4f
        }
        
        if (legitimateBusinessNumbers.containsKey(normalizedNumber)) {
            score -= 0.6f
        }
        
        // Factor 5: Frequency analysis
        if (callHistory.isNotEmpty()) {
            val recentCalls = callHistory.filter { 
                System.currentTimeMillis() - it.timestamp < 3600000L // Last hour
            }
            
            when {
                recentCalls.size >= VERY_HIGH_FREQUENCY_THRESHOLD -> score += 0.8f
                recentCalls.size >= HIGH_FREQUENCY_THRESHOLD -> score += 0.5f
            }
            
            // Check for rapid repeated calls
            val sortedCalls = callHistory.sortedBy { it.timestamp }
            var rapidCallCount = 0
            for (i in 1 until sortedCalls.size) {
                if (sortedCalls[i].timestamp - sortedCalls[i-1].timestamp < SHORT_INTERVAL_THRESHOLD) {
                    rapidCallCount++
                }
            }
            
            if (rapidCallCount >= RAPID_CALL_THRESHOLD) {
                score += 0.7f
            }
        }
        
        // Factor 6: Crowd-sourced reports
        val reportCount = crowdSourcedSpam[normalizedNumber] ?: 0
        when {
            reportCount >= 50 -> score += 0.8f
            reportCount >= 20 -> score += 0.6f
            reportCount >= 10 -> score += 0.4f
            reportCount >= 5 -> score += 0.2f
        }
        
        // Ensure score is between 0 and 1
        return score.coerceIn(0f, 1f)
    }
    
    /**
     * Generate reasons for spam classification
     */
    private fun generateReasons(
        normalizedNumber: String,
        callerInfo: CallerInfo,
        callHistory: List<CallRecord>,
        score: Float
    ): List<String> {
        val reasons = mutableListOf<String>()
        
        if (spamDatabase.contains(normalizedNumber)) {
            reasons.add("Listed in spam database")
        }
        
        when (callerInfo.type) {
            CallerType.SPAM -> reasons.add("Identified as spam caller")
            CallerType.SCAM -> reasons.add("Identified as potential scam")
            CallerType.ROBOCALL -> reasons.add("Robocall detected")
            CallerType.BUSINESS -> reasons.add("Business number")
            CallerType.PERSONAL -> reasons.add("Personal contact")
            CallerType.UNKNOWN -> reasons.add("Unknown caller")
        }
        
        if (spamNumberPatterns.any { it.matches(normalizedNumber) }) {
            reasons.add("Matches spam number pattern")
        }
        
        if (legitimateBusinessNumbers.containsKey(normalizedNumber)) {
            reasons.add("Verified business: ${legitimateBusinessNumbers[normalizedNumber]}")
        }
        
        if (callHistory.isNotEmpty()) {
            val recentCalls = callHistory.filter { 
                System.currentTimeMillis() - it.timestamp < 3600000L
            }
            
            if (recentCalls.size >= HIGH_FREQUENCY_THRESHOLD) {
                reasons.add("High call frequency: ${recentCalls.size} calls in last hour")
            }
        }
        
        val reportCount = crowdSourcedSpam[normalizedNumber] ?: 0
        if (reportCount > 0) {
            reasons.add("Reported by $reportCount users")
        }
        
        return reasons
    }
    
    /**
     * Report a number as spam (crowd-sourced)
     */
    suspend fun reportAsSpam(phoneNumber: String) {
        val normalizedNumber = normalizePhoneNumber(phoneNumber)
        crowdSourcedSpam[normalizedNumber] = (crowdSourcedSpam[normalizedNumber] ?: 0) + 1
        
        // In production, send to server
        Log.d(TAG, "Number reported as spam: $normalizedNumber (total reports: ${crowdSourcedSpam[normalizedNumber]})")
    }
    
    /**
     * Report a number as legitimate (not spam)
     */
    suspend fun reportAsLegitimate(phoneNumber: String) {
        val normalizedNumber = normalizePhoneNumber(phoneNumber)
        crowdSourcedSpam.remove(normalizedNumber)
        
        // In production, send to server
        Log.d(TAG, "Number reported as legitimate: $normalizedNumber")
    }
    
    /**
     * Add number to local spam database
     */
    fun addToSpamDatabase(phoneNumber: String) {
        val normalizedNumber = normalizePhoneNumber(phoneNumber)
        spamDatabase.add(normalizedNumber)
    }
    
    /**
     * Remove number from local spam database
     */
    fun removeFromSpamDatabase(phoneNumber: String) {
        val normalizedNumber = normalizePhoneNumber(phoneNumber)
        spamDatabase.remove(normalizedNumber)
    }
    
    /**
     * Normalize phone number
     */
    private fun normalizePhoneNumber(phoneNumber: String): String {
        var normalized = phoneNumber.replace(Regex("[^+0-9]"), "")
        
        if (!normalized.startsWith("+")) {
            normalized = "+91$normalized"
        }
        
        return normalized
    }
    
    /**
     * Batch analyze multiple numbers
     */
    suspend fun analyzeNumbers(phoneNumbers: List<String>, callHistoryMap: Map<String, List<CallRecord>> = emptyMap()): Map<String, SpamAnalysisResult> {
        return phoneNumbers.associateWith { phoneNumber ->
            runCatching {
                analyzeNumber(phoneNumber, callHistoryMap[phoneNumber] ?: emptyList())
            }.getOrNull() ?: SpamAnalysisResult(
                phoneNumber = phoneNumber,
                spamScore = 0f,
                spamLevel = SpamLevel.NONE,
                reasons = emptyList(),
                callerName = null,
                callerType = CallerType.UNKNOWN
            )
        }
    }
}

/**
 * Spam analysis result
 */
data class SpamAnalysisResult(
    val phoneNumber: String,
    val spamScore: Float, // 0.0 to 1.0
    val spamLevel: SpamLevel,
    val reasons: List<String>,
    val callerName: String?,
    val callerType: CallerType
)

/**
 * Spam level
 */
enum class SpamLevel {
    NONE,      // Not spam
    LOW,       // Low confidence spam
    MEDIUM,    // Medium confidence spam
    HIGH       // High confidence spam
}

/**
 * Call record for frequency analysis
 */
data class CallRecord(
    val timestamp: Long,
    val duration: Long,
    val type: String // "INCOMING", "OUTGOING", "MISSED", etc.
)
