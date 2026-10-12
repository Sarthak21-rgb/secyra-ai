package com.secyra.ai.core.notifications

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.Person
import androidx.core.graphics.drawable.IconCompat
import com.secyra.ai.R
import com.secyra.ai.feature.calls.IncomingCallActivity
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages incoming call notifications for Secyra AI.
 * 
 * Provides both:
 * 1. Full-screen incoming call UI (when permitted)
 * 2. High-priority notification with Answer/Decline actions (fallback)
 */
@Singleton
class CallNotificationManager @Inject constructor(
    private val context: Context
) {
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    companion object {
        private const val CHANNEL_ID_INCOMING_CALLS = "incoming_calls"
        private const val CHANNEL_ID_CALL_SERVICE = "call_service"
        private const val NOTIFICATION_ID_INCOMING_CALL = 1001
        
        const val ACTION_ANSWER = "com.secyra.ai.ACTION_ANSWER"
        const val ACTION_DECLINE = "com.secyra.ai.ACTION_DECLINE"
        const val ACTION_MESSAGE = "com.secyra.ai.ACTION_MESSAGE"
        const val ACTION_CALLER_INFO = "com.secyra.ai.ACTION_CALLER_INFO"
        
        const val EXTRA_CALLER_NAME = "caller_name"
        const val EXTRA_PHONE_NUMBER = "phone_number"
        const val EXTRA_NOTIFICATION_ID = "notification_id"
    }

    init {
        createNotificationChannels()
    }

    /**
     * Create notification channels for call notifications
     */
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Incoming calls channel - high importance for full-screen
            val incomingCallChannel = NotificationChannel(
                CHANNEL_ID_INCOMING_CALLS,
                "Incoming Calls",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for incoming calls with Secyra AI protection"
                enableVibration(true)
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }

            // Call service channel - ongoing call notifications
            val callServiceChannel = NotificationChannel(
                CHANNEL_ID_CALL_SERVICE,
                "Active Call",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Ongoing call notifications"
                setShowBadge(false)
            }

            notificationManager.createNotificationChannel(incomingCallChannel)
            notificationManager.createNotificationChannel(callServiceChannel)
        }
    }

    /**
     * Show incoming call notification
     */
    fun showIncomingCallNotification(
        callerName: String,
        phoneNumber: String,
        riskLevel: String = "Unable to verify"
    ) {
        // Create intent for tapping notification body (caller info screen)
        val callerInfoIntent = Intent(context, CallNotificationReceiver::class.java).apply {
            action = ACTION_CALLER_INFO
            putExtra(EXTRA_CALLER_NAME, callerName)
            putExtra(EXTRA_PHONE_NUMBER, phoneNumber)
            putExtra(EXTRA_NOTIFICATION_ID, NOTIFICATION_ID_INCOMING_CALL)
        }
        val callerInfoPendingIntent = PendingIntent.getBroadcast(
            context,
            3,
            callerInfoIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Create full-screen intent for incoming call screen (only shown on accept)
        val fullScreenIntent = IncomingCallActivity.createIntent(callerName, phoneNumber).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val fullScreenPendingIntent = PendingIntent.getActivity(
            context,
            0,
            fullScreenIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Create Answer action
        val answerIntent = Intent(context, CallNotificationReceiver::class.java).apply {
            action = ACTION_ANSWER
            putExtra(EXTRA_CALLER_NAME, callerName)
            putExtra(EXTRA_PHONE_NUMBER, phoneNumber)
            putExtra(EXTRA_NOTIFICATION_ID, NOTIFICATION_ID_INCOMING_CALL)
        }
        val answerPendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            answerIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Create Decline action
        val declineIntent = Intent(context, CallNotificationReceiver::class.java).apply {
            action = ACTION_DECLINE
            putExtra(EXTRA_CALLER_NAME, callerName)
            putExtra(EXTRA_PHONE_NUMBER, phoneNumber)
            putExtra(EXTRA_NOTIFICATION_ID, NOTIFICATION_ID_INCOMING_CALL)
        }
        val declinePendingIntent = PendingIntent.getBroadcast(
            context,
            1,
            declineIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Create Message action
        val messageIntent = Intent(context, CallNotificationReceiver::class.java).apply {
            action = ACTION_MESSAGE
            putExtra(EXTRA_CALLER_NAME, callerName)
            putExtra(EXTRA_PHONE_NUMBER, phoneNumber)
            putExtra(EXTRA_NOTIFICATION_ID, NOTIFICATION_ID_INCOMING_CALL)
        }
        val messagePendingIntent = PendingIntent.getBroadcast(
            context,
            2,
            messageIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Build notification
        val notification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            buildCallStyleNotification(
                callerName,
                phoneNumber,
                riskLevel,
                callerInfoPendingIntent,
                answerPendingIntent,
                declinePendingIntent,
                messagePendingIntent
            )
        } else {
            buildLegacyNotification(
                callerName,
                phoneNumber,
                riskLevel,
                callerInfoPendingIntent,
                answerPendingIntent,
                declinePendingIntent,
                messagePendingIntent
            )
        }

        notificationManager.notify(NOTIFICATION_ID_INCOMING_CALL, notification)
    }

    /**
     * Build notification using CallStyle (Android 8.0+)
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun buildCallStyleNotification(
        callerName: String,
        phoneNumber: String,
        riskLevel: String,
        callerInfoIntent: PendingIntent,
        answerPendingIntent: PendingIntent,
        declinePendingIntent: PendingIntent,
        messagePendingIntent: PendingIntent
    ): Notification {
        val person = Person.Builder()
            .setName(callerName)
            .setIcon(IconCompat.createWithResource(context, R.drawable.ic_launcher_foreground))
            .build()

        val callStyle = NotificationCompat.CallStyle.forIncomingCall(
            person,
            declinePendingIntent,
            answerPendingIntent
        )

        return NotificationCompat.Builder(context, CHANNEL_ID_INCOMING_CALLS)
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setStyle(callStyle)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Incoming Call")
            .setContentText(phoneNumber)
            .setContentIntent(callerInfoIntent)
            .setAutoCancel(false)
            .addAction(
                android.R.drawable.ic_menu_send,
                "Message",
                messagePendingIntent
            )
            .build()
    }

    /**
     * Build legacy notification for pre-Android 8.0
     */
    private fun buildLegacyNotification(
        callerName: String,
        phoneNumber: String,
        riskLevel: String,
        callerInfoIntent: PendingIntent,
        answerPendingIntent: PendingIntent,
        declinePendingIntent: PendingIntent,
        messagePendingIntent: PendingIntent
    ): Notification {
        return NotificationCompat.Builder(context, CHANNEL_ID_INCOMING_CALLS)
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Incoming Call")
            .setContentText("$callerName - $phoneNumber")
            .setSubText("Risk: $riskLevel")
            .setContentIntent(callerInfoIntent)
            .setAutoCancel(false)
            .addAction(
                android.R.drawable.ic_menu_call,
                "Answer",
                answerPendingIntent
            )
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "Decline",
                declinePendingIntent
            )
            .addAction(
                android.R.drawable.ic_menu_send,
                "Message",
                messagePendingIntent
            )
            .build()
    }

    /**
     * Cancel incoming call notification
     */
    fun cancelIncomingCallNotification() {
        notificationManager.cancel(NOTIFICATION_ID_INCOMING_CALL)
    }

    /**
     * Show active call notification
     */
    fun showActiveCallNotification(
        callerName: String,
        phoneNumber: String
    ) {
        val intent = IncomingCallActivity.createIntent(callerName, phoneNumber).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_CALL_SERVICE)
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("In Call")
            .setContentText(callerName)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(NOTIFICATION_ID_INCOMING_CALL + 1, notification)
    }

    /**
     * Cancel active call notification
     */
    fun cancelActiveCallNotification() {
        notificationManager.cancel(NOTIFICATION_ID_INCOMING_CALL + 1)
    }
}
