package com.secyra.ai.feature.contacts

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.secyra.ai.core.ui.theme.*
import com.secyra.ai.feature.calls.CallItem

@Composable
fun ContactProfileScreen(
    call: CallItem,
    onBack: () -> Unit,
    onCall: (String) -> Unit,
    onMessage: (String) -> Unit,
    onBlock: (String) -> Unit,
    onUnblock: (String) -> Unit,
    onReport: (String) -> Unit,
    onTrust: (String) -> Unit
) {
    var showBlockDialog by remember { mutableStateOf(false) }
    var showUnblockDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SecyraBackground)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // Top App Bar
            item {
                ContactProfileTopBar(
                    onBack = onBack,
                    onSearch = { },
                    onMenu = { }
                )
            }

            // Contact Identity Section
            item {
                ContactIdentitySection(call = call)
            }

            // Main Action Buttons
            item {
                MainActionButtons(
                    onCall = { onCall(call.phoneNumber) },
                    onVideoCall = { },
                    onMessage = { onMessage(call.phoneNumber) },
                    onMore = { }
                )
            }

            // Contact Details Card
            item {
                ContactDetailsCard(call = call)
            }

            // Security Analysis Card
            item {
                SecurityAnalysisCard(call = call)
            }

            // Call History Card
            item {
                CallHistoryCard(call = call)
            }

            // Message History Card
            item {
                MessageHistoryCard(call = call)
            }

            // Block/Unblock Action
            item {
                BlockContactAction(
                    isBlocked = call.isSpam,
                    onBlock = { showBlockDialog = true },
                    onUnblock = { showUnblockDialog = true }
                )
            }
        }
    }

    // Block Confirmation Dialog
    if (showBlockDialog) {
        AlertDialog(
            onDismissRequest = { showBlockDialog = false },
            title = {
                Text(
                    text = "Block Contact",
                    color = SecyraWhite,
                    fontWeight = FontWeight.SemiBold
                )
            },
            text = {
                Text(
                    text = "Block calls from ${call.name} (${call.phoneNumber})?",
                    color = SecyraTextSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onBlock(call.phoneNumber)
                    showBlockDialog = false
                }) {
                    Text("Block", color = RiskHigh)
                }
            },
            dismissButton = {
                TextButton(onClick = { showBlockDialog = false }) {
                    Text("Cancel", color = SecyraTextSecondary)
                }
            },
            containerColor = SecyraSurface
        )
    }

    // Unblock Confirmation Dialog
    if (showUnblockDialog) {
        AlertDialog(
            onDismissRequest = { showUnblockDialog = false },
            title = {
                Text(
                    text = "Unblock Contact",
                    color = SecyraWhite,
                    fontWeight = FontWeight.SemiBold
                )
            },
            text = {
                Text(
                    text = "Unblock ${call.name} (${call.phoneNumber})?",
                    color = SecyraTextSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onUnblock(call.phoneNumber)
                    showUnblockDialog = false
                }) {
                    Text("Unblock", color = TiffanyBlue)
                }
            },
            dismissButton = {
                TextButton(onClick = { showUnblockDialog = false }) {
                    Text("Cancel", color = SecyraTextSecondary)
                }
            },
            containerColor = SecyraSurface
        )
    }
}

@Composable
fun ContactProfileTopBar(
    onBack: () -> Unit,
    onSearch: () -> Unit,
    onMenu: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = SecyraWhite,
                modifier = Modifier.size(24.dp)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            IconButton(onClick = onSearch) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = SecyraTextSecondary,
                    modifier = Modifier.size(24.dp)
                )
            }
            IconButton(onClick = onMenu) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More",
                    tint = SecyraTextSecondary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun ContactIdentitySection(call: CallItem) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(110.dp)
                .clip(CircleShape)
                .background(call.avatarColor)
                .border(
                    width = 2.dp,
                    color = if (call.isTrusted) TiffanyBlue else AzureBlue,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (call.isTrusted) {
                Icon(
                    painter = painterResource(id = com.secyra.ai.R.drawable.ic_shield_logo),
                    contentDescription = null,
                    tint = SecyraWhite,
                    modifier = Modifier.size(48.dp)
                )
            } else {
                Text(
                    text = call.initials,
                    color = SecyraWhite,
                    fontWeight = FontWeight.Medium,
                    fontSize = 36.sp
                )
            }
        }

        // Name
        Text(
            text = call.name,
            color = SecyraWhite,
            fontWeight = FontWeight.SemiBold,
            fontSize = 24.sp
        )

        // Phone Number
        Text(
            text = call.phoneNumber,
            color = SecyraTextSecondary,
            fontSize = 17.sp
        )

        // Status Chip
        val (statusColor, statusText) = when {
            call.isSpam -> Color(0xFF3D0A0A) to "Blocked"
            call.isTrusted -> Color(0xFF0A3D3B) to "Trusted"
            else -> Color(0xFF1A2A3A) to "Unknown"
        }
        Box(
            modifier = Modifier
                .height(32.dp)
                .border(
                    width = 1.dp,
                    color = statusColor.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = statusText,
                color = statusColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun MainActionButtons(
    onCall: () -> Unit,
    onVideoCall: () -> Unit,
    onMessage: () -> Unit,
    onMore: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ActionButton(
            icon = Icons.Default.Phone,
            label = "Call",
            onClick = onCall,
            modifier = Modifier.weight(1f),
            isSelected = true
        )
        ActionButton(
            icon = Icons.Default.Videocam,
            label = "Video Call",
            onClick = onVideoCall,
            modifier = Modifier.weight(1f),
            isSelected = false
        )
        ActionButton(
            icon = Icons.Default.Email,
            label = "Message",
            onClick = onMessage,
            modifier = Modifier.weight(1f),
            isSelected = false
        )
        ActionButton(
            icon = Icons.Default.MoreVert,
            label = "More",
            onClick = onMore,
            modifier = Modifier.weight(1f),
            isSelected = false
        )
    }
}

@Composable
fun ActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false
) {
    Card(
        modifier = modifier
            .height(82.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF0A3D3B) else SecyraSurface
        ),
        border = BorderStroke(1.dp, Color(0xFF202020)),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) TiffanyBlue else SecyraTextSecondary,
                modifier = Modifier.size(28.dp)
            )
            Text(
                text = label,
                color = if (isSelected) SecyraWhite else SecyraTextSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun ContactDetailsCard(call: CallItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = SecyraSurface
        ),
        border = BorderStroke(1.dp, Color(0xFF202020)),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Contact Details",
                color = SecyraWhite,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
            DetailRow(
                icon = Icons.Default.Phone,
                label = "Phone",
                value = call.phoneNumber
            )
            DetailRow(
                icon = Icons.Default.Person,
                label = "Name",
                value = call.name
            )
        }
    }
}

@Composable
fun DetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = SecyraTextSecondary,
            modifier = Modifier.size(20.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                color = SecyraTextSecondary,
                fontSize = 13.sp
            )
            Text(
                text = value,
                color = SecyraWhite,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun SecurityAnalysisCard(call: CallItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .clickable { },
        colors = CardDefaults.cardColors(
            containerColor = SecyraSurface
        ),
        border = BorderStroke(1.dp, Color(0xFF202020)),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = com.secyra.ai.R.drawable.ic_shield_logo),
                    contentDescription = null,
                    tint = TiffanyBlue,
                    modifier = Modifier.size(32.dp)
                )
                Column {
                    Text(
                        text = "Security Analysis",
                        color = SecyraWhite,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    val statusText = when {
                        call.isSpam -> "Blocked - Suspicious activity detected"
                        call.isTrusted -> "Trusted - No warning signals found"
                        else -> "Unknown - Not enough information"
                    }
                    Text(
                        text = statusText,
                        color = SecyraTextSecondary,
                        fontSize = 13.sp
                    )
                }
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = SecyraTextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun CallHistoryCard(call: CallItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .clickable { },
        colors = CardDefaults.cardColors(
            containerColor = SecyraSurface
        ),
        border = BorderStroke(1.dp, Color(0xFF202020)),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = null,
                    tint = TiffanyBlue,
                    modifier = Modifier.size(32.dp)
                )
                Column {
                    Text(
                        text = "Call History",
                        color = SecyraWhite,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Last call: ${call.timestamp}",
                        color = SecyraTextSecondary,
                        fontSize = 13.sp
                    )
                }
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = SecyraTextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun MessageHistoryCard(call: CallItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .clickable { },
        colors = CardDefaults.cardColors(
            containerColor = SecyraSurface
        ),
        border = BorderStroke(1.dp, Color(0xFF202020)),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = null,
                    tint = AzureBlue,
                    modifier = Modifier.size(32.dp)
                )
                Column {
                    Text(
                        text = "Message History",
                        color = SecyraWhite,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "No messages yet",
                        color = SecyraTextSecondary,
                        fontSize = 13.sp
                    )
                }
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = SecyraTextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun BlockContactAction(
    isBlocked: Boolean,
    onBlock: () -> Unit,
    onUnblock: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .clickable { if (isBlocked) onUnblock() else onBlock() },
        colors = CardDefaults.cardColors(
            containerColor = if (isBlocked) Color(0xFF0A3D3B) else Color(0xFF3D0A0A)
        ),
        border = BorderStroke(1.dp, if (isBlocked) TiffanyBlue else RiskHigh),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isBlocked) Icons.Default.CheckCircle else Icons.Default.Block,
                contentDescription = null,
                tint = SecyraWhite,
                modifier = Modifier.size(28.dp)
            )
            Column {
                Text(
                    text = if (isBlocked) "Unblock Contact" else "Block Contact",
                    color = SecyraWhite,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = if (isBlocked) "Allow calls from this number" else "Block calls from this number",
                    color = SecyraWhite.copy(alpha = 0.7f),
                    fontSize = 13.sp
                )
            }
        }
    }
}
