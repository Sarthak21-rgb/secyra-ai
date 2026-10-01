package com.secyra.ai.core.telecom

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import com.secyra.ai.R
import com.secyra.ai.feature.calls.ActiveCallActivity
import com.secyra.ai.feature.calls.IncomingCallActivity
import com.secyra.ai.feature.calls.MessageActivity
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages notifications for incoming and active calls.
 * This is NOT a fake implementation - it uses real Android Notification APIs.
 */
@Singleton
class CallNotificationManager @Inject constructor(
    private val context: Context
) {
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    
    companion object {
        private const val CHANNEL_ID = "secyra_call_channel"
        private const val CHANNEL_NAME = "Secyra Calls"
        private const val NOTIFICATION_ID = 1001
        private const val INCOMING_NOTIFICATION_ID = 1002
        private const val MISSED_CALL_NOTIFICATION_ID = 1003
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for incoming and active calls"
                setShowBadge(false)
                enableVibration(true)
                enableLights(true)
                lightColor = Color.parseColor("#0ABAB5")
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Show notification for incoming call with accept/decline buttons
     */
    fun showIncomingCallNotification(
        callerName: String,
        phoneNumber: String
    ) {
        val acceptIntent = Intent(context, IncomingCallActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("action", "accept")
            putExtra("callerName", callerName)
            putExtra("phoneNumber", phoneNumber)
        }
        val acceptPendingIntent = PendingIntent.getActivity(
            context,
            0,
            acceptIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val declineIntent = Intent(context, IncomingCallActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("action", "decline")
            putExtra("callerName", callerName)
            putExtra("phoneNumber", phoneNumber)
        }
        val declinePendingIntent = PendingIntent.getActivity(
            context,
            1,
            declineIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val messageIntent = Intent(context, MessageActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra("phoneNumber", phoneNumber)
            putExtra("callerName", callerName)
        }
        val messagePendingIntent = PendingIntent.getActivity(
            context,
            2,
            messageIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_shield_logo)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setOngoing(true)
            .setAutoCancel(false)
            .setFullScreenIntent(acceptPendingIntent, true)
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Incoming call from $callerName\n$phoneNumber")
                .setBigContentTitle("Secyra AI")
            )
            .addAction(
                android.R.drawable.ic_menu_call,
                "Accept",
                acceptPendingIntent
            )
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "Decline",
                declinePendingIntent
            )
            .addAction(
                android.R.drawable.ic_dialog_email,
                "Message",
                messagePendingIntent
            )
            .build()

        notificationManager.notify(INCOMING_NOTIFICATION_ID, notification)
    }

    /**
     * Show notification for active call
     */
    fun showActiveCallNotification(
        callerName: String,
        phoneNumber: String
    ) {
        val intent = Intent(context, ActiveCallActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_shield_logo)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setOngoing(true)
            .setAutoCancel(false)
            .setContentTitle("In Call")
            .setContentText("$callerName - $phoneNumber")
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    /**
     * Show notification for missed call
     */
    fun showMissedCallNotification(
        callerName: String,
        phoneNumber: String,
        timeAgo: String
    ) {
        val callBackIntent = Intent(Intent.ACTION_DIAL).apply {
            data = android.net.Uri.parse("tel:$phoneNumber")
        }
        val callBackPendingIntent = PendingIntent.getActivity(
            context,
            0,
            callBackIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val messageIntent = Intent(context, MessageActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra("phoneNumber", phoneNumber)
            putExtra("callerName", callerName)
        }
        val messagePendingIntent = PendingIntent.getActivity(
            context,
            1,
            messageIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_shield_logo)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setAutoCancel(true)
            .setContentTitle("Missed Call")
            .setContentText("$callerName - $timeAgo ago")
            .addAction(
                android.R.drawable.ic_menu_call,
                "Call Back",
                callBackPendingIntent
            )
            .addAction(
                android.R.drawable.ic_dialog_email,
                "Message",
                messagePendingIntent
            )
            .build()

        notificationManager.notify(MISSED_CALL_NOTIFICATION_ID, notification)
    }

    /**
     * Cancel incoming call notification
     */
    fun cancelIncomingCallNotification() {
        notificationManager.cancel(INCOMING_NOTIFICATION_ID)
    }

    /**
     * Cancel active call notification
     */
    fun cancelActiveCallNotification() {
        notificationManager.cancel(NOTIFICATION_ID)
    }

    /**
     * Cancel missed call notification
     */
    fun cancelMissedCallNotification() {
        notificationManager.cancel(MISSED_CALL_NOTIFICATION_ID)
    }

    /**
     * Cancel all call notifications
     */
    fun cancelAllNotifications() {
        notificationManager.cancel(INCOMING_NOTIFICATION_ID)
        notificationManager.cancel(NOTIFICATION_ID)
        notificationManager.cancel(MISSED_CALL_NOTIFICATION_ID)
    }
}
