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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.secyra.ai.R
import com.secyra.ai.core.ui.theme.*

@Composable
fun MessagesScreen(
    modifier: Modifier = Modifier,
    viewModel: MessagesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SecyraBackground)
    ) {
        // Search Bar (when active)
        if (isSearchActive) {
            SearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                onClear = { searchQuery = ""; isSearchActive = false }
            )
        }

        // Filter Tabs
        MessagesTabRow(
            selectedTab = uiState.selectedTab,
            onTabSelected = { viewModel.onTabSelected(it) }
        )

        // Conversation List
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(start = 0.dp, top = 16.dp, end = 0.dp, bottom = 100.dp)
        ) {
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
                        val filteredConversations = if (searchQuery.isNotEmpty()) {
                            uiState.conversations.filter {
                                it.displayName.contains(searchQuery, ignoreCase = true) ||
                                it.snippet.contains(searchQuery, ignoreCase = true)
                            }
                        } else {
                            uiState.conversations
                        }
                        
                        if (filteredConversations.isEmpty() && searchQuery.isNotEmpty()) {
                            item {
                                EmptyState(
                                    message = "No results found",
                                    subMessage = "Try a different search term"
                                )
                            }
                        } else {
                            items(filteredConversations) { conversation ->
                                MessageConversationCard(
                                    conversation = conversation,
                                    onClick = { viewModel.onConversationClick(conversation) }
                                )
                            }
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
}

@Composable
fun MessagesHeader(
    onSearchClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            androidx.compose.foundation.Image(
                painter = painterResource(R.drawable.shield_logo),
                contentDescription = null,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "SECYRA",
                    style = MaterialTheme.typography.titleLarge,
                    color = SecyraWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                Text(
                    text = "AI POWERED SECURITY",
                    style = MaterialTheme.typography.bodySmall,
                    color = SecyraTextMuted,
                    fontSize = 12.sp
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            IconButton(
                onClick = onSearchClick,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = AzureBlue,
                    modifier = Modifier.size(24.dp)
                )
            }
            IconButton(
                onClick = onSettingsClick,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = TiffanyBlue,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .height(48.dp)
            .background(Color(0xFF080D12), RoundedCornerShape(24.dp))
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(AzureBlue, TiffanyBlue)
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            tint = SecyraTextMuted,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.weight(1f),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = SecyraWhite,
                focusedTextColor = SecyraWhite,
                unfocusedTextColor = SecyraWhite
            ),
            placeholder = {
                Text(
                    text = "Search messages...",
                    color = SecyraTextMuted,
                    fontSize = 14.sp
                )
            },
            textStyle = androidx.compose.ui.text.TextStyle(
                color = SecyraWhite,
                fontSize = 14.sp
            ),
            singleLine = true
        )
        if (query.isNotEmpty()) {
            IconButton(
                onClick = onClear,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = "Clear",
                    tint = SecyraTextMuted,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun MessagesTabRow(
    selectedTab: MessageTab,
    onTabSelected: (MessageTab) -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            MessageTab.values().forEach { tab ->
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onTabSelected(tab) }
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = tab.displayName,
                        color = if (selectedTab == tab) TiffanyBlue else SecyraTextMuted,
                        fontSize = 14.sp,
                        fontWeight = if (selectedTab == tab) FontWeight.SemiBold else FontWeight.Normal
                    )
                    if (selectedTab == tab) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .width(20.dp)
                                .height(2.dp)
                                .background(TiffanyBlue)
                        )
                    }
                }
            }
        }
        // Full-width divider
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(1.dp)
                .background(Color(0xFF17212D))
        )
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
            .height(114.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF03080E)
        ),
        border = BorderStroke(1.dp, Color(0xFF17212D))
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
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(SecyraSurfaceElevated),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = conversation.initials,
                    color = SecyraWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Middle section: Name row and message preview
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Name row with timestamp and count badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = conversation.displayName,
                        color = SecyraWhite,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Message count badge (only if > 1)
                        if (conversation.messageCount > 1) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(TiffanyBlue),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (conversation.messageCount > 9) "9+" else conversation.messageCount.toString(),
                                    color = Color.Black,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        
                        // Timestamp
                        Text(
                            text = conversation.timestamp,
                            color = Color(0xFF77869B),
                            fontSize = 12.sp
                        )
                    }
                }
                
                // Message preview
                val displaySnippet = if (conversation.isLatestMessageOutgoing) {
                    "You: ${conversation.snippet}"
                } else {
                    conversation.snippet
                }
                Text(
                    text = displaySnippet,
                    color = Color(0xFFA9B8CC),
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Right section: Security pill and chevron
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Security pill (only for non-saved contacts)
                if (!conversation.isSavedContact) {
                    MessageSecurityChip(securityLevel = conversation.securityLevel)
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

@Composable
fun MessageSecurityChip(securityLevel: SecurityLevel) {
    val (backgroundColor, textColor, icon) = when (securityLevel) {
        SecurityLevel.Trusted -> Triple(
            Color(0xFF0A3D3B),
            TiffanyBlue,
            Icons.Default.Shield
        )
        SecurityLevel.Unknown -> Triple(
            Color(0xFF1A2530),
            Color(0xFF6B9DBD),
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
            .height(32.dp)
            .border(
                width = 1.dp,
                color = textColor.copy(alpha = 0.3f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = textColor,
            modifier = Modifier.size(12.dp)
        )
        Text(
            text = securityLevel.displayName,
            color = textColor,
            fontSize = 11.sp,
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
            .padding(vertical = 60.dp),
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
