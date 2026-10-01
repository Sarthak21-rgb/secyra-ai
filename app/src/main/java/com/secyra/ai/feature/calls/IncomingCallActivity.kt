package com.secyra.ai.feature.calls

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.secyra.ai.core.telecom.CallManager
import com.secyra.ai.core.ui.theme.SecyraTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Activity for displaying incoming call screen with answer/reject options.
 * This is launched by the InCallService when an incoming call is received.
 */
@AndroidEntryPoint
class IncomingCallActivity : ComponentActivity() {

    @Inject
    lateinit var callManager: CallManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val action = intent.getStringExtra("action")
        val callerName = intent.getStringExtra("callerName") ?: ""
        val phoneNumber = intent.getStringExtra("phoneNumber") ?: ""

        when (action) {
            "decline" -> {
                // Reject the call and close
                callManager.rejectCall()
                finishAffinity()
                return
            }
            "accept" -> {
                // Answer the call and switch to active call screen
                callManager.answerCall()
                startActivity(Intent(this, ActiveCallActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                })
                finish()
                return
            }
        }

        setContent {
            SecyraTheme {
                val currentCallerName by callManager.currentCallerName.collectAsState()
                val currentPhoneNumber by callManager.currentPhoneNumber.collectAsState()

                IncomingCallScreen(
                    callerName = if (currentCallerName.isNotEmpty()) currentCallerName else callerName.ifEmpty { "Unknown" },
                    phoneNumber = if (currentPhoneNumber.isNotEmpty()) currentPhoneNumber else phoneNumber,
                    onAnswer = {
                        // Answer the call and switch to active call screen
                        callManager.answerCall()
                        startActivity(Intent(this, ActiveCallActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        })
                        finish()
                    },
                    onReject = {
                        // Reject the call and close app
                        callManager.rejectCall()
                        finishAffinity()
                    },
                    onMessage = {
                        // Open message screen
                        startActivity(Intent(this, MessageActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            putExtra("phoneNumber", if (currentPhoneNumber.isNotEmpty()) currentPhoneNumber else phoneNumber)
                            putExtra("callerName", if (currentCallerName.isNotEmpty()) currentCallerName else callerName)
                        })
                    },
                    modifier = Modifier
                )
            }
        }
    }
}
