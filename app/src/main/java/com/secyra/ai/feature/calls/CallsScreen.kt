package com.secyra.ai.feature.calls

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.secyra.ai.R
import com.secyra.ai.core.ui.theme.*
import com.secyra.ai.data.repository.Contact

@Composable
fun CallsScreen(
    modifier: Modifier = Modifier,
    viewModel: CallsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Handle call intent
    LaunchedEffect(uiState.callIntent) {
        uiState.callIntent?.let { intent ->
            try {
                context.startActivity(intent)
                viewModel.onCallIntentHandled()
            } catch (e: Exception) {
                viewModel.onCallIntentHandled()
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(SecyraBackground)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        // SECYRA Header
        item {
            SecyraCallsHeader(
                onSearchClick = { viewModel.onSearchClick() },
                onMenuClick = { }
            )
        }

        // Search Bar (shown when search is active)
        if (uiState.isSearchActive) {
            item {
                SearchBar(
                    onSearchChange = { viewModel.onSearchChange(it) },
                    searchQuery = uiState.searchQuery,
                    onCloseSearch = { viewModel.onCloseSearch() }
                )
            }
        }

        // Calls Title Section
        item {
            CallsTitleSection()
        }

        // Segmented Control
        item {
            CallsSegmentedControl(
                selectedTab = uiState.selectedTab,
                onTabSelected = { viewModel.onTabSelected(it) }
            )
        }

        // Content based on selected tab
        when (uiState.selectedTab) {
            CallTab.Recent -> {
                if (uiState.recentCalls.isEmpty()) {
                    item {
                        EmptyState(
                        message = "No recent calls",
                        subMessage = "Your call history will appear here"
                        )
                    }
                } else {
                    items(uiState.recentCalls) { call ->
                        CallRow(
                            call = call,
                            onClick = { viewModel.onCallPhoneNumber(call.phoneNumber) }
                        )
                    }
                }
            }
            CallTab.Contacts -> {
                val contactsToShow = if (uiState.isSearchActive && uiState.searchQuery.isNotEmpty()) {
                    uiState.searchResults
                } else {
                    uiState.contacts
                }
                if (contactsToShow.isEmpty()) {
                    item {
                        ContactsEmptyState()
                    }
                } else {
                    items(contactsToShow) { contact ->
                        ContactRow(
                            contact = contact,
                            onClick = { viewModel.onCallContact(contact) }
                        )
                    }
                }
            }
            CallTab.Blocked -> {
                if (uiState.blockedNumbers.isEmpty()) {
                    item {
                        BlockedEmptyState()
                    }
                } else {
                    items(uiState.blockedNumbers) { blocked ->
                        BlockedNumberRow(
                            blocked = blocked,
                            onUnblock = { viewModel.onUnblockNumber(blocked) }
                        )
                    }
                }
            }
        }

        // Ask Secyra Banner
        item {
            AskSecyraBanner(
                onAskNow = { }
            )
        }
    }
}

@Composable
fun SecyraCallsHeader(
    onSearchClick: () -> Unit,
    onMenuClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // SECYRA Shield Logo
            Icon(
                painter = painterResource(id = R.drawable.ic_shield_logo),
                contentDescription = "SECYRA AI",
                tint = Color.Unspecified,
                modifier = Modifier.size(40.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "SECYRA AI",
                    style = MaterialTheme.typography.headlineSmall,
                    color = SecyraWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                Text(
                    text = "See. Understand. Stay Secure.",
                    style = MaterialTheme.typography.bodySmall,
                    color = SecyraTextSecondary,
                    fontSize = 11.sp
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            IconButton(onClick = onSearchClick) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = SecyraWhite,
                    modifier = Modifier.size(24.dp)
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
}

@Composable
fun CallsTitleSection() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "Calls",
                style = MaterialTheme.typography.displaySmall,
                color = SecyraWhite,
                fontWeight = FontWeight.Bold,
                fontSize = 40.sp
            )
            Text(
                text = "Smart call protection. Safer conversations.",
                style = MaterialTheme.typography.bodyMedium,
                color = SecyraTextSecondary,
                fontSize = 16.sp
            )
        }
        // Subtle security visual
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(TiffanyBlue.copy(alpha = 0.05f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_shield_logo),
                contentDescription = null,
                tint = TiffanyBlue.copy(alpha = 0.3f),
                modifier = Modifier.size(36.dp)
            )
        }
    }
}

@Composable
fun SearchBar(
    onSearchChange: (String) -> Unit,
    searchQuery: String,
    onCloseSearch: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(SecyraSurface, RoundedCornerShape(28.dp))
            .border(1.dp, TiffanyBlue, RoundedCornerShape(28.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = SecyraWhite,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            TextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("Search contacts...", color = SecyraTextSecondary) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                textStyle = androidx.compose.material3.LocalTextStyle.current.copy(color = SecyraWhite),
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            IconButton(onClick = onCloseSearch) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = SecyraTextSecondary
                )
            }
        }
    }
}

@Composable
fun CallsSegmentedControl(
    selectedTab: CallTab,
    onTabSelected: (CallTab) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .background(SecyraSurface, RoundedCornerShape(28.dp))
            .border(1.dp, SecyraBorder, RoundedCornerShape(28.dp)),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        SegmentedTab(
            icon = Icons.Default.Phone,
            text = "Recent",
            isSelected = selectedTab == CallTab.Recent,
            onClick = { onTabSelected(CallTab.Recent) },
            modifier = Modifier.weight(1f)
        )
        SegmentedTab(
            icon = Icons.Default.Person,
            text = "Contacts",
            isSelected = selectedTab == CallTab.Contacts,
            onClick = { onTabSelected(CallTab.Contacts) },
            modifier = Modifier.weight(1f)
        )
        SegmentedTab(
            icon = Icons.Default.Close,
            text = "Blocked",
            isSelected = selectedTab == CallTab.Blocked,
            onClick = { onTabSelected(CallTab.Blocked) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun SegmentedTab(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) {
        TiffanyBlue
    } else {
        Color.Transparent
    }
    val textColor = if (isSelected) Color.Black else SecyraTextSecondary
    val iconColor = if (isSelected) Color.Black else SecyraTextSecondary

    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(28.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = textColor,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun CallRow(
    call: CallItem,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.weight(1f)
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(call.avatarColor),
                contentAlignment = Alignment.Center
            ) {
                if (call.isTrusted) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_shield_logo),
                        contentDescription = null,
                        tint = SecyraWhite,
                        modifier = Modifier.size(22.dp)
                    )
                } else {
                    Text(
                        text = call.initials,
                        style = MaterialTheme.typography.titleMedium,
                        color = SecyraWhite,
                        fontWeight = FontWeight.Medium,
                        fontSize = 18.sp
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = call.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (call.isMissed) RiskHigh else SecyraWhite,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
                // Security status - only show tag for unknown/spam
                if (!call.isTrusted && call.isSpam) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_shield_logo),
                            contentDescription = null,
                            tint = RiskHigh,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "Potential Spam",
                            style = MaterialTheme.typography.bodySmall,
                            color = RiskHigh,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Call direction arrow
            Icon(
                imageVector = when (call.callDirection) {
                    "incoming" -> Icons.Default.ArrowDownward
                    "outgoing" -> Icons.Default.ArrowUpward
                    "missed" -> Icons.Default.ArrowDownward
                    else -> Icons.Default.Phone
                },
                contentDescription = "Call Direction",
                tint = if (call.isMissed) RiskHigh else SecyraTextSecondary,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = call.timestamp,
                style = MaterialTheme.typography.labelSmall,
                color = SecyraTextSecondary,
                fontSize = 12.sp
            )
            // Call button
            IconButton(
                onClick = onClick,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = "Call",
                    tint = TiffanyBlue,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun ContactRow(
    contact: Contact,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(TiffanyBlue.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (contact.name.isNotEmpty()) contact.name.first().toString().uppercase() else "C",
                    style = MaterialTheme.typography.headlineSmall,
                    color = TiffanyBlue,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = contact.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = SecyraWhite,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
                Text(
                    text = contact.phoneNumber,
                    style = MaterialTheme.typography.bodyMedium,
                    color = SecyraTextSecondary,
                    fontSize = 14.sp
                )
            }
        }
        IconButton(
            onClick = onClick,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Phone,
                contentDescription = "Call",
                tint = TiffanyBlue,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun BlockedNumberRow(
    blocked: BlockedNumber,
    onUnblock: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(RiskHigh.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null,
                    tint = RiskHigh,
                    modifier = Modifier.size(22.dp)
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = blocked.displayName ?: blocked.number,
                    style = MaterialTheme.typography.bodyLarge,
                    color = SecyraWhite,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
                Text(
                    text = blocked.number,
                    style = MaterialTheme.typography.bodyMedium,
                    color = SecyraTextSecondary,
                    fontSize = 14.sp
                )
            }
        }
        TextButton(
            onClick = onUnblock,
            colors = ButtonDefaults.textButtonColors(
                contentColor = TiffanyBlue
            )
        ) {
            Text("Unblock", fontSize = 14.sp)
        }
    }
}

@Composable
fun EmptyState(
    message: String,
    subMessage: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Phone,
            contentDescription = null,
            tint = SecyraTextMuted,
            modifier = Modifier.size(48.dp)
        )
        Text(
            text = message,
            style = MaterialTheme.typography.titleMedium,
            color = SecyraWhite,
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp
        )
        Text(
            text = subMessage,
            style = MaterialTheme.typography.bodyMedium,
            color = SecyraTextSecondary,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ContactsEmptyState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = null,
            tint = SecyraTextMuted,
            modifier = Modifier.size(48.dp)
        )
        Text(
            text = "Contacts access required",
            style = MaterialTheme.typography.titleMedium,
            color = SecyraWhite,
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp
        )
        Text(
            text = "Allow contacts access to show your contacts",
            style = MaterialTheme.typography.bodyMedium,
            color = SecyraTextSecondary,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = { },
            colors = ButtonDefaults.buttonColors(
                containerColor = TiffanyBlue,
                contentColor = Color.Black
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Allow Contacts", fontSize = 14.sp)
        }
    }
}

@Composable
fun BlockedEmptyState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = null,
            tint = SecyraTextMuted,
            modifier = Modifier.size(48.dp)
        )
        Text(
            text = "No blocked numbers",
            style = MaterialTheme.typography.titleMedium,
            color = SecyraWhite,
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp
        )
        Text(
            text = "Numbers you block will appear here",
            style = MaterialTheme.typography.bodyMedium,
            color = SecyraTextSecondary,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun AskSecyraBanner(
    onAskNow: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onAskNow),
        colors = CardDefaults.cardColors(
            containerColor = SecyraSurface
        ),
        border = BorderStroke(1.dp, TiffanyBlue),
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
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(TiffanyBlue.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_shield_logo),
                        contentDescription = null,
                        tint = TiffanyBlue,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Ask Secyra AI",
                        style = MaterialTheme.typography.titleMedium,
                        color = SecyraWhite,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp
                    )
                    Text(
                        text = "Not sure about a number? Ask Secyra AI.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SecyraTextSecondary,
                        fontSize = 14.sp
                    )
                }
            }
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null,
                tint = TiffanyBlue,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
