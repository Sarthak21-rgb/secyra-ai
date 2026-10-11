package com.secyra.ai.feature.calls

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.secyra.ai.core.telecom.CallManager
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SecyraTheme {
                val callState by callManager.callState.collectAsState()
                val isMuted by callManager.isMuted.collectAsState()
                val isSpeakerOn by callManager.isSpeakerOn.collectAsState()

                ActiveCallScreen(
                    callerName = "Unknown",
                    phoneNumber = "",
                    callState = when (callState) {
                        com.secyra.ai.core.telecom.CallState.Dialing -> com.secyra.ai.feature.calls.CallState.Dialing
                        com.secyra.ai.core.telecom.CallState.Ringing -> com.secyra.ai.feature.calls.CallState.Ringing
                        com.secyra.ai.core.telecom.CallState.Connected -> com.secyra.ai.feature.calls.CallState.Connected
                        com.secyra.ai.core.telecom.CallState.OnHold -> com.secyra.ai.feature.calls.CallState.OnHold
                        com.secyra.ai.core.telecom.CallState.Disconnecting -> com.secyra.ai.feature.calls.CallState.Dialing
                        com.secyra.ai.core.telecom.CallState.Disconnected -> com.secyra.ai.feature.calls.CallState.Dialing
                        com.secyra.ai.core.telecom.CallState.Idle -> com.secyra.ai.feature.calls.CallState.Dialing
                    },
                    isMuted = isMuted,
                    isSpeakerOn = isSpeakerOn,
                    onEndCall = { 
                        callManager.disconnectCall()
                        finishAffinity() // Close activity and app completely
                    },
                    onMute = { callManager.setMuted(!isMuted) },
                    onSpeaker = { callManager.setSpeaker(!isSpeakerOn) },
                    onKeypad = { },
                    onHold = { callManager.holdCall() },
                    onAddCall = { },
                    onVideoCall = { },
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
