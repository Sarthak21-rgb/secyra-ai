package com.secyra.ai.core.telecom

import android.content.Intent
import android.os.Bundle
import android.telecom.Call
import android.telecom.InCallService
import android.telecom.CallEndpoint
import com.secyra.ai.feature.calls.ActiveCallActivity
import com.secyra.ai.feature.calls.IncomingCallActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Secyra's InCallService for handling REAL calls.
 *
 * This service receives call events from Android Telecom when Secyra is the default phone app.
 * It's NOT a fake implementation - it integrates with actual Android Telecom.
 *
 * NOTE: For this to work, the user must explicitly set Secyra as the default phone app
 * through Android's RoleManager (ROLE_DIALER).
 */
@AndroidEntryPoint
class SecyraInCallService : InCallService() {

    @Inject
    lateinit var callManager: CallManager

    override fun onCallAdded(call: Call) {
        super.onCallAdded(call)
        // Register the real call with our CallManager
        callManager.setActiveCall(call)

        // Don't launch any screen directly - let notification handle it
        // CallManager will show appropriate notification based on call state
    }

    override fun onCallRemoved(call: Call) {
        super.onCallRemoved(call)
        // Clean up when call is removed
        if (callManager.activeCall.value == call) {
            callManager.cleanup()
        }
    }

    override fun onCallEndpointChanged(endpoint: CallEndpoint) {
        super.onCallEndpointChanged(endpoint)
        // Handle audio endpoint changes (speaker, bluetooth, etc.)
        // TODO: Update UI to reflect current endpoint
    }

    override fun onAvailableCallEndpointsChanged(endpoints: MutableList<CallEndpoint>) {
        super.onAvailableCallEndpointsChanged(endpoints)
        // Update available audio endpoints
        // TODO: Update UI to show available endpoints
    }

    override fun onMuteStateChanged(isMuted: Boolean) {
        super.onMuteStateChanged(isMuted)
        // Handle mute state changes from system
        // TODO: Update CallManager and UI
    }

    override fun onCanAddCallChanged(canAddCall: Boolean) {
        super.onCanAddCallChanged(canAddCall)
        // Handle availability of adding another call
        // TODO: Update UI to enable/disable Add Call button
    }

    override fun onConnectionEvent(call: Call, event: String, extras: Bundle) {
        super.onConnectionEvent(call, event, extras)
        // Handle custom connection events
    }
}
