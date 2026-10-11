package com.secyra.ai.data.repository

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import com.secyra.ai.core.permissions.PermissionManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class Email(
    val id: String,
    val sender: String,
    val subject: String,
    val preview: String,
    val timestamp: Long,
    val isRead: Boolean,
    val isInbox: Boolean = true
)

class EmailRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val permissionManager: PermissionManager
) {

    /**
     * Get recent emails from the device
     * Note: This requires proper email client integration and permissions
     * Android doesn't provide a unified email API like SMS/CallLog
     */
    suspend fun getRecentEmails(limit: Int = 50): List<Email> = withContext(Dispatchers.IO) {
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
