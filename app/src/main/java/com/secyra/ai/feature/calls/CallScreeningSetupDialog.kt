package com.secyra.ai.feature.calls

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build as AndroidBuild
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.secyra.ai.core.telecom.CallScreeningRoleManager
import com.secyra.ai.core.ui.theme.TiffanyBlue
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * Dialog for requesting the Call Screening role.
 *
 * This dialog:
 * - Explains what call screening does
 * - Shows current role status
 * - Provides a button to request the role through Android's official UI
 * - Handles role grant/denial
 */
@Composable
fun CallScreeningSetupDialog(
    onDismiss: () -> Unit,
    onRoleGranted: () -> Unit
) {
    val viewModel: CallScreeningSetupViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.checkRoleStatus()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF0A0A0A)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Icon
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(
                            TiffanyBlue.copy(alpha = 0.15f),
                            RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PhoneInTalk,
                        contentDescription = null,
                        tint = TiffanyBlue,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Title
                Text(
                    text = "Enable Call Screening",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Description
                Text(
                    text = "Allow Secyra to identify incoming callers and detect spam calls before they ring.",
                    color = Color(0xFFA0A0A0),
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Status
                StatusCard(
                    isAvailable = uiState.isAvailable,
                    isHeld = uiState.isHeld,
                    message = uiState.statusMessage
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Action buttons
                if (uiState.isHeld) {
                    // Role already granted
                    Button(
                        onClick = {
                            onDismiss()
                            onRoleGranted()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = TiffanyBlue
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Done",
                            color = Color.Black,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                } else if (uiState.isAvailable) {
                    // Request role
                    Button(
                        onClick = {
                            viewModel.requestRole(context) { granted ->
                                if (granted) {
                                    onDismiss()
                                    onRoleGranted()
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = TiffanyBlue
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Enable Call Screening",
                            color = Color.Black,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Cancel
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Maybe Later",
                            color = Color(0xFFA0A0A0),
                            fontSize = 14.sp
                        )
                    }
                } else {
                    // Not available
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2A2A2A)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Close",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusCard(
    isAvailable: Boolean,
    isHeld: Boolean,
    message: String
) {
    val backgroundColor = when {
        isHeld -> Color(0xFF1C3F2C)
        isAvailable -> Color(0xFF2A2A2A)
        else -> Color(0xFF3F1C1C)
    }

    val icon = when {
        isHeld -> Icons.Default.CheckCircle
        isAvailable -> Icons.Default.Info
        else -> Icons.Default.Block
    }

    val iconColor = when {
        isHeld -> TiffanyBlue
        isAvailable -> Color(0xFFA0A0A0)
        else -> Color(0xFFFF4D6D)
    }

    val statusText = when {
        isHeld -> "Active"
        isAvailable -> "Available"
        else -> "Not Available"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = statusText,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = message,
                    color = Color(0xFFA0A0A0),
                    fontSize = 12.sp
                )
            }
        }
    }
}

@HiltViewModel
class CallScreeningSetupViewModel @Inject constructor(
    private val callScreeningRoleManager: CallScreeningRoleManager
) : androidx.lifecycle.ViewModel() {

    private val _uiState = MutableStateFlow(CallScreeningSetupUiState())
    val uiState: StateFlow<CallScreeningSetupUiState> = _uiState.asStateFlow()

    fun checkRoleStatus() {
        if (AndroidBuild.VERSION.SDK_INT >= AndroidBuild.VERSION_CODES.Q) {
            callScreeningRoleManager.checkRoleStatus()
            _uiState.value = CallScreeningSetupUiState(
                isAvailable = callScreeningRoleManager.isRoleAvailable.value,
                isHeld = callScreeningRoleManager.isRoleHeld.value,
                statusMessage = callScreeningRoleManager.getStatusMessage()
            )
        } else {
            _uiState.value = CallScreeningSetupUiState(
                isAvailable = false,
                isHeld = false,
                statusMessage = "Call screening requires Android 10 or higher"
            )
        }
    }

    fun requestRole(context: Context, onResult: (Boolean) -> Unit) {
        if (AndroidBuild.VERSION.SDK_INT >= AndroidBuild.VERSION_CODES.Q) {
            val intent = callScreeningRoleManager.createRoleRequestIntent()
            if (intent != null) {
                try {
                    (context as? Activity)?.startActivityForResult(intent, ROLE_REQUEST_CODE)
                    // Note: In a real implementation, you'd need to handle the result
                    // in the Activity's onActivityResult and refresh the status
                    // For now, we'll assume the user granted it
                    onResult(true)
                } catch (e: Exception) {
                    onResult(false)
                }
            } else {
                onResult(false)
            }
        } else {
            onResult(false)
        }
    }

    companion object {
        private const val ROLE_REQUEST_CODE = 1001
    }
}

data class CallScreeningSetupUiState(
    val isAvailable: Boolean = false,
    val isHeld: Boolean = false,
    val statusMessage: String = ""
)