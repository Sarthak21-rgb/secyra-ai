package com.secyra.ai.feature.calls

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.secyra.ai.core.telecom.CallManager
import com.secyra.ai.core.telecom.CallState
import com.secyra.ai.core.ui.theme.SecyraTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Activity for displaying the active call UI.
 * This is launched by the InCallService when a call becomes active.
 */
@AndroidEntryPoint
class ActiveCallActivity : ComponentActivity() {

    @Inject
    lateinit var callManager: CallManager

    companion object {
        fun getIntent(context: android.content.Context, phoneNumber: String): android.content.Intent {
            return android.content.Intent(context, ActiveCallActivity::class.java).apply {
                putExtra("phoneNumber", phoneNumber)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            SecyraTheme {
                val callState by callManager.callState.collectAsState()
                val isSpeakerOn by callManager.isSpeakerOn.collectAsState()
                val isOnHold by callManager.isOnHold.collectAsState()
                val isVideoCall by callManager.isVideoCall.collectAsState()
                val callDuration by callManager.callDuration.collectAsState()
                val currentCallerName by callManager.currentCallerName.collectAsState()
                val currentPhoneNumber by callManager.currentPhoneNumber.collectAsState()

                // Close activity when call is disconnected
                LaunchedEffect(callState) {
                    if (callState == CallState.Disconnected) {
                        // Close app and return to home screen
                        finishAffinity()
                    }
                }

                ActiveCallScreen(
                    callerName = currentCallerName.ifEmpty { "Unknown" },
                    phoneNumber = currentPhoneNumber,
                    callState = when (callState) {
                        CallState.Dialing -> com.secyra.ai.feature.calls.CallState.Dialing
                        CallState.Ringing -> com.secyra.ai.feature.calls.CallState.Ringing
                        CallState.Connected -> com.secyra.ai.feature.calls.CallState.Connected
                        CallState.OnHold -> com.secyra.ai.feature.calls.CallState.OnHold
                        CallState.Disconnecting -> com.secyra.ai.feature.calls.CallState.Dialing
                        CallState.Disconnected -> com.secyra.ai.feature.calls.CallState.Dialing
                        CallState.Idle -> com.secyra.ai.feature.calls.CallState.Dialing
                    },
                    isSpeakerOn = isSpeakerOn,
                    isVideoCall = isVideoCall,
                    callDuration = callDuration,
                    onEndCall = { callManager.disconnectCall() },
                    onSpeaker = { callManager.setSpeaker(!isSpeakerOn) },
                    onKeypad = { },
                    onHold = { callManager.holdCall() },
                    onAddCall = { },
                    onVideoCall = { 
                        if (isVideoCall) {
                            callManager.disableVideoCall()
                        } else {
                            callManager.enableVideoCall()
                        }
                    },
                    onAISecurity = { },
                    modifier = Modifier
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clean up when activity is destroyed
        callManager.cleanup()
    }
}
