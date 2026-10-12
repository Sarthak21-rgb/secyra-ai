package com.secyra.ai.feature.calls

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.telecom.TelecomManager
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import com.secyra.ai.core.ui.theme.SecyraTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class IncomingCallActivity : ComponentActivity() {

    private val viewModel: IncomingCallViewModel by viewModels()

    private val requestCallPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.acceptCall()
        } else {
            viewModel.declineCall()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set window flags for incoming call screen
        window.addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )

        // Get call information from intent
        val callerName = intent.getStringExtra(EXTRA_CALLER_NAME) ?: "Unknown"
        val phoneNumber = intent.getStringExtra(EXTRA_PHONE_NUMBER) ?: ""

        viewModel.setCallInfo(callerName, phoneNumber)

        setContent {
            SecyraTheme {
                IncomingCallScreenWrapper(
                    viewModel = viewModel,
                    onPermissionRequest = { requestCallPermission() }
                )
            }
        }
    }

    private fun requestCallPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ANSWER_PHONE_CALLS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestCallPermissionLauncher.launch(Manifest.permission.ANSWER_PHONE_CALLS)
        } else {
            viewModel.acceptCall()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.cleanup()
    }

    companion object {
        const val EXTRA_CALLER_NAME = "caller_name"
        const val EXTRA_PHONE_NUMBER = "phone_number"

        fun createIntent(
            callerName: String,
            phoneNumber: String
        ): Intent {
            return Intent().apply {
                putExtra(EXTRA_CALLER_NAME, callerName)
                putExtra(EXTRA_PHONE_NUMBER, phoneNumber)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
        }
    }
}

@Composable
private fun IncomingCallScreenWrapper(
    viewModel: IncomingCallViewModel,
    onPermissionRequest: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.shouldAccept) {
        if (uiState.shouldAccept) {
            viewModel.handleAccept()
        }
    }

    LaunchedEffect(uiState.shouldDecline) {
        if (uiState.shouldDecline) {
            viewModel.handleDecline()
        }
    }

    IncomingCallScreen(
        callerName = uiState.callerName,
        phoneNumber = uiState.phoneNumber,
        onAccept = {
            onPermissionRequest()
        },
        onDecline = {
            viewModel.declineCall()
        },
        onMessage = {
            viewModel.openMessage()
        }
    )
}
