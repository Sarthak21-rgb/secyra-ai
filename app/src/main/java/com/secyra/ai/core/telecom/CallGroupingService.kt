package com.secyra.ai.core.telecom

import java.util.Locale

/**
 * Service for grouping call log records according to production-quality rules.
 * Implements 23-hour rolling window grouping with phone number normalization.
 */
class CallGroupingService(
    private val config: CallGroupingConfig = CallGroupingConfig(),
    private val enableLogging: Boolean = true
) {

    companion object {
        private const val TAG = "CallGroupingService"
    }

    private fun logD(message: String) {
        if (enableLogging) {
            try {
                android.util.Log.d(TAG, message)
            } catch (e: Exception) {
                // Ignore logging errors in unit tests
            }
        }
    }

    /**
     * Groups raw call records according to the defined rules:
     * 1. Group by normalized phone number
     * 2. Use 23-hour rolling window
     * 3. Preserve groups after gaps > 23 hours
     * 4. Combine incoming and outgoing calls in same group
     * 5. Show latest call (can be incoming or outgoing) as group representative
     * 6. Track total and missed counts separately
     */
    fun groupCalls(rawCalls: List<RawCallRecord>): CallGroupingResult {
        if (rawCalls.isEmpty()) {
            return CallGroupingResult(emptyList(), 0, 0)
        }

        logD("Grouping ${rawCalls.size} calls")

        // Sort calls chronologically (oldest first for proper grouping)
        val sortedCalls = rawCalls.sortedBy { it.timestampMillis }

        // Group by normalized number
        val callsByNumber = sortedCalls.groupBy { it.normalizedNumber }

        val allGroups = mutableListOf<CallGroup>()

        // Process each phone number independently
        for ((number, calls) in callsByNumber) {
            val groupsForNumber = groupCallsByNumber(calls, number)
            allGroups.addAll(groupsForNumber)
        }

        // Sort groups by newest call timestamp (newest first)
        allGroups.sortByDescending { it.newestCall.timestampMillis }

        logD("Created ${allGroups.size} groups from ${rawCalls.size} calls")

        return CallGroupingResult(
            groups = allGroups,
            totalGroups = allGroups.size,
            totalCalls = rawCalls.size
        )
    }

    /**
     * Groups calls for a single phone number.
     * - Combine incoming and outgoing calls in same group
     * - Use 23-hour rolling window for time-based grouping
     * - Show latest call (incoming or outgoing) as group representative
     * - Missed calls are grouped separately with 23-hour window until user calls back
     */
    private fun groupCallsByNumber(calls: List<RawCallRecord>, phoneNumber: String): List<CallGroup> {
        if (calls.isEmpty()) return emptyList()

        val groups = mutableListOf<CallGroup>()

        // Separate missed calls from non-missed calls
        val missedCalls = calls.filter { it.callType == CallType.MISSED }
        val nonMissedCalls = calls.filter { it.callType != CallType.MISSED }

        // Group non-missed calls (incoming + outgoing combined) with 23-hour window
        if (nonMissedCalls.isNotEmpty()) {
            groups.addAll(groupCallsByType(nonMissedCalls))
        }

        // Group missed calls separately with 23-hour window
        // Reset grouping if user called back (outgoing call after missed)
        if (missedCalls.isNotEmpty()) {
            val lastMissedCall = missedCalls.maxByOrNull { it.timestampMillis }
            val hasOutgoingAfterMissed = nonMissedCalls.any { 
                it.callType == CallType.OUTGOING &&
                it.timestampMillis > (lastMissedCall?.timestampMillis ?: 0) 
            }
            
            if (hasOutgoingAfterMissed) {
                // If user called back, only group missed calls before the outgoing call
                val earliestOutgoing = nonMissedCalls
                    .filter { it.callType == CallType.OUTGOING }
                    .minByOrNull { it.timestampMillis }
                val missedBeforeCallback = missedCalls.filter { 
                    it.timestampMillis < (earliestOutgoing?.timestampMillis ?: Long.MAX_VALUE) 
                }
                val missedAfterCallback = missedCalls.filter { 
                    it.timestampMillis >= (earliestOutgoing?.timestampMillis ?: 0) 
                }
                
                if (missedBeforeCallback.isNotEmpty()) {
                    groups.addAll(groupCallsByType(missedBeforeCallback))
                }
                if (missedAfterCallback.isNotEmpty()) {
                    groups.addAll(groupCallsByType(missedAfterCallback))
                }
            } else {
                // No callback yet - group all missed calls with 23-hour window
                groups.addAll(groupCallsByType(missedCalls))
            }
        }

        return groups
    }

    /**
     * Groups calls of the same type using 23-hour rolling window.
     */
    private fun groupCallsByType(calls: List<RawCallRecord>): List<CallGroup> {
        if (calls.isEmpty()) return emptyList()

        val groups = mutableListOf<CallGroup>()
        val currentGroupCalls = mutableListOf<RawCallRecord>()

        // Calls are already sorted chronologically
        for (call in calls) {
            if (currentGroupCalls.isEmpty()) {
                // First call starts a new group
                currentGroupCalls.add(call)
            } else {
                // Check if this call can join the current group
                val newestCallInGroup = currentGroupCalls.maxByOrNull { it.timestampMillis }!!
                val timeGap = call.timestampMillis - newestCallInGroup.timestampMillis

                if (timeGap <= config.groupingWindowMillis) {
                    // Within 23-hour window - add to current group
                    currentGroupCalls.add(call)
                } else {
                    // Gap exceeds 23 hours - finalize current group and start new one
                    if (currentGroupCalls.isNotEmpty()) {
                        groups.add(createGroup(currentGroupCalls))
                    }
                    currentGroupCalls.clear()
                    currentGroupCalls.add(call)
                }
            }
        }

        // Don't forget the last group
        if (currentGroupCalls.isNotEmpty()) {
            groups.add(createGroup(currentGroupCalls))
        }

        return groups
    }

    /**
     * Creates a CallGroup from a list of raw calls.
     */
    private fun createGroup(calls: List<RawCallRecord>): CallGroup {
        val newestCall = calls.maxByOrNull { it.timestampMillis }!!
        val missedCount = calls.count { it.callType == CallType.MISSED }
        val isBlocked = calls.any { it.isBlocked }

        // Use contact info from the newest call (most likely to be current)
        return CallGroup(
            phoneNumber = newestCall.phoneNumber,
            normalizedNumber = newestCall.normalizedNumber,
            contactName = newestCall.contactName,
            contactPhotoUri = newestCall.contactPhotoUri,
            isSavedContact = newestCall.isSavedContact,
            calls = calls.sortedByDescending { it.timestampMillis },
            newestCall = newestCall,
            totalCallCount = calls.size,
            missedCallCount = missedCount,
            isBlocked = isBlocked
        )
    }

    /**
     * Normalizes a phone number for grouping purposes.
     * Strips common formatting characters and converts to a standard format.
     * Note: For production, consider using libphonenumber for full normalization.
     */
    fun normalizePhoneNumber(phoneNumber: String): String {
        // Remove all non-digit characters except + at the start
        var normalized = phoneNumber.replace(Regex("[^+0-9]"), "")

        // If number starts with country code but no +, add it
        if (normalized.isNotEmpty() && !normalized.startsWith("+")) {
            // Keep as is for now - in production, would add country code logic
        }

        return normalized
    }

    /**
     * Converts Android CallLog.Calls type to our CallType enum
     */
    fun convertCallType(androidCallType: Int): CallType {
        return when (androidCallType) {
            android.provider.CallLog.Calls.INCOMING_TYPE -> CallType.INCOMING
            android.provider.CallLog.Calls.OUTGOING_TYPE -> CallType.OUTGOING
            android.provider.CallLog.Calls.MISSED_TYPE -> CallType.MISSED
            android.provider.CallLog.Calls.REJECTED_TYPE -> CallType.REJECTED
            android.provider.CallLog.Calls.BLOCKED_TYPE -> CallType.BLOCKED
            android.provider.CallLog.Calls.VOICEMAIL_TYPE -> CallType.VOICEMAIL
            else -> CallType.UNKNOWN
        }
    }
}
