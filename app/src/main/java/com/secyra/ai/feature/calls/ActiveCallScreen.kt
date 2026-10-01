package com.secyra.ai.feature.calls

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.secyra.ai.R
import com.secyra.ai.core.ui.theme.*

enum class CallState {
    Dialing,
    Ringing,
    Connected,
    OnHold,
    Disconnected
}

@Composable
fun ActiveCallScreen(
    callerName: String = "Unknown",
    phoneNumber: String = "",
    callState: CallState = CallState.Ringing,
    isSpeakerOn: Boolean = false,
    isVideoCall: Boolean = false,
    callDuration: Long = 0L,
    onEndCall: () -> Unit = {},
    onSpeaker: () -> Unit = {},
    onKeypad: () -> Unit = {},
    onHold: () -> Unit = {},
    onAddCall: () -> Unit = {},
    onVideoCall: () -> Unit = {},
    onAISecurity: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val showKeypad = remember { mutableStateOf(false) }
    val isAISecurityActive = remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        TiffanyBlue.copy(alpha = 0.3f),
                        AzureBlue.copy(alpha = 0.3f)
                    )
                )
            )
    ) {
        if (showKeypad.value) {
            KeypadBottomSheet(
                onClose = { showKeypad.value = false },
                onNumberPress = { }
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = SecyraWhite,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Secyra Secure Call",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SecyraTextSecondary,
                        fontSize = 14.sp
                    )
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = SecyraWhite,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Caller info
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Avatar or Video preview
                    if (isVideoCall) {
                        VideoCallPreview()
                    } else {
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            TiffanyBlue.copy(alpha = 0.2f),
                                            AzureBlue.copy(alpha = 0.2f)
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_shield_logo),
                                contentDescription = null,
                                tint = TiffanyBlue,
                                modifier = Modifier.size(60.dp)
                            )
                        }
                    }

                    // Name and number
                    Text(
                        text = callerName,
                        style = MaterialTheme.typography.headlineMedium,
                        color = SecyraWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp
                    )
                    Text(
                        text = phoneNumber,
                        style = MaterialTheme.typography.bodyLarge,
                        color = SecyraTextSecondary,
                        fontSize = 18.sp
                    )

                    // Call state
                    Text(
                        text = when (callState) {
                            CallState.Dialing -> "Calling..."
                            CallState.Ringing -> "Ringing..."
                            CallState.Connected -> if (isVideoCall) "Video Call Connected" else "Connected"
                            CallState.OnHold -> "On Hold"
                            CallState.Disconnected -> "Call Ended"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = TiffanyBlue,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )

                    // Call duration
                    if (callState == CallState.Connected && callDuration > 0) {
                        val minutes = callDuration / 60
                        val seconds = callDuration % 60
                        Text(
                            text = String.format("%02d:%02d", minutes, seconds),
                            style = MaterialTheme.typography.bodyLarge,
                            color = SecyraWhite,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Call controls
                CallControls(
                    isSpeakerOn = isSpeakerOn,
                    isOnHold = callState == CallState.OnHold,
                    isVideoCall = isVideoCall,
                    isSpeaker = onSpeaker,
                    onKeypad = { showKeypad.value = true },
                    onHold = onHold,
                    onAddCall = onAddCall,
                    onVideoCall = onVideoCall,
                    onAISecurity = { 
                        isAISecurityActive.value = !isAISecurityActive.value
                        onAISecurity()
                    },
                    isAISecurityActive = isAISecurityActive.value,
                    onEndCall = onEndCall
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun CallControls(
    isSpeakerOn: Boolean,
    isOnHold: Boolean,
    isVideoCall: Boolean,
    isSpeaker: () -> Unit,
    onKeypad: () -> Unit,
    onHold: () -> Unit,
    onAddCall: () -> Unit,
    onVideoCall: () -> Unit,
    onAISecurity: () -> Unit,
    isAISecurityActive: Boolean,
    onEndCall: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Top row of controls - 4 buttons
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CallControlButton(
                icon = Icons.Default.VolumeUp,
                label = "Speaker",
                isActive = isSpeakerOn,
                onClick = { isSpeaker() }
            )
            CallControlButton(
                icon = Icons.Default.Dialpad,
                label = "Keypad",
                isActive = false,
                onClick = onKeypad
            )
            CallControlButton(
                icon = Icons.Default.Bluetooth,
                label = "Bluetooth",
                isActive = false,
                onClick = { }
            )
            CallControlButton(
                icon = if (isOnHold) Icons.Default.Call else Icons.Default.Pause,
                label = "Hold",
                isActive = isOnHold,
                onClick = onHold
            )
        }

        // Bottom row of controls - 4 buttons
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CallControlButton(
                icon = Icons.Default.Add,
                label = "Add",
                isActive = false,
                onClick = onAddCall
            )
            CallControlButton(
                icon = Icons.Default.Videocam,
                label = "Video",
                isActive = isVideoCall,
                onClick = onVideoCall
            )
            // AI Security Button - Special Secyra feature
            AISecurityButton(onClick = onAISecurity, isActive = isAISecurityActive)
            // End Call Button in the row
            EndCallButtonRow(onClick = onEndCall)
        }
    }
}

@Composable
fun CallControlButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isActive) {
        AzureBlue
    } else {
        SecyraSurface
    }
    val iconColor = if (isActive) Color.White else SecyraWhite
    val labelColor = if (isActive) Color.White else SecyraTextSecondary

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(backgroundColor)
                .border(
                    width = if (isActive) 0.dp else 1.dp,
                    color = SecyraBorder,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconColor,
                modifier = Modifier.size(28.dp)
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = labelColor,
            fontSize = 12.sp
        )
    }
}

@Composable
fun AISecurityButton(onClick: () -> Unit, isActive: Boolean) {
    val iconColor = if (isActive) TiffanyBlue else Color.White
    val textColor = if (isActive) Color(0xFF505050) else TiffanyBlue

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        if (isActive) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1A1A1A))
                    .border(
                        width = 1.dp,
                        color = TiffanyBlue,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_shield_logo),
                    contentDescription = "AI Security",
                    tint = iconColor,
                    modifier = Modifier.size(28.dp)
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(TiffanyBlue, AzureBlue)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_shield_logo),
                    contentDescription = "AI Security",
                    tint = iconColor,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
        Text(
            text = "AI Security",
            style = MaterialTheme.typography.bodySmall,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun EndCallButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(RiskHigh)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.CallEnd,
            contentDescription = "End Call",
            tint = Color.White,
            modifier = Modifier.size(36.dp)
        )
    }
}

@Composable
fun EndCallButtonRow(onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(RiskHigh),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.CallEnd,
                contentDescription = "End Call",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
        Text(
            text = "End",
            style = MaterialTheme.typography.bodySmall,
            color = RiskHigh,
            fontSize = 12.sp
        )
    }
}

@Composable
fun KeypadBottomSheet(
    onClose: () -> Unit,
    onNumberPress: (String) -> Unit
) {
    val enteredNumber = remember { mutableStateOf("") }
    val context = androidx.compose.ui.platform.LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            SecyraSurface,
                            SecyraBackground
                        )
                    )
                )
                .padding(horizontal = 24.dp, vertical = 32.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Top bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Keypad",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SecyraWhite,
                        fontSize = 14.sp
                    )
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = SecyraWhite,
                        modifier = Modifier
                            .size(24.dp)
                            .clickable(onClick = onClose)
                    )
                }

                // Number display
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = enteredNumber.value,
                        style = MaterialTheme.typography.headlineMedium,
                        color = SecyraWhite,
                        fontSize = 32.sp
                    )
                    if (enteredNumber.value.isNotEmpty()) {
                        Icon(
                            imageVector = Icons.Default.Backspace,
                            contentDescription = "Delete",
                            tint = SecyraTextSecondary,
                            modifier = Modifier
                                .size(24.dp)
                                .clickable(onClick = {
                                    enteredNumber.value = enteredNumber.value.dropLast(1)
                                })
                        )
                    }
                }

                // Keypad grid
                KeypadGrid(
                    onNumberPress = { number ->
                        enteredNumber.value += number
                        onNumberPress(number)
                    }
                )
            }
        }
    }
}

@Composable
fun KeypadGrid(onNumberPress: (String) -> Unit) {
    val numbers = listOf(
        "1", "2", "3",
        "4", "5", "6",
        "7", "8", "9",
        "*", "0", "#"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        for (i in numbers.indices step 3) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                for (j in 0..2) {
                    if (i + j < numbers.size) {
                        KeypadButton(
                            number = numbers[i + j],
                            onClick = { onNumberPress(numbers[i + j]) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun KeypadButton(
    number: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(SecyraSurface)
            .border(1.dp, SecyraBorder, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = number,
            style = MaterialTheme.typography.headlineMedium,
            color = SecyraWhite,
            fontSize = 32.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun VideoCallPreview() {
    Box(
        modifier = Modifier
            .size(120.dp)
            .clip(CircleShape)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        TiffanyBlue.copy(alpha = 0.3f),
                        AzureBlue.copy(alpha = 0.3f)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Videocam,
            contentDescription = "Video Call",
            tint = TiffanyBlue,
            modifier = Modifier.size(60.dp)
        )
        // Small indicator for video call
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(RiskHigh)
                .align(Alignment.BottomEnd),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.FiberManualRecord,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}
