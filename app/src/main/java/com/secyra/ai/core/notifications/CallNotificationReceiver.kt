package com.secyra.ai.core.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.telecom.TelecomManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.secyra.ai.feature.calls.IncomingCallActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * BroadcastReceiver for handling call notification actions.
 * 
 * Handles:
 * - Answer call action
 * - Decline call action
 * - Message action
 */
@AndroidEntryPoint
class CallNotificationReceiver : BroadcastReceiver() {

    @Inject
    lateinit var callNotificationManager: CallNotificationManager

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_ANSWER -> handleAnswerCall(context, intent)
            ACTION_DECLINE -> handleDeclineCall(context, intent)
            ACTION_MESSAGE -> handleMessageAction(context, intent)
            ACTION_CALLER_INFO -> handleCallerInfoAction(context, intent)
        }
    }

    private fun handleAnswerCall(context: Context, intent: Intent) {
        try {
            val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as? TelecomManager
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
                ContextCompat.checkSelfPermission(context, android.Manifest.permission.ANSWER_PHONE_CALLS) 
                == android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                // Accept the ringing call
                telecomManager?.acceptRingingCall()
                
                // Cancel the notification
                callNotificationManager.cancelIncomingCallNotification()
                
                // The InCallService will launch the active call screen
            } else {
                // Fallback: launch IncomingCallActivity to request permission
                val callerName = intent.getStringExtra(EXTRA_CALLER_NAME) ?: "Unknown"
                val phoneNumber = intent.getStringExtra(EXTRA_PHONE_NUMBER) ?: ""
                
                val activityIntent = IncomingCallActivity.createIntent(callerName, phoneNumber)
                context.startActivity(activityIntent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Failed to answer call", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleDeclineCall(context: Context, intent: Intent) {
        try {
            val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as? TelecomManager
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
                ContextCompat.checkSelfPermission(context, android.Manifest.permission.ANSWER_PHONE_CALLS) 
                == android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                // Reject the ringing call
                telecomManager?.endCall()
                
                // Cancel the notification
                callNotificationManager.cancelIncomingCallNotification()
            } else {
                // Fallback: Just cancel notification and show toast
                callNotificationManager.cancelIncomingCallNotification()
                Toast.makeText(context, "Call declined", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // At least cancel the notification
            callNotificationManager.cancelIncomingCallNotification()
        }
    }

    private fun handleMessageAction(context: Context, intent: Intent) {
        try {
            val phoneNumber = intent.getStringExtra(EXTRA_PHONE_NUMBER) ?: ""
            
            // Open SMS app
            val smsIntent = Intent(Intent.ACTION_VIEW).apply {
                data = android.net.Uri.parse("sms:$phoneNumber")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(smsIntent)
            
            // Cancel the notification
            callNotificationManager.cancelIncomingCallNotification()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Failed to open messages", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleCallerInfoAction(context: Context, intent: Intent) {
        try {
            val callerName = intent.getStringExtra(EXTRA_CALLER_NAME) ?: "Unknown"
            val phoneNumber = intent.getStringExtra(EXTRA_PHONE_NUMBER) ?: ""
            
            // Launch caller info screen
            val infoIntent = com.secyra.ai.feature.calls.CallerInfoActivity.createIntent(callerName, phoneNumber)
            infoIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(infoIntent)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Failed to show caller info", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        const val ACTION_ANSWER = "com.secyra.ai.ACTION_ANSWER"
        const val ACTION_DECLINE = "com.secyra.ai.ACTION_DECLINE"
        const val ACTION_MESSAGE = "com.secyra.ai.ACTION_MESSAGE"
        const val ACTION_CALLER_INFO = "com.secyra.ai.ACTION_CALLER_INFO"
        
        const val EXTRA_CALLER_NAME = "caller_name"
        const val EXTRA_PHONE_NUMBER = "phone_number"
        const val EXTRA_NOTIFICATION_ID = "notification_id"
    }
}
