package com.secyra.ai.data.repository

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.provider.CallLog
import com.secyra.ai.core.permissions.PermissionManager
import com.secyra.ai.feature.calls.CallItem
import com.secyra.ai.core.telecom.RawCallRecord
import com.secyra.ai.core.telecom.CallType
import com.secyra.ai.core.ui.theme.*
import com.secyra.ai.core.security.CallerIdentificationService
import com.secyra.ai.core.security.AdvancedSpamDetectionEngine
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

/**
 * Custom colors for contact avatars
 */
private val CONTACT_COLORS = listOf(
    Color(0xFF007FFF), // Azure Blue
    Color(0xFF0ABAB5), // Tiffany Blue
    Color(0xFF008080), // Teal Blue
    Color(0xFF8A2BE2), // Violet
    Color(0xFFFF9933), // Saffron
    Color(0xFF2E8B57), // Pine
    Color(0xFF00A86B), // Pantone Jade
    Color(0xFF960018), // Carmine
    Color(0xFF8E4585), // Plum
    Color(0xFFDA70D6), // Orchid Purple
    Color(0xFFE30B5D), // Raspberry
    Color(0xFFC21807), // Chili
    Color(0xFF1565C0), // Parish Blue
    Color(0xFF50C878), // Emerald Green
    Color(0xFF000080), // Navy Blue
    Color(0xFFE2725B), // Terracotta
    Color(0xFF663399), // Damson
    Color(0xFF6F4E37), // Coffee
    Color(0xFF228B22), // Forest
    Color(0xFF722F37), // Wine
    Color(0xFFCC7722), // Ochre
    Color(0xFF7B3F00), // Cacao
    Color(0xFFA0522D)  // Auburn
)

/**
 * Get a consistent color for a contact based on their name or phone number
 */
private var lastUsedColorIndex = -1

private fun getContactColor(name: String?, phoneNumber: String): Color {
    val key = (name ?: phoneNumber).hashCode()
    var index = kotlin.math.abs(key) % CONTACT_COLORS.size
    
    // Avoid repeating the same color consecutively
    if (index == lastUsedColorIndex) {
        index = (index + 1) % CONTACT_COLORS.size
    }
    lastUsedColorIndex = index
    
    return CONTACT_COLORS[index]
}

class CallLogRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val permissionManager: PermissionManager,
    private val callerIdentificationService: CallerIdentificationService,
    private val spamDetectionEngine: AdvancedSpamDetectionEngine
) {

    @SuppressLint("SimpleDateFormat")
    suspend fun getRecentCalls(limit: Int = 50): List<CallItem> = withContext(Dispatchers.IO) {
        if (!permissionManager.isPermissionGranted(com.secyra.ai.core.permissions.PermissionManager.Permissions.READ_CALL_LOG)) {
            return@withContext emptyList()
        }

        val calls = mutableListOf<CallItem>()
        val dateFormat = SimpleDateFormat("MMM dd, yyyy, hh:mm a", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        val projection = arrayOf(
            CallLog.Calls._ID,
            CallLog.Calls.NUMBER,
            CallLog.Calls.CACHED_NAME,
            CallLog.Calls.TYPE,
            CallLog.Calls.DATE,
            CallLog.Calls.DURATION
        )

        val sortOrder = "${CallLog.Calls.DATE} DESC"

        val cursor: Cursor? = context.contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            projection,
            null,
            null,
            sortOrder
        )

        cursor?.use {
            val idIndex = it.getColumnIndex(CallLog.Calls._ID)
            val numberIndex = it.getColumnIndex(CallLog.Calls.NUMBER)
            val nameIndex = it.getColumnIndex(CallLog.Calls.CACHED_NAME)
            val typeIndex = it.getColumnIndex(CallLog.Calls.TYPE)
            val dateIndex = it.getColumnIndex(CallLog.Calls.DATE)
            val durationIndex = it.getColumnIndex(CallLog.Calls.DURATION)

            android.util.Log.d("CallLogRepository", "Cursor count: ${it.count}")
            var count = 0
            while (it.moveToNext() && count < limit) {
                try {
                    val id = it.getLong(idIndex).toString()
                    val number = it.getString(numberIndex) ?: "Unknown"
                    val name = it.getString(nameIndex)
                    val type = it.getInt(typeIndex)
                    val date = it.getLong(dateIndex)
                    val duration = it.getLong(durationIndex)
                    android.util.Log.d("CallLogRepository", "Processing call $count: number=$number, name=$name")

                    // Truncate name to 20 characters if too long
                    val truncatedName = if (name != null && name.length > 20) {
                        name.take(20) + "..."
                    } else {
                        name ?: "Unknown"
                    }
                    val initials = if (name != null && name.isNotEmpty()) {
                        name.first().toString().uppercase()
                    } else {
                        "U"
                    }

                    val callType = when (type) {
                        CallLog.Calls.INCOMING_TYPE -> com.secyra.ai.feature.calls.CallType.INCOMING
                        CallLog.Calls.OUTGOING_TYPE -> com.secyra.ai.feature.calls.CallType.OUTGOING
                        CallLog.Calls.MISSED_TYPE -> com.secyra.ai.feature.calls.CallType.MISSED
                        CallLog.Calls.REJECTED_TYPE -> com.secyra.ai.feature.calls.CallType.REJECTED
                        CallLog.Calls.VOICEMAIL_TYPE -> com.secyra.ai.feature.calls.CallType.INCOMING
                        CallLog.Calls.BLOCKED_TYPE -> com.secyra.ai.feature.calls.CallType.MISSED
                        CallLog.Calls.ANSWERED_EXTERNALLY_TYPE -> com.secyra.ai.feature.calls.CallType.INCOMING
                        else -> com.secyra.ai.feature.calls.CallType.UNKNOWN
                    }

                    val callTypeLabel = when (callType) {
                        com.secyra.ai.feature.calls.CallType.INCOMING -> "Incoming"
                        com.secyra.ai.feature.calls.CallType.OUTGOING -> "Outgoing"
                        com.secyra.ai.feature.calls.CallType.MISSED -> "Missed"
                        com.secyra.ai.feature.calls.CallType.REJECTED -> "Declined"
                        com.secyra.ai.feature.calls.CallType.BLOCKED -> "Blocked"
                        com.secyra.ai.feature.calls.CallType.UNKNOWN -> "Call"
                    }

                    val timestamp = try {
                        val callDate = Date(date)
                        val now = Date()
                        val calendar = Calendar.getInstance()
                        calendar.time = now
                        val currentYear = calendar.get(Calendar.YEAR)
                        val currentDay = calendar.get(Calendar.DAY_OF_YEAR)

                        calendar.time = callDate
                        val callYear = calendar.get(Calendar.YEAR)
                        val callDay = calendar.get(Calendar.DAY_OF_YEAR)

                        val dayDiff = currentDay - callDay
                        val yearDiff = currentYear - callYear

                        when {
                            yearDiff == 0 && dayDiff == 0 -> {
                                // Today - show time
                                val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
                                timeFormat.format(callDate)
                            }
                            yearDiff == 0 && dayDiff == 1 -> {
                                // Yesterday
                                "Yesterday"
                            }
                            yearDiff == 0 && dayDiff in 2..6 -> {
                                // This week - show weekday
                                val dayFormat = SimpleDateFormat("EEEE", Locale.getDefault())
                                dayFormat.format(callDate)
                            }
                            yearDiff == 0 -> {
                                // This year - show day and month
                                val dateFormat = SimpleDateFormat("d MMM", Locale.getDefault())
                                dateFormat.format(callDate)
                            }
                            else -> {
                                // Previous year - show day, month, year
                                val dateFormat = SimpleDateFormat("d MMM yyyy", Locale.getDefault())
                                dateFormat.format(callDate)
                            }
                        }
                    } catch (e: Exception) {
                        "Unknown time"
                    }

                    // Advanced spam detection using the new engine
                    val spamAnalysis = runCatching {
                        spamDetectionEngine.analyzeNumber(number)
                    }.getOrNull()

                    val isSpam = spamAnalysis?.spamLevel == com.secyra.ai.core.security.SpamLevel.HIGH ||
                                spamAnalysis?.spamLevel == com.secyra.ai.core.security.SpamLevel.MEDIUM
                    
                    // Use caller ID name if available and better than cached name
                    val callerName = spamAnalysis?.callerName ?: name

                    val status = when {
                        isSpam -> "Spam"
                        callerName != null && callerName != name -> "$callerName • $callTypeLabel"
                        name != null -> callTypeLabel
                        else -> callTypeLabel
                    }

                    val statusColor = when {
                        isSpam -> RiskHigh
                        else -> SecyraTextSecondary
                    }

                    val avatarColor = getContactColor(callerName, number)

                    val callIconColor = when {
                        isSpam -> RiskHigh
                        else -> TiffanyBlue
                    }

                    // Calculate call count in the same hour - disabled to avoid cursor reset issue
                    val callCountInHour = 1

                    // Determine call type icon
                    val callTypeIcon = when (callType) {
                        com.secyra.ai.feature.calls.CallType.INCOMING -> Icons.Default.Phone
                        com.secyra.ai.feature.calls.CallType.OUTGOING -> Icons.Default.Phone
                        com.secyra.ai.feature.calls.CallType.MISSED -> Icons.Default.PhoneMissed
                        com.secyra.ai.feature.calls.CallType.REJECTED -> Icons.Default.PhoneDisabled
                        com.secyra.ai.feature.calls.CallType.BLOCKED -> Icons.Default.Block
                        com.secyra.ai.feature.calls.CallType.UNKNOWN -> Icons.Default.Phone
                    }

                    // Determine security tag based on spam analysis
                    val securityTag = when {
                        isSpam -> com.secyra.ai.feature.calls.SecurityTag.SPAM
                        callerName != null && callerName != name -> com.secyra.ai.feature.calls.SecurityTag.TRUSTED
                        name != null -> com.secyra.ai.feature.calls.SecurityTag.UNKNOWN // Saved contact - will be hidden in UI
                        else -> com.secyra.ai.feature.calls.SecurityTag.UNKNOWN
                    }

                    // Use caller ID name if available, otherwise use cached name
                    val displayName = if (callerName != null && callerName.isNotEmpty()) {
                        callerName.take(20)
                    } else {
                        truncatedName
                    }

                    // Recalculate initials based on display name
                    val displayInitials = if (displayName.isNotEmpty()) {
                        displayName.first().toString().uppercase()
                    } else {
                        "C"
                    }

                    calls.add(
                        CallItem(
                            id = id,
                            name = displayName,
                            initials = displayInitials,
                            status = status,
                            statusColor = statusColor,
                            avatarColor = avatarColor,
                            timestamp = timestamp,
                            phoneNumber = number,
                            isSpam = isSpam,
                            isTrusted = callerName != null && callerName != name,
                            callIconColor = callIconColor,
                            isSavedContact = name != null,
                            contactId = null,
                            callCountInHour = callCountInHour,
                            callTypeIcon = callTypeIcon,
                            callType = callType,
                            securityTag = securityTag,
                            timestampMillis = date
                        )
                    )
                    count++
                } catch (e: Exception) {
                    android.util.Log.e("CallLogRepository", "Error processing call at index $count", e)
                }
            }
        }

        android.util.Log.d("CallLogRepository", "Total calls added to list: ${calls.size}")
        calls
    }

    private fun countCallsInHour(number: String, currentCallDate: Long, cursor: Cursor, numberIndex: Int, dateIndex: Int): Int {
        var count = 0
        val oneHourInMillis = 60 * 60 * 1000L
        val hourStart = currentCallDate - oneHourInMillis
        val hourEnd = currentCallDate + oneHourInMillis

        cursor.moveToPosition(-1) // Reset to beginning
        while (cursor.moveToNext()) {
            val callNumber = cursor.getString(numberIndex) ?: continue
            if (callNumber != number) continue

            val callDate = cursor.getLong(dateIndex)
            if (callDate in hourStart..hourEnd) {
                count++
            }
        }

        return count
    }

    /**
     * Get raw call records for grouping purposes.
     * Returns minimal data needed for grouping logic.
     */
    @SuppressLint("SimpleDateFormat")
    suspend fun getRawCallRecords(limit: Int = 1000): List<RawCallRecord> = withContext(Dispatchers.IO) {
        if (!permissionManager.isPermissionGranted(com.secyra.ai.core.permissions.PermissionManager.Permissions.READ_CALL_LOG)) {
            return@withContext emptyList()
        }

        val calls = mutableListOf<RawCallRecord>()

        val projection = arrayOf(
            CallLog.Calls._ID,
            CallLog.Calls.NUMBER,
            CallLog.Calls.CACHED_NAME,
            CallLog.Calls.TYPE,
            CallLog.Calls.DATE,
            CallLog.Calls.DURATION
        )

        val sortOrder = "${CallLog.Calls.DATE} DESC"

        val cursor: Cursor? = context.contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            projection,
            null,
            null,
            sortOrder
        )

        cursor?.use {
            val idIndex = it.getColumnIndex(CallLog.Calls._ID)
            val numberIndex = it.getColumnIndex(CallLog.Calls.NUMBER)
            val nameIndex = it.getColumnIndex(CallLog.Calls.CACHED_NAME)
            val typeIndex = it.getColumnIndex(CallLog.Calls.TYPE)
            val dateIndex = it.getColumnIndex(CallLog.Calls.DATE)
            val durationIndex = it.getColumnIndex(CallLog.Calls.DURATION)

            android.util.Log.d("CallLogRepository", "Raw cursor count: ${it.count}")
            var count = 0
            while (it.moveToNext() && count < limit) {
                try {
                    val id = it.getLong(idIndex)
                    val number = it.getString(numberIndex) ?: "Unknown"
                    val name = it.getString(nameIndex)
                    val type = it.getInt(typeIndex)
                    val date = it.getLong(dateIndex)
                    val duration = it.getInt(durationIndex)

                    // Normalize phone number
                    val normalizedNumber = number.replace(Regex("[^+0-9]"), "")

                    // Convert Android call type to our CallType enum
                    val callType = when (type) {
                        CallLog.Calls.INCOMING_TYPE -> CallType.INCOMING
                        CallLog.Calls.OUTGOING_TYPE -> CallType.OUTGOING
                        CallLog.Calls.MISSED_TYPE -> CallType.MISSED
                        CallLog.Calls.REJECTED_TYPE -> CallType.REJECTED
                        CallLog.Calls.BLOCKED_TYPE -> CallType.BLOCKED
                        CallLog.Calls.VOICEMAIL_TYPE -> CallType.VOICEMAIL
                        else -> CallType.UNKNOWN
                    }

                    // Check if number is blocked (simplified - in production would use blocklist)
                    val isBlocked = callType == CallType.BLOCKED

                    val rawCall = RawCallRecord(
                        id = id,
                        phoneNumber = number,
                        normalizedNumber = normalizedNumber,
                        contactName = name,
                        contactPhotoUri = null, // Would fetch from contacts provider in production
                        isSavedContact = name != null,
                        timestampMillis = date,
                        callType = callType,
                        durationSeconds = duration,
                        isBlocked = isBlocked
                    )

                    calls.add(rawCall)
                    count++
                } catch (e: Exception) {
                    android.util.Log.e("CallLogRepository", "Error processing raw call at index $count", e)
                }
            }
        }

        android.util.Log.d("CallLogRepository", "Total raw calls: ${calls.size}")
        calls
    }
}
