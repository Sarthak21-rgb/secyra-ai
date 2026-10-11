package com.secyra.ai.feature.messages

import com.secyra.ai.data.repository.SmsConversation

// Common security level enum used across MessagesScreen and SmsSecurityAnalyzer
enum class SecurityLevel(val displayName: String) {
    Trusted("Trusted"),
    Unknown("Unknown"),
    Suspicious("Suspicious"),
    Blocked("Blocked")
}

data class MessageConversation(
    val threadId: Long,
    val address: String,
    val displayName: String,
    val snippet: String,
    val timestamp: String,
    val securityLevel: SecurityLevel,
    val initials: String,
    val unreadCount: Int = 0,
    val isLatestMessageOutgoing: Boolean = false
) {
    companion object {
        fun fromSmsConversation(conversation: SmsConversation): MessageConversation {
            val address = conversation.address
            val initials = if (address.isNotEmpty()) {
                address.take(2).uppercase()
            } else {
                "UN"
            }

            return MessageConversation(
                threadId = conversation.threadId,
                address = address,
                displayName = address, // TODO: Resolve to contact name
                snippet = conversation.snippet,
                timestamp = formatTimestamp(conversation.timestamp),
                securityLevel = SecurityLevel.Unknown, // TODO: Implement security analysis
                initials = initials,
                unreadCount = conversation.unreadCount,
                isLatestMessageOutgoing = conversation.isLatestMessageOutgoing
            )
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    val calendar = java.util.Calendar.getInstance()
    calendar.timeInMillis = timestamp

    val hours = calendar.get(java.util.Calendar.HOUR_OF_DAY)
    val minutes = calendar.get(java.util.Calendar.MINUTE)
    val ampm = if (hours >= 12) "PM" else "AM"
    val displayHours = if (hours % 12 == 0) 12 else hours % 12
    val displayMinutes = if (minutes < 10) "0$minutes" else minutes

    return when {
        diff < 86400000 -> "$displayHours:$displayMinutes $ampm" // Today: show time
        diff < 172800000 -> "Yesterday" // Yesterday
        diff < 604800000 -> {
            val dayOfWeek = calendar.get(java.util.Calendar.DAY_OF_WEEK)
            when (dayOfWeek) {
                java.util.Calendar.MONDAY -> "Mon"
                java.util.Calendar.TUESDAY -> "Tue"
                java.util.Calendar.WEDNESDAY -> "Wed"
                java.util.Calendar.THURSDAY -> "Thu"
                java.util.Calendar.FRIDAY -> "Fri"
                java.util.Calendar.SATURDAY -> "Sat"
                java.util.Calendar.SUNDAY -> "Sun"
                else -> ""
            }
        }
        else -> "${calendar.get(java.util.Calendar.MONTH) + 1}/${calendar.get(java.util.Calendar.DAY_OF_MONTH)}"
    }
}
