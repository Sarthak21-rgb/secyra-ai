package com.secyra.ai.feature.calls

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PhoneMissed
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import java.util.Calendar as JavaCalendar
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextMotion
import androidx.compose.ui.res.painterResource
import com.secyra.ai.feature.emails.EmailScreen
import com.secyra.ai.feature.search.SearchScreen
import com.secyra.ai.navigation.Screen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.secyra.ai.R
import com.secyra.ai.core.ui.theme.*
import com.secyra.ai.data.repository.Contact
import com.secyra.ai.feature.contacts.ContactProfileScreen
import com.secyra.ai.feature.messages.MessagesScreen
import com.secyra.ai.feature.messages.MessageTab
import kotlinx.coroutines.launch

@Composable
fun CallsScreen(
    modifier: Modifier = Modifier,
    viewModel: CallsViewModel = hiltViewModel(),
    onNavigateToSearch: () -> Unit = {},
    onNavigateToOnboarding: () -> Unit = {},
    onUserSetDefault: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val pagerState = rememberPagerState(pageCount = { 3 })
    val coroutineScope = rememberCoroutineScope()

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

    // Sync pager with selected page
    LaunchedEffect(uiState.selectedPage) {
        pagerState.animateScrollToPage(uiState.selectedPage.ordinal)
    }

    // Sync selected page with pager
    LaunchedEffect(pagerState.currentPage) {
        viewModel.onPageSelected(PageIndex.values()[pagerState.currentPage])
    }

    // Check if default phone app and show onboarding prompt
    LaunchedEffect(Unit) {
        viewModel.checkDefaultPhoneStatus()
    }

    LaunchedEffect(uiState.showOnboardingPrompt) {
        if (uiState.showOnboardingPrompt) {
            // Show a prompt card for default phone app
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SecyraBackground)
    ) {
        // SECYRA Header
        SecyraCallsHeader(
            selectedPage = uiState.selectedPage,
            onSearchClick = { onNavigateToSearch() },
            onMenuClick = { }
        )

        // Default Phone App Onboarding Banner
        if (!uiState.isDefaultPhoneApp && uiState.showOnboardingPrompt) {
            DefaultPhoneAppBanner(
                onSetAsDefault = { 
                    onNavigateToOnboarding()
                    onUserSetDefault()
                },
                onDismiss = { viewModel.dismissOnboardingPrompt() }
            )
        }

        // Calls / Messages Segmented Control
        CallsMessagesSegmentedControl(
            selectedPage = uiState.selectedPage,
            onPageSelected = { page ->
                viewModel.onPageSelected(page)
                coroutineScope.launch {
                    pagerState.animateScrollToPage(page.ordinal)
                }
            }
        )

        // Horizontal Pager
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            when (PageIndex.values()[page]) {
                PageIndex.Calls -> CallsPageContent(viewModel = viewModel)
                PageIndex.Messages -> MessagesScreen()
                PageIndex.Emails -> EmailScreen()
            }
        }

        // Contact Profile Overlay
        uiState.selectedContactForProfile?.let { contact ->
            ContactProfileScreen(
                call = contact,
                onBack = { viewModel.onContactProfileDismissed() },
                onCall = { viewModel.onCallPhoneNumber(it) },
                onMessage = { },
                onBlock = { viewModel.onBlockNumber(it) },
                onUnblock = { viewModel.onUnblockNumber(it) },
                onReport = { },
                onTrust = { }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CallsPageContent(
    viewModel: CallsViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SecyraBackground)
    ) {
        // Calls secondary tabs - fixed at top
        CallsTabRow(
            selectedTab = uiState.selectedTab,
            onTabSelected = { viewModel.onTabSelected(it) }
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {

        // Content based on selected tab
        when (uiState.selectedTab) {
            CallTab.All -> {
                // 7 months = 7 * 30.44 days * 24 * 60 * 60 * 1000 ms
                val sevenMonthsAgo = System.currentTimeMillis() - (7L * 30 * 24 * 60 * 60 * 1000L)
                val allGroups = uiState.callGroups.filter { it.newestCall.timestampMillis >= sevenMonthsAgo }
                android.util.Log.d("CallsScreen", "All tab: filtered to ${allGroups.size} groups from last 7 months")

                if (allGroups.isEmpty()) {
                    item {
                        EmptyState(
                            message = "No calls",
                            subMessage = "Your call history will appear here"
                        )
                    }
                } else {
                    // Group by time period
                    val now = System.currentTimeMillis()
                    val dayInMillis = 24 * 60 * 60 * 1000L

                    val today = allGroups.filter { now - it.newestCall.timestampMillis < dayInMillis }
                    val yesterday = allGroups.filter {
                        val diff = now - it.newestCall.timestampMillis
                        diff >= dayInMillis && diff < 2 * dayInMillis
                    }
                    val older = allGroups.filter { now - it.newestCall.timestampMillis >= 2 * dayInMillis }

                    if (today.isNotEmpty()) {
                        item {
                            TimePeriodHeader("Today")
                        }
                        items(today) { group ->
                            CallGroupRow(
                                group = group,
                                onProfileClick = { viewModel.onViewGroupProfile(group) },
                                onCallClick = { viewModel.onCallPhoneNumber(group.phoneNumber) }
                            )
                        }
                    }

                    if (yesterday.isNotEmpty()) {
                        item {
                            TimePeriodHeader("Yesterday")
                        }
                        items(yesterday) { group ->
                            CallGroupRow(
                                group = group,
                                onProfileClick = { viewModel.onViewGroupProfile(group) },
                                onCallClick = { viewModel.onCallPhoneNumber(group.phoneNumber) }
                            )
                        }
                    }

                    if (older.isNotEmpty()) {
                        item {
                            TimePeriodHeader("Older")
                        }
                        items(older) { group ->
                            CallGroupRow(
                                group = group,
                                onProfileClick = { viewModel.onViewGroupProfile(group) },
                                onCallClick = { viewModel.onCallPhoneNumber(group.phoneNumber) }
                            )
                        }
                    }
                }
            }
            CallTab.Recent -> {
                val threeDaysAgo = System.currentTimeMillis() - (3 * 24 * 60 * 60 * 1000L)
                val recentGroups = uiState.callGroups.filter { it.newestCall.timestampMillis >= threeDaysAgo }
                android.util.Log.d("CallsScreen", "Recent tab: filtered to ${recentGroups.size} groups from last 3 days")
                if (recentGroups.isEmpty()) {
                    item {
                        EmptyState(
                            message = "No recent calls",
                            subMessage = "Calls from the last 3 days will appear here"
                        )
                    }
                } else {
                    items(recentGroups) { group ->
                        CallGroupRow(
                            group = group,
                            onProfileClick = { viewModel.onViewGroupProfile(group) },
                            onCallClick = { viewModel.onCallPhoneNumber(group.phoneNumber) }
                        )
                    }
                }
            }
            CallTab.Blocked -> {
                val blockedGroups = uiState.callGroups.filter { it.isBlocked }
                if (blockedGroups.isEmpty()) {
                    item {
                        BlockedEmptyState()
                    }
                } else {
                    items(blockedGroups) { group ->
                        CallGroupRow(
                            group = group,
                            onProfileClick = { viewModel.onViewGroupProfile(group) },
                            onCallClick = { viewModel.onCallPhoneNumber(group.phoneNumber) }
                        )
                    }
                }
            }
            CallTab.Spam -> {
                // Spam tab will show real spam calls from spam detection system
                // For now, show empty state until spam detection is implemented
                item {
                    EmptyState(
                        message = "No spam calls",
                        subMessage = "Spam calls will appear here"
                    )
                }
            }
            CallTab.Contacts -> {
                // Show all saved contacts from user's phone
                if (uiState.contacts.isEmpty()) {
                    item {
                        EmptyState(
                            message = "No contacts",
                            subMessage = "Your contacts will appear here"
                        )
                    }
                } else {
                    items(uiState.contacts) { contact ->
                        ContactCard(
                            contact = contact,
                            onProfileClick = { viewModel.onContactProfileClick(contact) },
                            onCallClick = { viewModel.onCallContact(contact) }
                        )
                    }
                }
            }
        }
    }
    }
}

@Composable
fun DefaultPhoneAppBanner(
    onSetAsDefault: () -> Unit,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF0A1520)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            TiffanyBlue.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = null,
                    tint = TiffanyBlue,
                    modifier = Modifier.size(24.dp)
                )
                Column {
                    Text(
                        text = "Enable Full Call Features",
                        style = MaterialTheme.typography.bodyLarge,
                        color = SecyraWhite,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Set Secyra as default phone app for custom incoming call screen",
                        style = MaterialTheme.typography.bodySmall,
                        color = SecyraTextSecondary
                    )
                }
            }
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss",
                    tint = SecyraTextMuted
                )
            }
        }
    }
}

@Composable
fun SecyraCallsHeader(
    selectedPage: PageIndex,
    onSearchClick: () -> Unit,
    onMenuClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // SECYRA Shield Logo - use actual PNG file
            Icon(
                painter = painterResource(id = R.drawable.shield_logo),
                contentDescription = "SECYRA AI",
                tint = Color.Unspecified,
                modifier = Modifier.size(52.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "SECYRA",
                    style = MaterialTheme.typography.headlineSmall,
                    color = SecyraWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                Text(
                    text = "AI POWERED SECURITY",
                    style = MaterialTheme.typography.bodySmall,
                    color = AzureBlue,
                    fontSize = 11.sp
                )
            }
        }

        // Centered Search Bar
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp, end = 4.dp)
                .height(38.dp)
                .clip(RoundedCornerShape(19.dp))
                .background(Color(0xFF252525))
                .border(
                    width = 1.dp,
                    brush = Brush.horizontalGradient(
                        colors = listOf(AzureBlue, TiffanyBlue)
                    ),
                    shape = RoundedCornerShape(19.dp)
                )
                .clickable(onClick = onSearchClick),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = TiffanyBlue,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = when (selectedPage) {
                        PageIndex.Calls -> "Search calls..."
                        PageIndex.Messages -> "Search messages..."
                        PageIndex.Emails -> "Search emails..."
                    },
                    color = Color(0xFFA0A0A0),
                    fontSize = 14.sp
                )
            }
        }

        IconButton(onClick = onMenuClick) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                tint = TiffanyBlue,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun CallsMessagesSegmentedControl(
    selectedPage: PageIndex,
    onPageSelected: (PageIndex) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .height(54.dp)
            .clip(RoundedCornerShape(27.dp))
            .background(Color.Transparent)
            .border(1.dp, Color(0xFF6A89A7), RoundedCornerShape(27.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            SegmentedButton(
                selected = selectedPage == PageIndex.Calls,
                onClick = { onPageSelected(PageIndex.Calls) },
                icon = Icons.Default.Phone,
                iconTint = if (selectedPage == PageIndex.Calls) Color.Black else Color(0xFF707070),
                label = "CALLS",
                modifier = Modifier.weight(1f)
            )
            SegmentedButton(
                selected = selectedPage == PageIndex.Messages,
                onClick = { onPageSelected(PageIndex.Messages) },
                icon = Icons.Default.Comment,
                iconTint = if (selectedPage == PageIndex.Messages) Color.Black else Color(0xFF707070),
                label = "MESSAGES",
                modifier = Modifier.weight(1f)
            )
            SegmentedButton(
                selected = selectedPage == PageIndex.Emails,
                onClick = { onPageSelected(PageIndex.Emails) },
                icon = Icons.Default.Email,
                iconTint = if (selectedPage == PageIndex.Emails) Color.Black else Color(0xFF707070),
                label = "EMAILS",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun SegmentedButton(
    selected: Boolean,
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    label: String,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (selected) {
        Color.Transparent
    } else {
        Color.Transparent
    }
    val textColor = if (selected) SecyraWhite else Color(0xFF707070)
    val iconColor = if (selected) AzureBlue else Color(0xFF707070)

    Box(
        modifier = modifier
            .fillMaxHeight()
            .clickable(onClick = onClick)
            .then(
                if (selected) {
                    Modifier
                        .background(
                            backgroundColor,
                            RoundedCornerShape(27.dp)
                        )
                        .border(
                            width = 2.dp,
                            brush = Brush.horizontalGradient(
                                colors = listOf(AzureBlue, TiffanyBlue)
                            ),
                            shape = RoundedCornerShape(27.dp)
                        )
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = label,
                color = textColor,
                fontSize = 14.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
            )
        }
    }
}

@Composable
fun CallsTabRow(
    selectedTab: CallTab,
    onTabSelected: (CallTab) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        CallTab.values().forEach { tab ->
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
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(2.dp)
                            .background(TiffanyBlue)
                    )
                }
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
fun CallsSegmentedControl(
    selectedTab: CallTab,
    onTabSelected: (CallTab) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .background(SecyraSurface, RoundedCornerShape(22.dp))
            .border(1.dp, SecyraBorder, RoundedCornerShape(22.dp)),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        SegmentedTab(
            icon = Icons.Default.Phone,
            text = "All",
            isSelected = selectedTab == CallTab.All,
            onClick = { onTabSelected(CallTab.All) },
            modifier = Modifier.weight(1f)
        )
        SegmentedTab(
            icon = Icons.AutoMirrored.Filled.PhoneMissed,
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
        SegmentedTab(
            icon = Icons.Default.Warning,
            text = "Spam",
            isSelected = selectedTab == CallTab.Spam,
            onClick = { onTabSelected(CallTab.Spam) },
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
    val textColor = if (isSelected) SecyraWhite else SecyraTextSecondary
    val iconColor = if (isSelected) SecyraWhite else SecyraTextSecondary

    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(22.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = text.replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.bodyMedium,
                color = textColor,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
fun CallGroupRow(
    group: com.secyra.ai.core.telecom.CallGroup,
    onProfileClick: () -> Unit,
    onCallClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(88.dp)
            .clickable(onClick = onProfileClick),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF0A0A0A)
        ),
        border = BorderStroke(1.dp, Color(0xFF202020)),
        shape = RoundedCornerShape(12.dp)
    ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar - always show first letter in white
                val displayName = group.contactName ?: group.phoneNumber
                val initials = if (group.contactName != null && group.contactName.isNotEmpty()) {
                    group.contactName.first().toString().uppercase()
                } else {
                    "U"
                }
                val avatarColor = getContactColor(group.contactName, group.phoneNumber)

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(avatarColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initials,
                        color = SecyraWhite,
                        fontWeight = FontWeight.Medium,
                        fontSize = 22.sp
                    )
                }

                // Center: Name/Number and Call Type + Timestamp
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Name or Phone Number with count in brackets
                    val nameWithCount = if (group.hasMultipleCalls) {
                        "${displayName.take(17)}(${group.totalCallCount})"
                    } else {
                        displayName.take(20)
                    }
                    Text(
                        text = nameWithCount,
                        color = SecyraWhite,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )

                    // Call Type and Timestamp row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val callType = when (group.newestCall.callType) {
                            com.secyra.ai.core.telecom.CallType.INCOMING -> "Incoming"
                            com.secyra.ai.core.telecom.CallType.OUTGOING -> "Outgoing"
                            com.secyra.ai.core.telecom.CallType.MISSED -> "Missed"
                            com.secyra.ai.core.telecom.CallType.REJECTED -> "Declined"
                            com.secyra.ai.core.telecom.CallType.BLOCKED -> "Blocked"
                            com.secyra.ai.core.telecom.CallType.VOICEMAIL -> "Voicemail"
                            com.secyra.ai.core.telecom.CallType.UNKNOWN -> "Call"
                        }

                        val callTypeIcon = when (group.newestCall.callType) {
                            com.secyra.ai.core.telecom.CallType.INCOMING -> Icons.Default.CallReceived
                            com.secyra.ai.core.telecom.CallType.OUTGOING -> Icons.Default.CallMade
                            com.secyra.ai.core.telecom.CallType.MISSED -> Icons.AutoMirrored.Filled.PhoneMissed
                            com.secyra.ai.core.telecom.CallType.REJECTED -> Icons.Default.PhoneDisabled
                            com.secyra.ai.core.telecom.CallType.BLOCKED -> Icons.Default.Block
                            com.secyra.ai.core.telecom.CallType.VOICEMAIL -> Icons.Default.Voicemail
                            com.secyra.ai.core.telecom.CallType.UNKNOWN -> Icons.Default.Phone
                        }

                        val iconColor = if (group.newestCall.callType == com.secyra.ai.core.telecom.CallType.MISSED ||
                                            group.newestCall.callType == com.secyra.ai.core.telecom.CallType.BLOCKED) {
                            Color(0xFFFF4D6D)
                        } else {
                            TiffanyBlue
                        }

                        Icon(
                            imageVector = callTypeIcon,
                            contentDescription = null,
                            tint = iconColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = callType,
                            color = if (group.newestCall.callType == com.secyra.ai.core.telecom.CallType.MISSED ||
                                        group.newestCall.callType == com.secyra.ai.core.telecom.CallType.BLOCKED) Color(0xFFFF4D6D) else Color(0xFFA0AEC0),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "•",
                            color = Color(0xFF505050)
                        )
                        Text(
                            text = formatTimestamp(group.newestCall.timestampMillis),
                            color = SecyraTextMuted,
                            fontSize = 13.sp
                        )

                        // Show Unknown tag for unsaved incoming calls
                        if (!group.isSavedContact && group.newestCall.callType == com.secyra.ai.core.telecom.CallType.INCOMING) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .height(20.dp)
                                    .background(Color(0xFF4A5568).copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Unknown",
                                    color = Color(0xFFA0AEC0),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                // Right side: Call action only (count shown in name with brackets)
                IconButton(
                    onClick = onCallClick,
                    modifier = Modifier.size(40.dp)
                ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .border(2.dp, TiffanyBlue, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = "Call",
                                tint = TiffanyBlue,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                }
            }
        }
}

@Composable
fun TimePeriodHeader(title: String) {
    Text(
        text = title,
        color = Color(0xFFA0A0A0),
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.padding(start = 12.dp, top = 16.dp, bottom = 8.dp)
    )
}

// Helper function to get contact color from the 23-color palette
// Tracks previous color to avoid consecutive repetition
private var lastUsedColorIndex = -1
private fun getContactColor(name: String?, phoneNumber: String): Color {
    val CONTACT_COLORS = listOf(
        Color(0xFF007FFF), // Azure Blue
        Color(0xFF0ABAB5), // Tiffany Blue
        Color(0xFF008080), // Teal Blue
        Color(0xFF8A2BE2), // Violet
        Color(0xFFFF9933), // Saffron
        Color(0xFF2E8B57), // Pine
        Color(0xFF00A86B), // Pantone Jade
        Color(0xFF960018), // Carmine
        Color(0xFF8E4585), // Plum
        Color(0xFFDA70D6), // Orchid Purple
        Color(0xFFE30B5D), // Raspberry
        Color(0xFFC21807), // Chili
        Color(0xFF1565C0), // Parish Blue
        Color(0xFF50C878), // Emerald Green
        Color(0xFF000080), // Navy Blue
        Color(0xFFE2725B), // Terracotta
        Color(0xFF663399), // Damson
        Color(0xFF6F4E37), // Coffee
        Color(0xFF228B22), // Forest
        Color(0xFF722F37), // Wine
        Color(0xFFCC7722), // Ochre
        Color(0xFF7B3F00), // Cacao
        Color(0xFFA0522D)  // Auburn
    )
    val key = (name ?: phoneNumber).hashCode()
    var index = kotlin.math.abs(key) % CONTACT_COLORS.size
    
    // Avoid repeating the same color consecutively
    if (index == lastUsedColorIndex) {
        index = (index + 1) % CONTACT_COLORS.size
    }
    lastUsedColorIndex = index
    
    return CONTACT_COLORS[index]
}

// Helper function to format timestamp
private fun formatTimestamp(timestampMillis: Long): String {
    val now = System.currentTimeMillis()
    val calendarNow = JavaCalendar.getInstance()
    val calendarCall = JavaCalendar.getInstance()
    calendarCall.timeInMillis = timestampMillis

    // Check if same day (midnight to midnight)
    val isSameDay = calendarNow.get(JavaCalendar.YEAR) == calendarCall.get(JavaCalendar.YEAR) &&
                    calendarNow.get(JavaCalendar.DAY_OF_YEAR) == calendarCall.get(JavaCalendar.DAY_OF_YEAR)

    // Check if yesterday
    calendarNow.add(JavaCalendar.DAY_OF_YEAR, -1)
    val isYesterday = calendarNow.get(JavaCalendar.YEAR) == calendarCall.get(JavaCalendar.YEAR) &&
                     calendarNow.get(JavaCalendar.DAY_OF_YEAR) == calendarCall.get(JavaCalendar.DAY_OF_YEAR)

    val date = java.util.Date(timestampMillis)
    val timeFormat = java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault())
    val timeStr = timeFormat.format(date)

    return when {
        isSameDay -> {
            // Today - show only time
            timeStr
        }
        isYesterday -> {
            // Yesterday - show "Yes, time"
            "Yes, $timeStr"
        }
        else -> {
            // Other days - show first 3 letters of day name + time
            val dayFormat = java.text.SimpleDateFormat("EEE", java.util.Locale.getDefault())
            val dayStr = dayFormat.format(date).take(3)
            "$dayStr, $timeStr"
        }
    }
}

@Composable
fun CallRowCard(
    call: CallItem,
    onProfileClick: () -> Unit,
    onCallClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(88.dp)
            .clickable(onClick = onProfileClick),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF0A0A0A)
        ),
        border = BorderStroke(1.dp, Color(0xFF202020)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar - always show first letter in white
            val avatarColor = getContactColor(call.name, call.phoneNumber)
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(avatarColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = call.initials,
                    color = SecyraWhite,
                    fontWeight = FontWeight.Medium,
                    fontSize = 22.sp
                )
            }

            // Center: Name/Number and Call Type + Timestamp
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically)
            ) {
                val displayName = if (call.callCountInHour > 1) {
                    "${call.name} (${call.callCountInHour})"
                } else {
                    call.name
                }
                Text(
                    text = displayName,
                    color = SecyraWhite,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Call type icon with correct color
                    val iconColor = when (call.callType) {
                        CallType.MISSED -> Color(0xFFFF4D6D)
                        CallType.REJECTED -> Color(0xFFFF6B6B)
                        else -> Color(0xFF8AB4F8)
                    }
                    Icon(
                        imageVector = call.callTypeIcon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = when (call.callType) {
                            CallType.INCOMING -> "Incoming"
                            CallType.OUTGOING -> "Outgoing"
                            CallType.MISSED -> "Missed"
                            CallType.REJECTED -> "Declined"
                            CallType.BLOCKED -> "Blocked"
                            CallType.UNKNOWN -> "Call"
                        },
                        color = if (call.callType == CallType.MISSED || call.callType == CallType.BLOCKED) Color(0xFFFF4D6D) else Color(0xFFA0AEC0),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "•",
                        color = Color(0xFF657080),
                        fontSize = 13.sp
                    )
                    Text(
                        text = call.timestamp,
                        color = Color(0xFFA0AEC0),
                        fontSize = 12.sp
                    )
                }
            }

            // Security Tag - Only show for unknown contacts, not for saved contacts
            if (!call.isSavedContact && call.securityTag != SecurityTag.TRUSTED) {
                SecurityTagPill(securityTag = call.securityTag)
            }

            // Call Button with Tiffany Blue border only
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .border(2.dp, TiffanyBlue, CircleShape)
                    .clickable(onClick = onCallClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = "Call",
                    tint = TiffanyBlue,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun ContactCard(
    contact: com.secyra.ai.data.repository.Contact,
    onProfileClick: () -> Unit,
    onCallClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(88.dp)
            .clickable(onClick = onProfileClick),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF0A0A0A)
        ),
        border = BorderStroke(1.dp, Color(0xFF202020)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            val initials = if (contact.name.isNotEmpty()) {
                contact.name.first().toString().uppercase()
            } else {
                "C"
            }
            val avatarColor = getContactColor(contact.name, contact.phoneNumber)

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(avatarColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    color = SecyraWhite,
                    fontWeight = FontWeight.Medium,
                    fontSize = 22.sp
                )
            }

            // Name and phone number
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = contact.name.take(20),
                    color = SecyraWhite,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = contact.phoneNumber,
                    color = SecyraTextMuted,
                    fontSize = 13.sp
                )
            }

            // Call action button
            IconButton(
                onClick = onCallClick,
                modifier = Modifier.size(40.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .border(2.dp, TiffanyBlue, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = "Call",
                        tint = TiffanyBlue,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SecurityTagPill(securityTag: SecurityTag) {
    val label = when (securityTag) {
        SecurityTag.TRUSTED -> "Trusted"
        SecurityTag.UNKNOWN -> "Unknown"
        SecurityTag.SUSPICIOUS -> "Suspicious"
        SecurityTag.SPAM -> "Spam"
        SecurityTag.BLOCKED -> "Blocked"
    }

    val icon = when (securityTag) {
        SecurityTag.TRUSTED -> Icons.Default.Shield
        SecurityTag.UNKNOWN -> Icons.Default.Help
        SecurityTag.SUSPICIOUS -> Icons.Default.Warning
        SecurityTag.SPAM -> Icons.Default.Warning
        SecurityTag.BLOCKED -> Icons.Default.Block
    }

    val textColor = when (securityTag) {
        SecurityTag.TRUSTED -> Color(0xFF0ABAB5)
        SecurityTag.UNKNOWN -> Color(0xFFA0AEC0)
        SecurityTag.SUSPICIOUS -> Color(0xFFFFB020)
        SecurityTag.SPAM -> Color(0xFFFF7043)
        SecurityTag.BLOCKED -> Color(0xFFFF4D6D)
    }

    val backgroundColor = when (securityTag) {
        SecurityTag.TRUSTED -> Color(0xFF063B3B)
        SecurityTag.UNKNOWN -> Color(0xFF18212B)
        SecurityTag.SUSPICIOUS -> Color(0xFF35230B)
        SecurityTag.SPAM -> Color(0xFF351A14)
        SecurityTag.BLOCKED -> Color(0xFF35131D)
    }

    val borderColor = when (securityTag) {
        SecurityTag.TRUSTED -> Color(0xFF0A6B68)
        SecurityTag.UNKNOWN -> Color(0xFF293747)
        SecurityTag.SUSPICIOUS -> Color(0xFF6B470D)
        SecurityTag.SPAM -> Color(0xFF713322)
        SecurityTag.BLOCKED -> Color(0xFF702338)
    }

    Box(
        modifier = Modifier
            .wrapContentWidth()
            .height(26.dp)
            .background(
                backgroundColor,
                RoundedCornerShape(13.dp)
            )
            .border(
                width = 1.dp,
                color = borderColor.copy(alpha = 0.5f),
                shape = RoundedCornerShape(13.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
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
                text = label,
                color = textColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun ContactRowCard(
    contact: Contact,
    onProfileClick: () -> Unit,
    onCallClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clickable(onClick = onProfileClick),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF0A0A0A)
        ),
        border = BorderStroke(1.dp, Color(0xFF202020)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(TiffanyBlue.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (contact.name.isNotEmpty()) contact.name.first().toString().uppercase() else "C",
                    color = TiffanyBlue,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }

            // Center: Name and Phone Number
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = contact.name,
                    color = SecyraWhite,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
                Text(
                    text = contact.phoneNumber,
                    color = Color(0xFFA0A0A0),
                    fontSize = 13.sp
                )
            }

            // Call Button
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .border(
                        width = 1.dp,
                        color = TiffanyBlue,
                        shape = CircleShape
                    )
                    .clickable(onClick = onCallClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = "Call",
                    tint = TiffanyBlue,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun BlockedNumberCard(
    blocked: BlockedNumber,
    onUnblock: () -> Unit
) {
    val avatarColor = getContactColor(blocked.displayName, blocked.number)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = SecyraSurface
        ),
        border = BorderStroke(1.dp, RiskHigh.copy(alpha = 0.5f)),
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
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(avatarColor),
                    contentAlignment = Alignment.Center
                ) {
                    val initials = if (blocked.displayName != null && blocked.displayName.isNotEmpty()) {
                        blocked.displayName.first().toString().uppercase()
                    } else {
                        "B"
                    }
                    Text(
                        text = initials,
                        color = SecyraWhite,
                        fontWeight = FontWeight.Medium,
                        fontSize = 24.sp
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
}

@Composable
fun EmptyState(
    message: String,
    subMessage: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = SecyraSurface
        ),
        border = BorderStroke(1.dp, SecyraBorder),
        shape = RoundedCornerShape(20.dp)
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
}

@Composable
fun ContactsEmptyState() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = SecyraSurface
        ),
        border = BorderStroke(1.dp, SecyraBorder),
        shape = RoundedCornerShape(20.dp)
    ) {
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
}

@Composable
fun BlockedEmptyState() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = SecyraSurface
        ),
        border = BorderStroke(1.dp, SecyraBorder),
        shape = RoundedCornerShape(20.dp)
    ) {
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
