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

@Composable
fun ActiveCallScreen(
    callerName: String = "Unknown",
    phoneNumber: String = "",
    callState: CallState = CallState.Ringing,
    isMuted: Boolean = false,
    isSpeakerOn: Boolean = false,
    onEndCall: () -> Unit = {},
    onMute: () -> Unit = {},
    onSpeaker: () -> Unit = {},
    onKeypad: () -> Unit = {},
    onHold: () -> Unit = {},
    onAddCall: () -> Unit = {},
    onVideoCall: () -> Unit = {},
    onAISecurity: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        SecyraBackground,
                        SecyraDarkBlue,
                        Color(0xFF051020)
                    )
                )
            )
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header
            ActiveCallHeader(
                onMenuClick = { }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Caller Info
            CallerInfoSection(
                callerName = callerName,
                phoneNumber = phoneNumber,
                callState = callState
            )

            Spacer(modifier = Modifier.height(30.dp))

            // Security Status
            SecurityStatusSection(
                isProtected = true,
                onAISecurity = onAISecurity
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Call Controls
            CallControls(
                isMuted = false,
                isSpeakerOn = false,
                isOnHold = false,
                onMute = onMute,
                onSpeaker = onSpeaker,
                onKeypad = onKeypad,
                onHold = onHold,
                onAddCall = onAddCall,
                onVideoCall = onVideoCall,
                onAISecurity = onAISecurity,
                onEndCall = onEndCall
            )

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun ActiveCallHeader(
    onMenuClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { }) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Minimize",
                tint = SecyraWhite,
                modifier = Modifier.size(32.dp)
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_shield_logo),
                contentDescription = null,
                tint = TiffanyBlue,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "Secyra Secure Call",
                style = MaterialTheme.typography.bodyMedium,
                color = SecyraWhite,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
        }

        IconButton(onClick = onMenuClick) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "Menu",
                tint = SecyraWhite,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
fun CallerInfoSection(
    callerName: String,
    phoneNumber: String,
    callState: CallState
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Avatar with ring
        Box(
            modifier = Modifier.size(140.dp),
            contentAlignment = Alignment.Center
        ) {
            // Outer ring
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                TiffanyBlue.copy(alpha = 0.3f),
                                AzureBlue.copy(alpha = 0.1f),
                                Color.Transparent
                            )
                        )
                    )
            )
            // Inner circle
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(SecyraSurface)
                    .border(2.dp, TiffanyBlue, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = callerName.first().toString().uppercase(),
                    style = MaterialTheme.typography.displaySmall,
                    color = SecyraWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 48.sp
                )
            }
        }

        // Caller name
        Text(
            text = callerName,
            style = MaterialTheme.typography.headlineMedium,
            color = SecyraWhite,
            fontWeight = FontWeight.Bold,
            fontSize = 32.sp
        )

        // Phone number
        if (phoneNumber.isNotEmpty()) {
            Text(
                text = phoneNumber,
                style = MaterialTheme.typography.bodyLarge,
                color = SecyraTextSecondary,
                fontSize = 18.sp
            )
        }

        // Call state
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = when (callState) {
                    CallState.Ringing -> Icons.Default.Phone
                    CallState.Connected -> Icons.Default.CheckCircle
                    CallState.OnHold -> Icons.Default.Phone
                    CallState.Dialing -> Icons.Default.Phone
                },
                contentDescription = null,
                tint = TiffanyBlue,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = when (callState) {
                    CallState.Ringing -> "Ringing..."
                    CallState.Connected -> "Connected"
                    CallState.OnHold -> "On Hold"
                    CallState.Dialing -> "Calling..."
                },
                style = MaterialTheme.typography.bodyLarge,
                color = TiffanyBlue,
                fontWeight = FontWeight.Medium,
                fontSize = 18.sp
            )
        }
    }
}

@Composable
fun SecurityStatusSection(
    isProtected: Boolean,
    onAISecurity: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        colors = CardDefaults.cardColors(
            containerColor = SecyraSurface.copy(alpha = 0.8f)
        ),
        border = BorderStroke(1.dp, if (isProtected) TiffanyBlue else RiskHigh),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_shield_logo),
                    contentDescription = null,
                    tint = if (isProtected) TiffanyBlue else RiskHigh,
                    modifier = Modifier.size(32.dp)
                )
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "SECYRA SECURITY",
                        style = MaterialTheme.typography.labelMedium,
                        color = SecyraTextSecondary,
                        fontSize = 12.sp
                    )
                    Text(
                        text = if (isProtected) "Protected call" else "Potential risk detected",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isProtected) TiffanyBlue else RiskHigh,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
fun CallControls(
    isMuted: Boolean,
    isSpeakerOn: Boolean,
    isOnHold: Boolean,
    onMute: () -> Unit,
    onSpeaker: () -> Unit,
    onKeypad: () -> Unit,
    onHold: () -> Unit,
    onAddCall: () -> Unit,
    onVideoCall: () -> Unit,
    onAISecurity: () -> Unit,
    onEndCall: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Top row of controls
        Row(
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CallControlButton(
                icon = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                label = "Mute",
                isActive = isMuted,
                onClick = onMute
            )
            CallControlButton(
                icon = Icons.Default.VolumeUp,
                label = "Speaker",
                isActive = isSpeakerOn,
                onClick = onSpeaker
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
        }

        // Bottom row of controls
        Row(
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CallControlButton(
                icon = if (isOnHold) Icons.Default.PlayArrow else Icons.Default.Pause,
                label = "Hold",
                isActive = isOnHold,
                onClick = onHold
            )
            CallControlButton(
                icon = Icons.Default.Add,
                label = "Add",
                isActive = false,
                onClick = onAddCall
            )
            CallControlButton(
                icon = Icons.Default.Videocam,
                label = "Video",
                isActive = false,
                onClick = onVideoCall
            )
            // AI Security Button - Special Secyra feature
            AISecurityButton(onClick = onAISecurity)
        }

        // End Call Button
        EndCallButton(onClick = onEndCall)
    }
}

@Composable
fun CallControlButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .then(
                    if (isActive) {
                        Modifier.background(BrushColors.verticalGradient)
                    } else {
                        Modifier.background(SecyraSurface.copy(alpha = 0.8f))
                    }
                )
                .border(
                    1.dp,
                    if (isActive) Color.Transparent else SecyraBorder,
                    CircleShape
                )
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive) SecyraWhite else SecyraTextSecondary,
                modifier = Modifier.size(28.dp)
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = if (isActive) TiffanyBlue else SecyraTextMuted,
            fontSize = 13.sp
        )
    }
}

@Composable
fun AISecurityButton(
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(SecyraSurface.copy(alpha = 0.8f))
                .border(2.dp, TiffanyBlue, CircleShape)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_shield_logo),
                contentDescription = "AI Security",
                tint = TiffanyBlue,
                modifier = Modifier.size(28.dp)
            )
        }
        Text(
            text = "AI Security",
            style = MaterialTheme.typography.bodySmall,
            color = TiffanyBlue,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp
        )
    }
}

@Composable
fun EndCallButton(
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(RiskHigh)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.CallEnd,
                contentDescription = "End Call",
                tint = SecyraWhite,
                modifier = Modifier.size(32.dp)
            )
        }
        Text(
            text = "End",
            style = MaterialTheme.typography.bodySmall,
            color = RiskHigh,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )
    }
}

enum class CallState {
    Ringing,
    Connected,
    OnHold,
    Dialing
}
