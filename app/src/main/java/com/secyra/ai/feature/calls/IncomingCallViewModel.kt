package com.secyra.ai.feature.calls

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.telecom.TelecomManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.secyra.ai.core.telecom.CallManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class IncomingCallViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val callManager: CallManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(IncomingCallUiState())
    val uiState: StateFlow<IncomingCallUiState> = _uiState.asStateFlow()

    private val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as? TelecomManager

    fun setCallInfo(callerName: String, phoneNumber: String) {
        _uiState.value = _uiState.value.copy(
            callerName = callerName,
            phoneNumber = phoneNumber
        )
    }

    fun acceptCall() {
        _uiState.value = _uiState.value.copy(shouldAccept = true)
    }

    fun handleAccept() {
        viewModelScope.launch {
            try {
                // Accept the call using TelecomManager
                telecomManager?.let { tm ->
                    if (ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.ANSWER_PHONE_CALLS
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        // Find the ringing call and accept it
                        tm.acceptRingingCall()
                    }
                }

                // Navigate to active call screen
                _uiState.value = _uiState.value.copy(
                    shouldAccept = false,
                    shouldNavigateToActiveCall = true
                )
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = _uiState.value.copy(
                    shouldAccept = false,
                    error = "Failed to accept call: ${e.message}"
                )
            }
        }
    }

    fun declineCall() {
        _uiState.value = _uiState.value.copy(shouldDecline = true)
    }

    fun handleDecline() {
        viewModelScope.launch {
            try {
                // Decline the call using TelecomManager
                telecomManager?.let { tm ->
                    if (ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.ANSWER_PHONE_CALLS
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        // Find the ringing call and reject it
                        tm.endCall()
                    }
                }

                // Close the activity
                _uiState.value = _uiState.value.copy(
                    shouldDecline = false,
                    shouldClose = true
                )
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = _uiState.value.copy(
                    shouldDecline = false,
                    error = "Failed to decline call: ${e.message}"
                )
            }
        }
    }

    fun openMessage() {
        viewModelScope.launch {
            try {
                // Open SMS app with the phone number
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("sms:${_uiState.value.phoneNumber}")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)

                // Close the incoming call screen
                _uiState.value = _uiState.value.copy(shouldClose = true)
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = _uiState.value.copy(
                    error = "Failed to open message: ${e.message}"
                )
            }
        }
    }

    fun cleanup() {
        _uiState.value = IncomingCallUiState()
    }
}

data class IncomingCallUiState(
    val callerName: String = "Unknown",
    val phoneNumber: String = "",
    val shouldAccept: Boolean = false,
    val shouldDecline: Boolean = false,
    val shouldNavigateToActiveCall: Boolean = false,
    val shouldClose: Boolean = false,
    val error: String? = null
)
