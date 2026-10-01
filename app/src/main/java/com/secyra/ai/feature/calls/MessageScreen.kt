package com.secyra.ai.feature.calls

import android.content.Intent
import android.net.Uri
import android.telephony.SmsManager
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.secyra.ai.core.ui.theme.*

@Composable
fun MessageScreen(
    recipientName: String = "Unknown",
    recipientNumber: String = "",
    onClose: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var messageText by remember { mutableStateOf("") }
    val quickReplies = listOf(
        "I'll call you back later",
        "I am busy right now",
        "Can you call me in 5 minutes?",
        "I'm in a meeting",
        "I'll text you back"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SecyraBackground)
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
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = SecyraWhite,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Text(
                    text = "Message",
                    style = MaterialTheme.typography.titleMedium,
                    color = SecyraWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.width(48.dp))
            }

            // Recipient info
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(TiffanyBlue.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (recipientName.isNotEmpty()) recipientName.first().toString().uppercase() else "U",
                        style = MaterialTheme.typography.headlineMedium,
                        color = TiffanyBlue,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = recipientName,
                    style = MaterialTheme.typography.titleLarge,
                    color = SecyraWhite,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = recipientNumber,
                    style = MaterialTheme.typography.bodyMedium,
                    color = SecyraTextSecondary,
                    fontSize = 16.sp
                )
            }

            // Quick replies (2:1 ratio width)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(SecyraSurface, RoundedCornerShape(16.dp))
                    .border(1.dp, SecyraBorder, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(quickReplies) { reply ->
                        QuickReplyCard(
                            text = reply,
                            onClick = {
                                messageText = reply
                            }
                        )
                    }
                }
            }

            // Message input area (2:1 ratio)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(2f)
                    .background(SecyraSurface, RoundedCornerShape(16.dp))
                    .border(1.dp, TiffanyBlue, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        placeholder = { Text("Type your message...", color = SecyraTextSecondary) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = TiffanyBlue
                        ),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            color = SecyraWhite,
                            fontSize = 16.sp
                        ),
                        maxLines = 5
                    )

                    // Message controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Add attachment button
                        IconButton(
                            onClick = {
                                // Open file picker (placeholder)
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(TiffanyBlue, AzureBlue)
                                    )
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Attachment",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        // Mic button
                        IconButton(
                            onClick = {
                                // Voice input (placeholder)
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(TiffanyBlue, AzureBlue)
                                    )
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Voice Input",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        // Send button
                        Button(
                            onClick = {
                                if (messageText.isNotEmpty()) {
                                    try {
                                        // Send SMS directly using SmsManager
                                        val smsManager = context.getSystemService(android.content.Context.TELEPHONY_SERVICE) as android.telephony.SmsManager
                                        smsManager.sendTextMessage(
                                            recipientNumber,
                                            null,
                                            messageText,
                                            null,
                                            null
                                        )
                                        onClose()
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = RiskLow,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier.height(48.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Send,
                                    contentDescription = "Send",
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "Send",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuickReplyCard(
    text: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        TiffanyBlue.copy(alpha = 0.2f),
                        AzureBlue.copy(alpha = 0.2f)
                    )
                ),
                RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = SecyraWhite,
            fontSize = 14.sp
        )
    }
}
