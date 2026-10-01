package com.secyra.ai.core.telecom

import android.os.Bundle
import android.telecom.Connection
import android.telecom.ConnectionRequest
import android.telecom.ConnectionService
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Secyra's ConnectionService for handling outgoing calls.
 *
 * This service is required for a phone app to handle outgoing calls
 * when it's set as the default dialer.
 * 
 * TEMPORARILY DISABLED due to API compatibility issues.
 * Will be re-enabled once proper Connection implementation is complete.
 */
/*
@AndroidEntryPoint
class SecyraConnectionService : ConnectionService() {

    @Inject
    lateinit var callManager: CallManager

    override fun onCreateOutgoingConnection(
        connectionManagerPhoneAccount: PhoneAccountHandle,
        request: ConnectionRequest
    ): Connection {
        // Create and configure the connection for outgoing call
        // This will be handled by CallManager
        val connection = SecyraConnection()
        connection.setAddress(request.address, TelecomManager.PRESENTATION_ALLOWED)
        connection.setInitializing()
        connection.setActive()
        return connection
    }

    override fun onCreateIncomingConnection(
        connectionManagerPhoneAccount: PhoneAccountHandle,
        request: ConnectionRequest
    ): Connection {
        // Handle incoming connection
        val connection = SecyraConnection()
        connection.setAddress(request.address, TelecomManager.PRESENTATION_ALLOWED)
        connection.setInitializing()
        connection.setRinging()
        return connection
    }
}

class SecyraConnection : Connection() {
    override fun onShowIncomingCallUi() {
        // TODO: Show Secyra's incoming call UI
    }

    override fun onAnswer() {
        // TODO: Handle answer
        setActive()
    }

    override fun onReject() {
        // TODO: Handle reject
        setDisconnected(android.telecom.DisconnectReason.REJECTED)
        destroy()
    }

    override fun onDisconnect() {
        // TODO: Handle disconnect
        setDisconnected(android.telecom.DisconnectReason.LOCAL)
        destroy()
    }
}
*/
