package com.secyra.ai.data.model

data class SmsMessage(
    val id: Long,
    val threadId: Long,
    val address: String,
    val body: String,
    val date: Long,
    val type: SmsType,
    val read: Boolean,
    val seen: Boolean,
    val status: SmsStatus,
    val subscriptionId: Int? = null
)

enum class SmsType {
    INBOX,
    SENT,
    DRAFT,
    OUTBOX,
    FAILED,
    QUEUED,
    UNKNOWN
}

enum class SmsStatus {
    NONE,
    COMPLETE,
    PENDING,
    FAILED,
    SENT
}

data class SmsConversation(
    val threadId: Long,
    val address: String,
    val contactName: String?,
    val snippet: String,
    val date: Long,
    val timestamp: Long,
    val unreadCount: Int,
    val messageCount: Int,
    val isLatestMessageOutgoing: Boolean,
    val securityStatus: SecurityStatus = SecurityStatus.UNKNOWN
)

enum class SecurityStatus {
    TRUSTED,
    SAFE,
    LOW_RISK,
    MEDIUM_RISK,
    HIGH_RISK,
    UNKNOWN,
    BLOCKED
}

enum class MessagesTab {
    All,
    Unread,
    Suspicious,
    Blocked
}

enum class CallsPage {
    Calls,
    Messages
}
