package com.secyra.ai.data.repository

import android.content.Context
import android.net.Uri
import android.provider.Telephony
import android.database.Cursor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for accessing real Android SMS data.
 * Uses the official Android SMS ContentProvider.
 */
@Singleton
class SmsRepository @Inject constructor(
    private val context: Context
) {

    /**
     * Get all SMS conversations from the device.
     */
    suspend fun getConversations(): List<SmsConversation> = withContext(Dispatchers.IO) {
        val conversations = mutableListOf<SmsConversation>()

        try {
            val uri = Telephony.Sms.Conversations.CONTENT_URI
            val projection = arrayOf(
                Telephony.Sms.Conversations.THREAD_ID,
                Telephony.Sms.Conversations.SNIPPET,
                Telephony.Sms.Conversations.MESSAGE_COUNT,
                Telephony.Sms.Conversations.DATE
            )

            val cursor: Cursor? = context.contentResolver.query(
                uri,
                projection,
                null,
                null,
                Telephony.Sms.Conversations.DATE + " DESC"
            )

            cursor?.use {
                val threadIdIndex = it.getColumnIndexOrThrow(Telephony.Sms.Conversations.THREAD_ID)
                val snippetIndex = it.getColumnIndexOrThrow(Telephony.Sms.Conversations.SNIPPET)
                val messageCountIndex = it.getColumnIndexOrThrow(Telephony.Sms.Conversations.MESSAGE_COUNT)
                val dateIndex = it.getColumnIndexOrThrow(Telephony.Sms.Conversations.DATE)

                while (it.moveToNext()) {
                    val threadId = it.getLong(threadIdIndex)
                    val snippet = it.getString(snippetIndex) ?: ""
                    val messageCount = it.getInt(messageCountIndex)
                    val timestamp = it.getLong(dateIndex)

                    // Get address from first message in thread
                    val address = getAddressForThread(threadId)

                    // Get unread count and latest message direction
                    val (unreadCount, isLatestOutgoing) = getThreadInfo(threadId)

                    conversations.add(
                        SmsConversation(
                            threadId = threadId,
                            address = address,
                            snippet = snippet,
                            messageCount = messageCount,
                            timestamp = timestamp,
                            unreadCount = unreadCount,
                            isLatestMessageOutgoing = isLatestOutgoing
                        )
                    )
                }
            }
        } catch (e: Exception) {
            // Handle permission denied or other errors
            e.printStackTrace()
        }

        conversations
    }

    /**
     * Get messages for a specific conversation thread.
     */
    suspend fun getMessagesForThread(threadId: Long): List<SmsMessage> = withContext(Dispatchers.IO) {
        val messages = mutableListOf<SmsMessage>()

        val uri = Telephony.Sms.CONTENT_URI
        val projection = arrayOf(
            Telephony.Sms._ID,
            Telephony.Sms.ADDRESS,
            Telephony.Sms.BODY,
            Telephony.Sms.DATE,
            Telephony.Sms.TYPE,
            Telephony.Sms.READ
        )

        val selection = "${Telephony.Sms.THREAD_ID} = ?"
        val selectionArgs = arrayOf(threadId.toString())

        val cursor: Cursor? = context.contentResolver.query(
            uri,
            projection,
            selection,
            selectionArgs,
            Telephony.Sms.DATE + " ASC"
        )

        cursor?.use {
            val idIndex = it.getColumnIndexOrThrow(Telephony.Sms._ID)
            val addressIndex = it.getColumnIndexOrThrow(Telephony.Sms.ADDRESS)
            val bodyIndex = it.getColumnIndexOrThrow(Telephony.Sms.BODY)
            val dateIndex = it.getColumnIndexOrThrow(Telephony.Sms.DATE)
            val typeIndex = it.getColumnIndexOrThrow(Telephony.Sms.TYPE)
            val readIndex = it.getColumnIndexOrThrow(Telephony.Sms.READ)

            while (it.moveToNext()) {
                val id = it.getLong(idIndex)
                val address = it.getString(addressIndex) ?: ""
                val body = it.getString(bodyIndex) ?: ""
                val date = it.getLong(dateIndex)
                val type = it.getInt(typeIndex)
                val read = it.getInt(readIndex) == 1

                messages.add(
                    SmsMessage(
                        id = id,
                        address = address,
                        body = body,
                        timestamp = date,
                        type = when (type) {
                            Telephony.Sms.MESSAGE_TYPE_INBOX -> SmsMessageType.INCOMING
                            Telephony.Sms.MESSAGE_TYPE_SENT -> SmsMessageType.OUTGOING
                            else -> SmsMessageType.UNKNOWN
                        },
                        isRead = read
                    )
                )
            }
        }

        messages
    }

    /**
     * Get unread conversations.
     */
    suspend fun getUnreadConversations(): List<SmsConversation> = withContext(Dispatchers.IO) {
        val conversations = mutableListOf<SmsConversation>()

        val uri = Telephony.Sms.CONTENT_URI
        val projection = arrayOf(
            Telephony.Sms.THREAD_ID,
            Telephony.Sms.ADDRESS,
            Telephony.Sms.BODY,
            Telephony.Sms.DATE
        )

        val selection = "${Telephony.Sms.READ} = ?"
        val selectionArgs = arrayOf("0")

        val cursor: Cursor? = context.contentResolver.query(
            uri,
            projection,
            selection,
            selectionArgs,
            Telephony.Sms.DATE + " DESC"
        )

        val seenThreadIds = mutableSetOf<Long>()

        cursor?.use {
            val threadIdIndex = it.getColumnIndexOrThrow(Telephony.Sms.THREAD_ID)
            val addressIndex = it.getColumnIndexOrThrow(Telephony.Sms.ADDRESS)
            val bodyIndex = it.getColumnIndexOrThrow(Telephony.Sms.BODY)
            val dateIndex = it.getColumnIndexOrThrow(Telephony.Sms.DATE)

            while (it.moveToNext()) {
                val threadId = it.getLong(threadIdIndex)
                val address = it.getString(addressIndex) ?: ""
                val body = it.getString(bodyIndex) ?: ""
                val date = it.getLong(dateIndex)

                if (threadId !in seenThreadIds) {
                    seenThreadIds.add(threadId)
                    conversations.add(
                        SmsConversation(
                            threadId = threadId,
                            address = address,
                            snippet = body,
                            messageCount = 1,
                            timestamp = date
                        )
                    )
                }
            }
        }

        conversations
    }

    private fun getAddressForThread(threadId: Long): String {
        val uri = Telephony.Sms.CONTENT_URI
        val projection = arrayOf(Telephony.Sms.ADDRESS)
        val selection = "${Telephony.Sms.THREAD_ID} = ?"
        val selectionArgs = arrayOf(threadId.toString())

        val cursor: Cursor? = context.contentResolver.query(
            uri,
            projection,
            selection,
            selectionArgs,
            Telephony.Sms.DATE + " DESC LIMIT 1"
        )

        cursor?.use {
            val addressIndex = it.getColumnIndexOrThrow(Telephony.Sms.ADDRESS)
            if (it.moveToFirst()) {
                return it.getString(addressIndex) ?: ""
            }
        }

        return ""
    }

    /**
     * Get thread info including unread count and latest message direction.
     */
    private fun getThreadInfo(threadId: Long): Pair<Int, Boolean> {
        val uri = Telephony.Sms.CONTENT_URI
        val projection = arrayOf(
            Telephony.Sms.READ,
            Telephony.Sms.TYPE
        )

        val selection = "${Telephony.Sms.THREAD_ID} = ?"
        val selectionArgs = arrayOf(threadId.toString())

        val cursor: Cursor? = context.contentResolver.query(
            uri,
            projection,
            selection,
            selectionArgs,
            Telephony.Sms.DATE + " DESC LIMIT 1"
        )

        var unreadCount = 0
        var isLatestOutgoing = false

        cursor?.use {
            val readIndex = it.getColumnIndexOrThrow(Telephony.Sms.READ)
            val typeIndex = it.getColumnIndexOrThrow(Telephony.Sms.TYPE)

            if (it.moveToFirst()) {
                isLatestOutgoing = it.getInt(typeIndex) == Telephony.Sms.MESSAGE_TYPE_SENT
            }

            it.moveToFirst()
            do {
                val isRead = it.getInt(readIndex) == 1
                if (!isRead) {
                    unreadCount++
                }
            } while (it.moveToNext())
        }

        return Pair(unreadCount, isLatestOutgoing)
    }
}

/**
 * Data model for an SMS conversation.
 */
data class SmsConversation(
    val threadId: Long,
    val address: String,
    val snippet: String,
    val messageCount: Int,
    val timestamp: Long,
    val unreadCount: Int = 0,
    val isLatestMessageOutgoing: Boolean = false
)

/**
 * Data model for an individual SMS message.
 */
data class SmsMessage(
    val id: Long,
    val address: String,
    val body: String,
    val timestamp: Long,
    val type: SmsMessageType,
    val isRead: Boolean
)

/**
 * Enum for SMS message types.
 */
enum class SmsMessageType {
    INCOMING,
    OUTGOING,
    UNKNOWN
}
