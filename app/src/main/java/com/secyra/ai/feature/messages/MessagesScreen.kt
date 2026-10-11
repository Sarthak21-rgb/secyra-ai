package com.secyra.ai.feature.messages

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.secyra.ai.core.ui.theme.*

@Composable
fun MessagesScreen(
    modifier: Modifier = Modifier,
    viewModel: MessagesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(SecyraBackground)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // Messages secondary tabs
        item {
            MessagesTabRow(
                selectedTab = uiState.selectedTab,
                onTabSelected = { viewModel.onTabSelected(it) }
            )
        }

        // Content based on selected tab
        when (uiState.selectedTab) {
            MessageTab.All -> {
                if (uiState.conversations.isEmpty()) {
                    item {
                        EmptyState(
                            message = "No messages yet",
                            subMessage = "Your messages will appear here"
                        )
                    }
                } else {
                    items(uiState.conversations) { conversation ->
                        MessageConversationCard(
                            conversation = conversation,
                            onClick = { viewModel.onConversationClick(conversation) }
                        )
                    }
                }
            }
            MessageTab.Unread -> {
                if (uiState.unreadConversations.isEmpty()) {
                    item {
                        EmptyState(
                            message = "No unread messages",
                            subMessage = "All messages have been read"
                        )
                    }
                } else {
                    items(uiState.unreadConversations) { conversation ->
                        MessageConversationCard(
                            conversation = conversation,
                            onClick = { viewModel.onConversationClick(conversation) }
                        )
                    }
                }
            }
            MessageTab.Suspicious -> {
                if (uiState.suspiciousConversations.isEmpty()) {
                    item {
                        EmptyState(
                            message = "No suspicious messages",
                            subMessage = "Secyra hasn't detected any threats"
                        )
                    }
                } else {
                    items(uiState.suspiciousConversations) { conversation ->
                        MessageConversationCard(
                            conversation = conversation,
                            onClick = { viewModel.onConversationClick(conversation) }
                        )
                    }
                }
            }
            MessageTab.Blocked -> {
                if (uiState.blockedConversations.isEmpty()) {
                    item {
                        EmptyState(
                            message = "No blocked senders",
                            subMessage = "Blocked messages will appear here"
                        )
                    }
                } else {
                    items(uiState.blockedConversations) { conversation ->
                        MessageConversationCard(
                            conversation = conversation,
                            onClick = { viewModel.onConversationClick(conversation) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MessagesTabRow(
    selectedTab: MessageTab,
    onTabSelected: (MessageTab) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        MessageTab.values().forEach { tab ->
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onTabSelected(tab) }
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = tab.displayName,
                    color = if (selectedTab == tab) TiffanyBlue else SecyraTextMuted,
                    fontSize = 15.sp,
                    fontWeight = if (selectedTab == tab) FontWeight.SemiBold else FontWeight.Normal
                )
                if (selectedTab == tab) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .width(30.dp)
                            .height(3.dp)
                            .background(TiffanyBlue)
                    )
                }
            }
        }
    }
}

@Composable
fun MessageConversationCard(
    conversation: MessageConversation,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF080D12)
        ),
        border = BorderStroke(1.dp, Color(0xFF18232D))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(SecyraSurfaceElevated),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = conversation.initials,
                    color = SecyraWhite,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Name and message preview
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = conversation.displayName,
                    color = SecyraWhite,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold
                )
                val displaySnippet = if (conversation.isLatestMessageOutgoing) {
                    "You: ${conversation.snippet}"
                } else {
                    conversation.snippet
                }
                Text(
                    text = displaySnippet,
                    color = Color(0xFFA9B8CC),
                    fontSize = 14.sp,
                    maxLines = 2
                )
            }

            // Right column: timestamp, security badge, unread count, arrow
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = conversation.timestamp,
                    color = Color(0xFF77869B),
                    fontSize = 13.sp
                )
                MessageSecurityChip(securityLevel = conversation.securityLevel)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Unread count badge
                    if (conversation.unreadCount > 0) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(TiffanyBlue),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (conversation.unreadCount > 9) "9+" else conversation.unreadCount.toString(),
                                color = SecyraWhite,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    // Chevron arrow
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = Color(0xFF77869B),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun MessageSecurityChip(securityLevel: SecurityLevel) {
    val (backgroundColor, textColor, icon) = when (securityLevel) {
        SecurityLevel.Trusted -> Triple(
            Color(0xFF0A3D3B),
            TiffanyBlue,
            Icons.Default.Shield
        )
        SecurityLevel.Unknown -> Triple(
            SecyraSurfaceElevated,
            SecyraTextMuted,
            Icons.Default.Help
        )
        SecurityLevel.Suspicious -> Triple(
            Color(0xFF3D3A0A),
            Color(0xFFFFAA33),
            Icons.Default.Warning
        )
        SecurityLevel.Blocked -> Triple(
            Color(0xFF3D0A0A),
            RiskHigh,
            Icons.Default.Block
        )
    }

    Row(
        modifier = Modifier
            .height(36.dp)
            .border(
                width = 1.dp,
                color = textColor.copy(alpha = 0.3f),
                shape = RoundedCornerShape(18.dp)
            )
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = textColor,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = securityLevel.displayName,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun EmptyState(
    message: String,
    subMessage: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Email,
                contentDescription = null,
                tint = SecyraTextMuted,
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = message,
                color = SecyraWhite,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subMessage,
                color = SecyraTextSecondary,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

enum class MessageTab(val displayName: String) {
    All("All"),
    Unread("Unread"),
    Suspicious("Suspicious"),
    Blocked("Blocked")
}
