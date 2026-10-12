package com.secyra.ai.core.telecom

import java.util.Locale

/**
 * Service for grouping call log records according to production-quality rules.
 * Implements 7-hour rolling window grouping with phone number normalization.
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
     * 2. Use 7-hour rolling window
     * 3. Preserve groups after gaps > 7 hours
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
     * - For non-missed calls: Group by 7-hour window, show only latest call (no count)
     * - For missed calls: Group by 7-hour window until user calls back, show count
     * - Different 7-hour windows create separate rows
     */
    private fun groupCallsByNumber(calls: List<RawCallRecord>, phoneNumber: String): List<CallGroup> {
        if (calls.isEmpty()) return emptyList()

        val groups = mutableListOf<CallGroup>()

        // Separate missed calls from non-missed calls
        val missedCalls = calls.filter { it.callType == CallType.MISSED }
        val nonMissedCalls = calls.filter { it.callType != CallType.MISSED }

        // Group non-missed calls (incoming + outgoing combined) with 7-hour window
        // Each group shows only the latest call, not a count
        if (nonMissedCalls.isNotEmpty()) {
            groups.addAll(groupNonMissedCallsByWindow(nonMissedCalls))
        }

        // Group missed calls separately with 7-hour window
        // Shows count of missed calls until user calls back
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
                // No callback yet - group all missed calls with 7-hour window
                groups.addAll(groupCallsByType(missedCalls))
            }
        }

        return groups
    }

    /**
     * Groups non-missed calls by 7-hour window.
     * Each group contains only the latest call (no count).
     */
    private fun groupNonMissedCallsByWindow(calls: List<RawCallRecord>): List<CallGroup> {
        if (calls.isEmpty()) return emptyList()

        val groups = mutableListOf<CallGroup>()
        val currentWindowCalls = mutableListOf<RawCallRecord>()

        // Sort chronologically
        val sortedCalls = calls.sortedBy { it.timestampMillis }

        for (call in sortedCalls) {
            if (currentWindowCalls.isEmpty()) {
                // First call starts a new window
                currentWindowCalls.add(call)
            } else {
                // Check if this call is within 7-hour window
                val newestCallInWindow = currentWindowCalls.maxByOrNull { it.timestampMillis }!!
                val timeGap = call.timestampMillis - newestCallInWindow.timestampMillis

                if (timeGap <= config.groupingWindowMillis) {
                    // Within 7-hour window - add to current window
                    currentWindowCalls.add(call)
                } else {
                    // Gap exceeds 7 hours - finalize current window with latest call only
                    val latestCall = currentWindowCalls.maxByOrNull { it.timestampMillis }!!
                    groups.add(createSingleCallGroup(latestCall))
                    currentWindowCalls.clear()
                    currentWindowCalls.add(call)
                }
            }
        }

        // Don't forget the last window
        if (currentWindowCalls.isNotEmpty()) {
            val latestCall = currentWindowCalls.maxByOrNull { it.timestampMillis }!!
            groups.add(createSingleCallGroup(latestCall))
        }

        return groups
    }

    /**
     * Creates a group for a single call (for non-missed latest calls).
     */
    private fun createSingleCallGroup(call: RawCallRecord): CallGroup {
        return CallGroup(
            phoneNumber = call.phoneNumber,
            normalizedNumber = call.normalizedNumber,
            contactName = call.contactName,
            contactPhotoUri = call.contactPhotoUri,
            isSavedContact = call.isSavedContact,
            totalCallCount = 1,
            missedCallCount = if (call.callType == CallType.MISSED) 1 else 0,
            newestCall = call,
            calls = listOf(call),
            isBlocked = call.isBlocked
        )
    }

    /**
     * Groups calls of the same type using 7-hour rolling window.
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
                    // Within 7-hour window - add to current group
                    currentGroupCalls.add(call)
                } else {
                    // Gap exceeds 7 hours - finalize current group and start new one
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
