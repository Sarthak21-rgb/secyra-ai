package com.secyra.ai.data.repository

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.provider.CallLog
import com.secyra.ai.core.permissions.PermissionManager
import com.secyra.ai.feature.calls.CallItem
import com.secyra.ai.core.ui.theme.*
import androidx.compose.ui.graphics.Color
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class CallLogRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val permissionManager: PermissionManager
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

            var count = 0
            while (it.moveToNext() && count < limit) {
                val id = it.getLong(idIndex).toString()
                val number = it.getString(numberIndex) ?: "Unknown"
                val name = it.getString(nameIndex)
                val type = it.getInt(typeIndex)
                val date = it.getLong(dateIndex)
                val duration = it.getLong(durationIndex)

                val displayName = name ?: "Unknown"
                val initials = if (name != null && name.isNotEmpty()) {
                    name.first().toString().uppercase()
                } else {
                    "U"
                }

                val callType = when (type) {
                    CallLog.Calls.INCOMING_TYPE -> "Incoming"
                    CallLog.Calls.OUTGOING_TYPE -> "Outgoing"
                    CallLog.Calls.MISSED_TYPE -> "Missed"
                    CallLog.Calls.VOICEMAIL_TYPE -> "Voicemail"
                    CallLog.Calls.REJECTED_TYPE -> "Rejected"
                    CallLog.Calls.BLOCKED_TYPE -> "Blocked"
                    CallLog.Calls.ANSWERED_EXTERNALLY_TYPE -> "Answered"
                    else -> "Unknown"
                }

                val timestamp = try {
                    val callDate = Date(date)
                    val now = Date()
                    val diff = now.time - callDate.time
                    val days = diff / (1000 * 60 * 60 * 24)
                    
                    when {
                        days < 1 -> "Today, ${timeFormat.format(callDate)}"
                        days < 2 -> "Yesterday, ${timeFormat.format(callDate)}"
                        else -> dateFormat.format(callDate)
                    }
                } catch (e: Exception) {
                    "Unknown time"
                }

                // Determine if this is a saved contact or unknown number
                val isSavedContact = name != null && name.isNotEmpty()
                val isSpam = !isSavedContact && detectSpam(number)
                val isTrusted = isSavedContact
                val isMissed = type == CallLog.Calls.MISSED_TYPE

                val status = when {
                    isMissed -> "Missed"
                    isSpam -> "Potential spam"
                    isTrusted -> "Trusted"
                    else -> callType
                }

                val statusColor = when {
                    isMissed -> RiskHigh
                    isSpam -> RiskHigh
                    isTrusted -> RiskLow
                    else -> SecyraTextSecondary
                }

                val avatarColor = when {
                    isMissed -> Color(0xFF3F1C1C)
                    isSpam -> Color(0xFF3F1C1C)
                    isTrusted -> Color(0xFF1C3F2C)
                    else -> Color(0xFF2C2C2C)
                }

                val callIconColor = when {
                    isMissed -> RiskHigh
                    isSpam -> RiskHigh
                    else -> TiffanyBlue
                }

                val callDirection = when (type) {
                    CallLog.Calls.INCOMING_TYPE -> "incoming"
                    CallLog.Calls.OUTGOING_TYPE -> "outgoing"
                    CallLog.Calls.MISSED_TYPE -> "missed"
                    else -> "unknown"
                }

                calls.add(
                    CallItem(
                        id = id,
                        name = displayName,
                        initials = initials,
                        status = status,
                        statusColor = statusColor,
                        avatarColor = avatarColor,
                        timestamp = timestamp,
                        phoneNumber = number,
                        isSpam = isSpam,
                        isTrusted = isTrusted,
                        callIconColor = callIconColor,
                        callDirection = callDirection,
                        isMissed = isMissed
                    )
                )
                count++
            }
        }

        calls
    }

    private fun detectSpam(number: String): Boolean {
        // Simple spam detection patterns
        val spamPatterns = listOf(
            Regex("^(\\+1)?0800"), // Toll-free spam
            Regex("^(\\+1)?800"), // 800 numbers
            Regex("^(\\+1)?900"), // Premium rate
            Regex("^\\+?[0-9]{10,}"), // Very long numbers
            Regex("^(\\+1)?555[0-9]{4}") // Fake 555 numbers
        )

        return spamPatterns.any { it.containsMatchIn(number) }
    }
}
