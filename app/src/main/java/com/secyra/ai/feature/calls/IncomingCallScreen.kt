package com.secyra.ai.feature.calls

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.secyra.ai.core.ui.theme.*

@Composable
fun IncomingCallScreen(
    callerName: String = "Unknown",
    phoneNumber: String = "",
    onAccept: () -> Unit = {},
    onDecline: () -> Unit = {},
    onMessage: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        SecyraBackground,
                        Color(0xFF0A1520),
                        Color(0xFF051015)
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
            // Header with Secyra branding
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "SECYRA AI",
                    style = MaterialTheme.typography.titleMedium,
                    color = SecyraTextSecondary,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(60.dp))

            // Caller Avatar
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                AzureBlue.copy(alpha = 0.3f),
                                TiffanyBlue.copy(alpha = 0.1f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (callerName.isNotEmpty()) callerName.first().uppercaseChar().toString() else "?",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 48.sp
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Caller Name
            Text(
                text = callerName,
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Phone Number
            Text(
                text = phoneNumber,
                style = MaterialTheme.typography.bodyLarge,
                color = SecyraTextSecondary,
                fontSize = 18.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Security Badge
            Surface(
                modifier = Modifier
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF0A1520),
                border = androidx.compose.foundation.BorderStroke(1.dp, TiffanyBlue.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = TiffanyBlue,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Scanning...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TiffanyBlue,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(80.dp))

            // Call Action Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Message Button
                IncomingCallActionButton(
                    icon = Icons.Default.Message,
                    label = "Message",
                    backgroundColor = Color(0xFF1A2530),
                    iconColor = Color.White,
                    onClick = onMessage
                )

                // Decline Button
                IncomingCallActionButton(
                    icon = Icons.Default.CallEnd,
                    label = "Decline",
                    backgroundColor = Color(0xFFCC0000),
                    iconColor = Color.White,
                    onClick = onDecline
                )

                // Accept Button
                IncomingCallActionButton(
                    icon = Icons.Default.Call,
                    label = "Accept",
                    backgroundColor = TiffanyBlue,
                    iconColor = Color.White,
                    onClick = onAccept
                )
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
private fun IncomingCallActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    backgroundColor: Color,
    iconColor: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            onClick = onClick,
            modifier = Modifier.size(72.dp),
            shape = CircleShape,
            color = backgroundColor,
            shadowElevation = 8.dp
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = iconColor,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
