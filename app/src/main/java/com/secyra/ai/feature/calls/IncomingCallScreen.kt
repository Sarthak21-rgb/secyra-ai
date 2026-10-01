package com.secyra.ai.feature.calls

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.secyra.ai.R
import com.secyra.ai.core.ui.theme.*
import kotlin.math.abs

@Composable
fun IncomingCallScreen(
    callerName: String = "Unknown",
    phoneNumber: String = "",
    onAnswer: () -> Unit = {},
    onReject: () -> Unit = {},
    onMessage: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var swipeOffset by remember { mutableStateOf(0f) }
    var isSwiped by remember { mutableStateOf(false) }

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
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        if (swipeOffset > 200) {
                            onAnswer()
                            isSwiped = true
                        } else if (swipeOffset < -200) {
                            onReject()
                            isSwiped = true
                        }
                        swipeOffset = 0f
                    }
                ) { change, dragAmount ->
                    change.consume()
                    swipeOffset += dragAmount
                }
            }
    ) {
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
                    text = "Incoming Call",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SecyraWhite,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Menu",
                    tint = SecyraWhite,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Caller info
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    TiffanyBlue.copy(alpha = 0.4f),
                                    AzureBlue.copy(alpha = 0.4f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_shield_logo),
                        contentDescription = null,
                        tint = TiffanyBlue,
                        modifier = Modifier.size(70.dp)
                    )
                }

                // Name and number
                Text(
                    text = callerName,
                    style = MaterialTheme.typography.headlineMedium,
                    color = SecyraWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp
                )
                Text(
                    text = phoneNumber,
                    style = MaterialTheme.typography.bodyLarge,
                    color = SecyraWhite.copy(alpha = 0.8f),
                    fontSize = 20.sp
                )

                // Swipe hint
                Text(
                    text = "Swipe right to answer",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SecyraWhite.copy(alpha = 0.6f),
                    fontSize = 14.sp
                )
            }

            // Call controls
            Row(
                horizontalArrangement = Arrangement.spacedBy(32.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Reject button
                IncomingCallButton(
                    icon = Icons.Default.CallEnd,
                    label = "Reject",
                    color = RiskHigh,
                    onClick = onReject
                )

                // Message button
                IncomingCallButton(
                    icon = Icons.Default.Message,
                    label = "Message",
                    color = AzureBlue,
                    onClick = onMessage
                )

                // Answer button
                IncomingCallButton(
                    icon = Icons.Default.Phone,
                    label = "Answer",
                    color = RiskLow,
                    onClick = onAnswer
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Swipe indicator
        if (abs(swipeOffset) > 50) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(
                        if (swipeOffset > 0) RiskLow else RiskHigh
                    )
            )
        }
    }
}

@Composable
fun IncomingCallButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(color),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(36.dp)
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = SecyraWhite,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
