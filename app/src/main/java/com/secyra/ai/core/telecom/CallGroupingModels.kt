package com.secyra.ai.core.telecom

/**
 * Raw call record from Android CallLog.Calls
 * This represents a single call from the call log before grouping.
 */
data class RawCallRecord(
    val id: Long,
    val phoneNumber: String,
    val normalizedNumber: String,
    val contactName: String?,
    val contactPhotoUri: String?,
    val isSavedContact: Boolean,
    val timestampMillis: Long,
    val callType: CallType,
    val durationSeconds: Int,
    val isBlocked: Boolean
)

/**
 * Call type as defined by Android CallLog.Calls
 */
enum class CallType {
    INCOMING,
    OUTGOING,
    MISSED,
    REJECTED,
    BLOCKED,
    VOICEMAIL,
    UNKNOWN
}

/**
 * A group of calls that belong together based on:
 * - Same normalized phone number
 * - Within 23-hour rolling window
 * - Same resolved contact (if available)
 * - Combines incoming and outgoing calls
 */
data class CallGroup(
    val phoneNumber: String,
    val normalizedNumber: String,
    val contactName: String?,
    val contactPhotoUri: String?,
    val isSavedContact: Boolean,
    val calls: List<RawCallRecord>,
    val newestCall: RawCallRecord,
    val totalCallCount: Int,
    val missedCallCount: Int,
    val isBlocked: Boolean
) {
    val hasMultipleCalls: Boolean
        get() = totalCallCount > 1

    val hasMissedCalls: Boolean
        get() = missedCallCount > 0
}

/**
 * Result of call grouping operation
 */
data class CallGroupingResult(
    val groups: List<CallGroup>,
    val totalGroups: Int,
    val totalCalls: Int
)

/**
 * Configuration for call grouping
 */
data class CallGroupingConfig(
    val groupingWindowHours: Int = 23
) {
    val groupingWindowMillis: Long
        get() = groupingWindowHours * 60 * 60 * 1000L
}
