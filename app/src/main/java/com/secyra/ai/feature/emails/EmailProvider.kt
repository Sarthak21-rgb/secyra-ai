package com.secyra.ai.feature.emails

/**
 * Enum representing supported email providers.
 */
enum class EmailProvider(val displayName: String) {
    GMAIL("Gmail"),
    OUTLOOK("Outlook"),
    IMAP("IMAP/SMTP (Coming Soon)")
}

/**
 * Data class representing connected email account information.
 */
data class EmailAccount(
    val provider: EmailProvider,
    val email: String,
    val accessToken: String,
    val refreshToken: String? = null,
    val connectedAt: Long = System.currentTimeMillis()
)

/**
 * Data class representing an email summary from providers.
 */
data class EmailSummary(
    val id: String,
    val from: String,
    val subject: String,
    val date: Long,
    val snippet: String
)
