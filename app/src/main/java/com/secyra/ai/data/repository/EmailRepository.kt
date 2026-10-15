package com.secyra.ai.data.repository

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.secyra.ai.core.permissions.PermissionManager
import com.secyra.ai.feature.emails.EmailAccount
import com.secyra.ai.feature.emails.EmailProvider
import com.secyra.ai.feature.emails.GmailOAuthManager
import com.secyra.ai.feature.emails.OutlookOAuthManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first

private val Context.emailDataStore: DataStore<Preferences> by preferencesDataStore(name = "email_accounts")

data class Email(
    val id: String,
    val sender: String,
    val subject: String,
    val preview: String,
    val timestamp: Long,
    val isRead: Boolean,
    val isInbox: Boolean = true
)

@Singleton
class EmailRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val permissionManager: PermissionManager,
    private val gmailOAuthManager: GmailOAuthManager,
    private val outlookOAuthManager: OutlookOAuthManager
) {
    private val dataStore = context.emailDataStore

    private val connectedAccountKey = stringPreferencesKey("connected_account")
    private val accessTokenKey = stringPreferencesKey("access_token")
    private val refreshTokenKey = stringPreferencesKey("refresh_token")
    private val providerKey = stringPreferencesKey("provider")

    /**
     * Flow that emits whether an account is connected
     */
    val isAccountConnected: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[connectedAccountKey] != null
    }

    /**
     * Flow that emits the connected account (if any)
     */
    val connectedAccount: Flow<EmailAccount?> = dataStore.data.map { preferences ->
        val email = preferences[connectedAccountKey]
        val accessToken = preferences[accessTokenKey]
        val refreshToken = preferences[refreshTokenKey]
        val providerName = preferences[providerKey]

        if (email != null && accessToken != null && providerName != null) {
            val provider = try {
                EmailProvider.valueOf(providerName)
            } catch (e: IllegalArgumentException) {
                EmailProvider.GMAIL
            }
            EmailAccount(
                provider = provider,
                email = email,
                accessToken = accessToken,
                refreshToken = refreshToken
            )
        } else {
            null
        }
    }

    /**
     * Save connected account credentials securely
     */
    suspend fun saveAccount(account: EmailAccount) {
        dataStore.edit { preferences ->
            preferences[connectedAccountKey] = account.email
            preferences[accessTokenKey] = account.accessToken
            preferences[refreshTokenKey] = account.refreshToken ?: ""
            preferences[providerKey] = account.provider.name
        }
    }

    /**
     * Disconnect the current account
     */
    suspend fun disconnectAccount() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    /**
     * Get recent emails from the connected account
     * Note: This requires proper OAuth authentication
     */
    suspend fun getRecentEmails(limit: Int = 50): List<Email> = withContext(Dispatchers.IO) {
        val account = connectedAccount.first() ?: return@withContext emptyList()

        when (account.provider) {
            EmailProvider.GMAIL -> {
                try {
                    val summaries = gmailOAuthManager.fetchRecentEmails(account.accessToken, limit)
                    summaries.map { summary ->
                        Email(
                            id = summary.id,
                            sender = summary.from,
                            subject = summary.subject,
                            preview = summary.snippet,
                            timestamp = summary.date,
                            isRead = false, // Gmail doesn't provide read status in summary
                            isInbox = true
                        )
                    }
                } catch (e: Exception) {
                    emptyList()
                }
            }
            EmailProvider.OUTLOOK -> {
                try {
                    val summaries = outlookOAuthManager.fetchRecentEmails(account.accessToken, limit)
                    summaries.map { summary ->
                        Email(
                            id = summary.id,
                            sender = summary.from,
                            subject = summary.subject,
                            preview = summary.snippet,
                            timestamp = summary.date,
                            isRead = false,
                            isInbox = true
                        )
                    }
                } catch (e: Exception) {
                    emptyList()
                }
            }
            EmailProvider.IMAP -> {
                emptyList() // Not implemented yet
            }
        }
    }

    /**
     * Send an email using the connected account
     */
    suspend fun sendEmail(to: String, subject: String, body: String): Result<Unit> = withContext(Dispatchers.IO) {
        val account = connectedAccount.first() ?: return@withContext Result.failure(Exception("No account connected"))

        try {
            when (account.provider) {
                EmailProvider.GMAIL -> {
                    gmailOAuthManager.sendEmail(account.accessToken, to, subject, body)
                    Result.success(Unit)
                }
                EmailProvider.OUTLOOK -> {
                    outlookOAuthManager.sendEmail(account.accessToken, to, subject, body)
                    Result.success(Unit)
                }
                EmailProvider.IMAP -> {
                    Result.failure(Exception("IMAP not implemented yet"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get recent emails from the device (legacy method for Gmail content provider)
     * Note: This is a fallback method and requires proper email client integration
     * Android doesn't provide a unified email API like SMS/CallLog
     */
    suspend fun getRecentEmailsFromDevice(limit: Int = 50): List<Email> = withContext(Dispatchers.IO) {
        if (!permissionManager.isPermissionGranted(com.secyra.ai.core.permissions.PermissionManager.Permissions.READ_EXTERNAL_STORAGE)) {
            return@withContext emptyList()
        }

        val emails = mutableListOf<Email>()

        try {
            // Try to access Gmail content provider if available
            // This is limited and may not work on all devices
            val gmailUri = Uri.parse("content://gmail-ls/conversations")
            val cursor: Cursor? = context.contentResolver.query(
                gmailUri,
                arrayOf("conversation_id", "subject", "snippet", "date", "labels"),
                null,
                null,
                "date DESC LIMIT $limit"
            )

            cursor?.use {
                val idIndex = it.getColumnIndex("conversation_id")
                val subjectIndex = it.getColumnIndex("subject")
                val snippetIndex = it.getColumnIndex("snippet")
                val dateIndex = it.getColumnIndex("date")
                val labelsIndex = it.getColumnIndex("labels")

                while (it.moveToNext()) {
                    val id = it.getString(idIndex) ?: ""
                    val subject = it.getString(subjectIndex) ?: "No Subject"
                    val snippet = it.getString(snippetIndex) ?: ""
                    val date = it.getLong(dateIndex)
                    val labels = it.getString(labelsIndex) ?: ""

                    val isRead = !labels.contains("^i") // ^i indicates inbox/unread in Gmail

                    emails.add(
                        Email(
                            id = id,
                            sender = extractSenderFromSnippet(snippet),
                            subject = subject,
                            preview = snippet,
                            timestamp = date,
                            isRead = isRead,
                            isInbox = labels.contains("^i")
                        )
                    )
                }
            }
        } catch (e: Exception) {
            // Gmail provider not available or no permission
            // Fallback to empty list
        }

        // If Gmail provider didn't work, try alternative methods
        if (emails.isEmpty()) {
            // Note: Android doesn't have a universal email API
            // This would require specific email client integrations
            // For now, return empty list with a message
        }

        emails
    }

    /**
     * Extract sender name from email snippet
     */
    private fun extractSenderFromSnippet(snippet: String): String {
        // Try to extract sender from common email formats
        val patterns = listOf(
            Regex("From: ([^\\n]+)"),
            Regex("<([^>]+)>"),
            Regex("([^\\s]+@[^\\s]+)")
        )

        for (pattern in patterns) {
            val match = pattern.find(snippet)
            if (match != null) {
                return match.groupValues[1].take(30)
            }
        }

        return "Unknown Sender"
    }

    /**
     * Check if email integration is available on this device
     */
    fun isEmailIntegrationAvailable(): Boolean {
        return try {
            val gmailUri = Uri.parse("content://gmail-ls/conversations")
            context.contentResolver.query(gmailUri, null, null, null, null)?.close()
            true
        } catch (e: Exception) {
            false
        }
    }
}
