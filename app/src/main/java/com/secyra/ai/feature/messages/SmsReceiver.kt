package com.secyra.ai.feature.messages

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.telephony.SmsMessage
import android.util.Log
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * BroadcastReceiver for receiving SMS messages.
 * This is required for Secyra to function as a default SMS app.
 */
@AndroidEntryPoint
class SmsReceiver : BroadcastReceiver() {

    @Inject
    lateinit var smsSecurityAnalyzer: SmsSecurityAnalyzer

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val extras = intent.extras
            if (extras != null) {
                val pdus = extras.get("pdus") as Array<ByteArray>
                val format = extras.getString("format")

                pdus.forEach { pdu ->
                    val smsMessage = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                        SmsMessage.createFromPdu(pdu, format)
                    } else {
                        @Suppress("DEPRECATION")
                        SmsMessage.createFromPdu(pdu)
                    }

                    smsMessage?.let { message ->
                        val phoneNumber = message.originatingAddress
                        val messageBody = message.messageBody
                        val timestamp = message.timestampMillis

                        Log.d("SmsReceiver", "Received SMS from $phoneNumber at $timestamp")

                        // Analyze the message for security threats in background
                        scope.launch {
                            smsSecurityAnalyzer.analyzeMessage(
                                phoneNumber = phoneNumber ?: "Unknown",
                                messageBody = messageBody ?: "",
                                timestamp = timestamp
                            )
                        }
                    }
                }
            }
        }
    }
}
