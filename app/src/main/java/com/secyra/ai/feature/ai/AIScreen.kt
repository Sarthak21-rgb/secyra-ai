package com.secyra.ai.feature.ai

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.secyra.ai.core.ui.theme.*
import com.secyra.ai.navigation.Screen

@Composable
fun AIScreen(
    modifier: Modifier = Modifier,
    viewModel: AIViewModel = hiltViewModel(),
    onNavigate: (Screen) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    // Handle navigation to Pro subscription
    LaunchedEffect(uiState.shouldNavigateToPro) {
        if (uiState.shouldNavigateToPro) {
            onNavigate(Screen.ProSubscription)
            viewModel.onNavigationHandled()
        }
    }

    if (!uiState.isProUser) {
        // Show Pro gate for free users
        ProGateScreen(
            onUpgrade = { viewModel.navigateToProSubscription() }
        )
    } else {
        // Show AI Chat for Pro users
        AIChatScreen(
            uiState = uiState,
            onSendMessage = { viewModel.sendMessage(it) },
            onClearChat = { viewModel.clearChat() },
            onInputChange = { viewModel.onInputChange(it) }
        )
    }
}

@Composable
fun ProGateScreen(onUpgrade: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SecyraBackground)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = null,
            tint = TiffanyBlue,
            modifier = Modifier.size(80.dp)
        )

        Text(
            text = "SECYRA AI",
            style = MaterialTheme.typography.headlineLarge,
            color = SecyraWhite,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "AI-Powered Security Analysis",
            style = MaterialTheme.typography.titleMedium,
            color = SecyraTextSecondary
        )

        Card(
            colors = CardDefaults.cardColors(
                containerColor = SecyraSurfaceElevated
            ),
            border = BorderStroke(1.dp, SecyraBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Pro Features",
                    style = MaterialTheme.typography.titleMedium,
                    color = SecyraWhite,
                    fontWeight = FontWeight.Bold
                )

                AIFeature(
                    icon = Icons.Default.Info,
                    title = "AI Security Chat",
                    description = "Talk to Secyra AI about security concerns"
                )
                AIFeature(
                    icon = Icons.Default.Phone,
                    title = "Voice AI",
                    description = "Voice interaction with security assistant"
                )
                AIFeature(
                    icon = Icons.Default.Info,
                    title = "Screen Security Session",
                    description = "Real-time AI screen analysis"
                )
                AIFeature(
                    icon = Icons.Default.Info,
                    title = "Screenshot Analysis",
                    description = "AI-powered image security analysis"
                )
            }
        }

        Button(
            onClick = onUpgrade,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = TiffanyBlue,
                contentColor = SecyraWhite
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Upgrade to Pro")
        }

        Text(
            text = "₹149/month or ₹1,499/year",
            style = MaterialTheme.typography.bodySmall,
            color = SecyraTextMuted
        )
    }
}

@Composable
fun AIFeature(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = TiffanyBlue,
            modifier = Modifier.size(24.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = SecyraWhite,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = SecyraTextSecondary
            )
        }
    }
}

@Composable
fun AIChatScreen(
    uiState: AIUiState,
    onSendMessage: (String) -> Unit,
    onClearChat: () -> Unit,
    onInputChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SecyraBackground)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SecyraSurface)
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = TiffanyBlue,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "SECYRA AI",
                        style = MaterialTheme.typography.titleMedium,
                        color = SecyraWhite,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Security Assistant",
                        style = MaterialTheme.typography.bodySmall,
                        color = SecyraTextSecondary
                    )
                }
            }
            IconButton(onClick = onClearChat) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Clear chat",
                    tint = SecyraTextSecondary
                )
            }
        }

        // Chat messages
        if (uiState.messages.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = SecyraTextMuted,
                        modifier = Modifier.size(64.dp)
                    )
                    Text(
                        text = "Start a conversation",
                        style = MaterialTheme.typography.titleMedium,
                        color = SecyraTextSecondary
                    )
                    Text(
                        text = "Ask about security, scams, or suspicious content",
                        style = MaterialTheme.typography.bodySmall,
                        color = SecyraTextMuted
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.messages) { message ->
                    ChatMessageItem(message)
                }
            }
        }

        // Error message
        uiState.error?.let { error ->
            Card(
                modifier = Modifier.padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = RiskCritical.copy(alpha = 0.2f)
                ),
                border = BorderStroke(1.dp, RiskCritical)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = RiskCritical,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodyMedium,
                        color = SecyraWhite
                    )
                }
            }
        }

        // Input area
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SecyraSurface)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = uiState.inputText,
                onValueChange = onInputChange,
                placeholder = { Text("Ask about security...") },
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TiffanyBlue,
                    unfocusedBorderColor = SecyraBorder,
                    cursorColor = TiffanyBlue,
                    focusedTextColor = SecyraWhite,
                    unfocusedTextColor = SecyraWhite
                ),
                shape = RoundedCornerShape(24.dp)
            )
            
            IconButton(
                onClick = { onSendMessage(uiState.inputText) },
                enabled = uiState.inputText.isNotBlank() && !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = TiffanyBlue,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send",
                        tint = TiffanyBlue
                    )
                }
            }
        }
    }
}

@Composable
fun ChatMessageItem(message: ChatMessage) {
    val isUser = message.role == "user"
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isUser) {
                    TiffanyBlue.copy(alpha = 0.2f)
                } else {
                    SecyraSurfaceElevated
                }
            ),
            border = BorderStroke(
                1.dp,
                if (isUser) TiffanyBlue else SecyraBorder
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp).widthIn(max = 280.dp)
            ) {
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = SecyraWhite
                )
                Text(
                    text = formatTimestamp(message.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = SecyraTextMuted
                )
            }
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    // Simple timestamp formatting
    val date = java.util.Date(timestamp)
    return java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(date)
}


