package com.secyra.ai.core.telecom

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for CallGroupingService covering all 20 required situations.
 */
class CallGroupingServiceTest {

    private lateinit var groupingService: CallGroupingService
    private lateinit var config: CallGroupingConfig

    @Before
    fun setup() {
        config = CallGroupingConfig(groupingWindowHours = 7)
        groupingService = CallGroupingService(config, enableLogging = false)
    }

    // Helper to create a mock call record
    private fun createCall(
        id: Long,
        phoneNumber: String,
        contactName: String? = null,
        timestampMillis: Long,
        callType: CallType,
        isBlocked: Boolean = false
    ): RawCallRecord {
        return RawCallRecord(
            id = id,
            phoneNumber = phoneNumber,
            normalizedNumber = groupingService.normalizePhoneNumber(phoneNumber),
            contactName = contactName,
            contactPhotoUri = null,
            isSavedContact = contactName != null,
            timestampMillis = timestampMillis,
            callType = callType,
            durationSeconds = 0,
            isBlocked = isBlocked
        )
    }

    /**
     * Situation 1 — Three incoming calls within five minutes
     * Should show only the latest incoming call (1 group with 1 call).
     */
    @Test
    fun testSituation1_ThreeIncomingCallsWithinFiveMinutes() {
        val baseTime = System.currentTimeMillis()
        val calls = listOf(
            createCall(1, "+1234567890", "John", baseTime, CallType.INCOMING),
            createCall(2, "+1234567890", "John", baseTime + 2 * 60 * 1000L, CallType.INCOMING),
            createCall(3, "+1234567890", "John", baseTime + 4 * 60 * 1000L, CallType.INCOMING)
        )

        val result = groupingService.groupCalls(calls)

        assertEquals("Should create 1 group (latest call only)", 1, result.totalGroups)
        assertEquals("Group should have 1 call", 1, result.groups[0].totalCallCount)
        assertEquals("Group should have 0 missed calls", 0, result.groups[0].missedCallCount)
    }

    /**
     * Situation 2 — Incoming call followed by an outgoing callback
     * Should show 2 groups (latest incoming and latest outgoing separately).
     */
    @Test
    fun testSituation2_IncomingThenOutgoingCallback() {
        val baseTime = System.currentTimeMillis()
        val calls = listOf(
            createCall(1, "+1234567890", "John", baseTime, CallType.INCOMING),
            createCall(2, "+1234567890", "John", baseTime + 2 * 60 * 1000L, CallType.OUTGOING)
        )

        val result = groupingService.groupCalls(calls)

        assertEquals("Should create 2 groups (separate latest calls)", 2, result.totalGroups)
        assertEquals("First group should have 1 call (latest incoming)", 1, result.groups[0].totalCallCount)
        assertEquals("Second group should have 1 call (latest outgoing)", 1, result.groups[1].totalCallCount)
    }

    /**
     * Situation 3 — Calls separated by 30 minutes
     * Should show only the latest incoming call (1 group with 1 call).
     */
    @Test
    fun testSituation3_CallsSeparatedBy30Minutes() {
        val baseTime = System.currentTimeMillis()
        val calls = listOf(
            createCall(1, "+1234567890", "John", baseTime, CallType.INCOMING),
            createCall(2, "+1234567890", "John", baseTime + 30 * 60 * 1000L, CallType.INCOMING)
        )

        val result = groupingService.groupCalls(calls)

        assertEquals("Should create 1 group (latest call only)", 1, result.totalGroups)
        assertEquals("Group should have 1 call", 1, result.groups[0].totalCallCount)
    }

    /**
     * Situation 4 — Calls separated by six hours
     * Should show only the latest incoming call (1 group with 1 call).
     */
    @Test
    fun testSituation4_CallsSeparatedBySixHours() {
        val baseTime = System.currentTimeMillis()
        val calls = listOf(
            createCall(1, "+1234567890", "John", baseTime, CallType.INCOMING),
            createCall(2, "+1234567890", "John", baseTime + 6 * 60 * 60 * 1000L, CallType.INCOMING)
        )

        val result = groupingService.groupCalls(calls)

        assertEquals("Should create 1 group (latest call only)", 1, result.totalGroups)
        assertEquals("Group should have 1 call", 1, result.groups[0].totalCallCount)
    }

    /**
     * Situation 5 — Calls separated by more than seven hours
     * Should show only the latest incoming call (1 group with 1 call).
     */
    @Test
    fun testSituation5_CallsSeparatedByMoreThanSevenHours() {
        val baseTime = System.currentTimeMillis()
        val calls = listOf(
            createCall(1, "+1234567890", "John", baseTime, CallType.INCOMING),
            createCall(2, "+1234567890", "John", baseTime + 7 * 60 * 60 * 1000L + 60 * 1000L, CallType.INCOMING)
        )

        val result = groupingService.groupCalls(calls)

        assertEquals("Should create 1 group (latest call only)", 1, result.totalGroups)
        assertEquals("Group should have 1 call", 1, result.groups[0].totalCallCount)
    }

    /**
     * Situation 6 — Exactly seven hours apart
     * Should show only the latest incoming call (1 group with 1 call).
     */
    @Test
    fun testSituation6_ExactlySevenHoursApart() {
        val baseTime = System.currentTimeMillis()
        val calls = listOf(
            createCall(1, "+1234567890", "John", baseTime, CallType.INCOMING),
            createCall(2, "+1234567890", "John", baseTime + 7 * 60 * 60 * 1000L, CallType.INCOMING)
        )

        val result = groupingService.groupCalls(calls)

        assertEquals("Should create 1 group (latest call only)", 1, result.totalGroups)
        assertEquals("Group should have 1 call", 1, result.groups[0].totalCallCount)
    }

    /**
     * Situation 7 — Rolling grouping window
     * Should show only the latest incoming call (1 group with 1 call).
     */
    @Test
    fun testSituation7_RollingGroupingWindow() {
        val baseTime = System.currentTimeMillis()
        val calls = listOf(
            createCall(1, "+1234567890", "John", baseTime, CallType.INCOMING),
            createCall(2, "+1234567890", "John", baseTime + 6 * 60 * 60 * 1000L, CallType.INCOMING),
            createCall(3, "+1234567890", "John", baseTime + 6 * 60 * 60 * 1000L + 30 * 60 * 1000L, CallType.INCOMING)
        )

        val result = groupingService.groupCalls(calls)

        assertEquals("Should create 1 group (latest call only)", 1, result.totalGroups)
        assertEquals("Group should have 1 call", 1, result.groups[0].totalCallCount)
    }

    /**
     * Situation 8 — Calls on consecutive days
     * Should show only the latest incoming call (1 group with 1 call).
     */
    @Test
    fun testSituation8_CallsOnConsecutiveDays() {
        val baseTime = System.currentTimeMillis()
        val calls = listOf(
            createCall(1, "+1234567890", "John", baseTime, CallType.INCOMING),
            createCall(2, "+1234567890", "John", baseTime + 24 * 60 * 60 * 1000L, CallType.INCOMING)
        )

        val result = groupingService.groupCalls(calls)

        assertEquals("Should create 1 group (latest call only)", 1, result.totalGroups)
        assertEquals("Group should have 1 call", 1, result.groups[0].totalCallCount)
    }

    /**
     * Situation 9 — Three missed calls
     * Should show a total count of 3 and a missed-call count of 3.
     */
    @Test
    fun testSituation9_ThreeMissedCalls() {
        val baseTime = System.currentTimeMillis()
        val calls = listOf(
            createCall(1, "+1234567890", "John", baseTime, CallType.MISSED),
            createCall(2, "+1234567890", "John", baseTime + 2 * 60 * 1000L, CallType.MISSED),
            createCall(3, "+1234567890", "John", baseTime + 4 * 60 * 1000L, CallType.MISSED)
        )

        val result = groupingService.groupCalls(calls)

        assertEquals("Should create 1 group", 1, result.totalGroups)
        assertEquals("Total calls should be 3", 3, result.groups[0].totalCallCount)
        assertEquals("Missed calls should be 3", 3, result.groups[0].missedCallCount)
    }

    /**
     * Situation 10 — Missed call followed by an answered incoming call
     * Should show 2 groups (missed group and latest incoming).
     */
    @Test
    fun testSituation10_MissedThenAnsweredIncoming() {
        val baseTime = System.currentTimeMillis()
        val calls = listOf(
            createCall(1, "+1234567890", "John", baseTime, CallType.MISSED),
            createCall(2, "+1234567890", "John", baseTime + 5 * 60 * 1000L, CallType.INCOMING)
        )

        val result = groupingService.groupCalls(calls)

        assertEquals("Should create 2 groups (missed + latest incoming)", 2, result.totalGroups)
        assertEquals("First group should have 1 missed call", 1, result.groups[0].totalCallCount)
        assertEquals("Second group should have 1 incoming call", 1, result.groups[1].totalCallCount)
    }

    /**
     * Situation 11 — Missed call followed by an outgoing callback
     * Should show 2 groups (missed group and latest outgoing).
     */
    @Test
    fun testSituation11_MissedThenOutgoingCallback() {
        val baseTime = System.currentTimeMillis()
        val calls = listOf(
            createCall(1, "+1234567890", "John", baseTime, CallType.MISSED),
            createCall(2, "+1234567890", "John", baseTime + 3 * 60 * 1000L, CallType.OUTGOING)
        )

        val result = groupingService.groupCalls(calls)

        assertEquals("Should create 2 groups (missed + latest outgoing)", 2, result.totalGroups)
        assertEquals("First group should have 1 missed call", 1, result.groups[0].totalCallCount)
        assertEquals("Second group should have 1 outgoing call", 1, result.groups[1].totalCallCount)
    }

    /**
     * Situation 12 — Incoming and outgoing calls mixed together
     * Should show 2 groups (latest incoming and latest outgoing).
     */
    @Test
    fun testSituation12_MixedIncomingAndOutgoing() {
        val baseTime = System.currentTimeMillis()
        val calls = listOf(
            createCall(1, "+1234567890", "John", baseTime, CallType.INCOMING),
            createCall(2, "+1234567890", "John", baseTime + 60 * 1000L, CallType.OUTGOING),
            createCall(3, "+1234567890", "John", baseTime + 2 * 60 * 1000L, CallType.INCOMING),
            createCall(4, "+1234567890", "John", baseTime + 3 * 60 * 1000L, CallType.OUTGOING)
        )

        val result = groupingService.groupCalls(calls)

        assertEquals("Should create 2 groups (latest incoming + latest outgoing)", 2, result.totalGroups)
        assertEquals("First group should have 1 call (latest incoming)", 1, result.groups[0].totalCallCount)
        assertEquals("Second group should have 1 call (latest outgoing)", 1, result.groups[1].totalCallCount)
    }

    /**
     * Situation 13 — One call after the 7-hour limit
     * Should show only the latest incoming call (1 group with 1 call).
     */
    @Test
    fun testSituation13_CallAfter7HourLimit() {
        val baseTime = System.currentTimeMillis()
        val calls = listOf(
            createCall(1, "+1234567890", "John", baseTime, CallType.INCOMING),
            createCall(2, "+1234567890", "John", baseTime + 6 * 60 * 60 * 1000L, CallType.INCOMING),
            createCall(3, "+1234567890", "John", baseTime + 13 * 60 * 60 * 1000L + 60 * 1000L, CallType.INCOMING)
        )

        val result = groupingService.groupCalls(calls)

        assertEquals("Should create 1 group (latest call only)", 1, result.totalGroups)
        assertEquals("Group should have 1 call", 1, result.groups[0].totalCallCount)
    }

    /**
     * Situation 14 — Different contacts
     * Should never merge their groups.
     */
    @Test
    fun testSituation14_DifferentContacts() {
        val baseTime = System.currentTimeMillis()
        val calls = listOf(
            createCall(1, "+1234567890", "John", baseTime, CallType.INCOMING),
            createCall(2, "+1987654321", "Jane", baseTime + 60 * 1000L, CallType.INCOMING)
        )

        val result = groupingService.groupCalls(calls)

        assertEquals("Should create 2 groups", 2, result.totalGroups)
    }

    /**
     * Situation 15 — Same contact, two phone numbers
     * Should keep them in separate groups.
     */
    @Test
    fun testSituation15_SameContactDifferentNumbers() {
        val baseTime = System.currentTimeMillis()
        val calls = listOf(
            createCall(1, "+1234567890", "John", baseTime, CallType.INCOMING),
            createCall(2, "+1987654321", "John", baseTime + 60 * 1000L, CallType.INCOMING)
        )

        val result = groupingService.groupCalls(calls)

        assertEquals("Should create 2 groups", 2, result.totalGroups)
    }

    /**
     * Situation 16 — Unknown number called repeatedly
     * Should show only the latest incoming call (1 group with 1 call).
     */
    @Test
    fun testSituation16_UnknownNumberRepeatedly() {
        val baseTime = System.currentTimeMillis()
        val calls = listOf(
            createCall(1, "+1234567890", null, baseTime, CallType.INCOMING),
            createCall(2, "+1234567890", null, baseTime + 2 * 60 * 1000L, CallType.INCOMING),
            createCall(3, "+1234567890", null, baseTime + 4 * 60 * 1000L, CallType.INCOMING)
        )

        val result = groupingService.groupCalls(calls)

        assertEquals("Should create 1 group (latest call only)", 1, result.totalGroups)
        assertEquals("Group should have 1 call", 1, result.groups[0].totalCallCount)
        assertNull("Contact name should be null", result.groups[0].contactName)
    }

    /**
     * Situation 17 — Blocked number calls repeatedly
     * Should group available call-log records for the same blocked number.
     */
    @Test
    fun testSituation17_BlockedNumberRepeatedly() {
        val baseTime = System.currentTimeMillis()
        val calls = listOf(
            createCall(1, "+1234567890", null, baseTime, CallType.BLOCKED, isBlocked = true),
            createCall(2, "+1234567890", null, baseTime + 2 * 60 * 1000L, CallType.BLOCKED, isBlocked = true)
        )

        val result = groupingService.groupCalls(calls)

        assertEquals("Should create 1 group", 1, result.totalGroups)
        assertTrue("Group should be marked as blocked", result.groups[0].isBlocked)
    }

    /**
     * Situation 18 — Contact is trusted or suspicious
     * Should show only the latest incoming call (1 group with 1 call).
     */
    @Test
    fun testSituation18_TrustedOrSuspiciousContact() {
        val baseTime = System.currentTimeMillis()
        val calls = listOf(
            createCall(1, "+1234567890", "John", baseTime, CallType.INCOMING),
            createCall(2, "+1234567890", "John", baseTime + 2 * 60 * 1000L, CallType.INCOMING)
        )

        val result = groupingService.groupCalls(calls)

        assertEquals("Should create 1 group (latest call only)", 1, result.totalGroups)
        assertEquals("Group should have 1 call", 1, result.groups[0].totalCallCount)
    }

    /**
     * Situation 19 — Missed-call notification has been acknowledged
     * This test verifies that viewing calls doesn't affect grouping.
     * The grouping is based on call log records, not notification state.
     * Missed and incoming should be in separate groups.
     */
    @Test
    fun testSituation19_MissedCallNotificationAcknowledged() {
        val baseTime = System.currentTimeMillis()
        val calls = listOf(
            createCall(1, "+1234567890", "John", baseTime, CallType.MISSED),
            createCall(2, "+1234567890", "John", baseTime + 2 * 60 * 1000L, CallType.INCOMING)
        )

        val result = groupingService.groupCalls(calls)

        assertEquals("Should create 2 groups (separate for missed and incoming)", 2, result.totalGroups)
        assertEquals("First group should have 1 missed call", 1, result.groups[0].totalCallCount)
        assertEquals("Second group should have 1 incoming call", 1, result.groups[1].totalCallCount)
    }

    /**
     * Situation 20 — Permission denied or revoked
     * Should not display fabricated records or counts.
     */
    @Test
    fun testSituation20_PermissionDenied() {
        val calls = emptyList<RawCallRecord>()

        val result = groupingService.groupCalls(calls)

        assertEquals("Should create 0 groups", 0, result.totalGroups)
        assertEquals("Total calls should be 0", 0, result.totalCalls)
    }

    /**
     * Additional test: Verify groups are sorted newest-first
     */
    @Test
    fun testGroupsSortedNewestFirst() {
        val baseTime = System.currentTimeMillis()
        val calls = listOf(
            createCall(1, "+1234567890", "John", baseTime - 24 * 60 * 60 * 1000L, CallType.INCOMING),
            createCall(2, "+1987654321", "Jane", baseTime, CallType.INCOMING),
            createCall(3, "+1555555555", "Bob", baseTime - 12 * 60 * 60 * 1000L, CallType.INCOMING)
        )

        val result = groupingService.groupCalls(calls)

        assertEquals("Should create 3 groups", 3, result.totalGroups)
        assertTrue("First group should be newest", result.groups[0].newestCall.timestampMillis > result.groups[1].newestCall.timestampMillis)
        assertTrue("Second group should be newer than third", result.groups[1].newestCall.timestampMillis > result.groups[2].newestCall.timestampMillis)
    }

    /**
     * Additional test: Verify phone number normalization
     */
    @Test
    fun testPhoneNumberNormalization() {
        val formatted = "+1 (555) 123-4567"
        val normalized = groupingService.normalizePhoneNumber(formatted)
        assertEquals("Normalized number", "+15551234567", normalized)
    }

    /**
     * Additional test: Verify REJECTED calls are not counted as MISSED
     */
    @Test
    fun testRejectedNotCountedAsMissed() {
        val baseTime = System.currentTimeMillis()
        val calls = listOf(
            createCall(1, "+1234567890", "John", baseTime, CallType.REJECTED),
            createCall(2, "+1234567890", "John", baseTime + 2 * 60 * 1000L, CallType.MISSED)
        )

        val result = groupingService.groupCalls(calls)

        assertEquals("Should create 2 groups (separate for rejected and missed)", 2, result.totalGroups)
        assertEquals("Missed group should have 1 missed call", 1, result.groups[0].missedCallCount)
    }
}
