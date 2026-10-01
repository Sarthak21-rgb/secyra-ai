package com.secyra.ai.feature.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.secyra.ai.R
import com.secyra.ai.core.ui.theme.*
import com.secyra.ai.domain.model.RiskLevel
import com.secyra.ai.navigation.Screen
import java.util.Calendar

@Composable
fun HomeScreen(
    onNavigate: (Screen) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Handle navigation events
    LaunchedEffect(uiState.navigationEvent) {
        uiState.navigationEvent?.let { event ->
            when (event) {
                is NavigationEvent.NavigateToLinkSecurity -> onNavigate(Screen.LinkSecurity)
                is NavigationEvent.NavigateToSecurity -> onNavigate(Screen.Security)
                is NavigationEvent.NavigateToAI -> onNavigate(Screen.AI)
                is NavigationEvent.NavigateToProSubscription -> onNavigate(Screen.ProSubscription)
                is NavigationEvent.NavigateToScan -> onNavigate(Screen.Scan)
                is NavigationEvent.NavigateToCalls -> onNavigate(Screen.Calls)
                is NavigationEvent.NavigateToMessageSecurity -> onNavigate(Screen.MessageSecurity)
                is NavigationEvent.NavigateToNotifications -> onNavigate(Screen.Notifications)
                is NavigationEvent.NavigateToProfile -> onNavigate(Screen.Profile)
                is NavigationEvent.NavigateToAllActions -> onNavigate(Screen.AllActions)
                is NavigationEvent.NavigateToActivityHistory -> onNavigate(Screen.ActivityHistory)
            }
            viewModel.onNavigationEventHandled()
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(SecyraBackground)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 120.dp) // Space for bottom nav + system nav
    ) {
        // SECYRA Header
        item {
            SecyraHeader(
                onNotificationClick = { viewModel.onNotificationClick() },
                onProfileClick = { viewModel.onProfileClick() }
            )
        }

        // Greeting
        item {
            TimeAwareGreeting(userName = uiState.userName)
        }

        // Security Status Card
        item {
            SecurityStatusCard(
                securityScore = uiState.securityScore,
                securityStatus = uiState.securityStatus,
                onViewSecurityCenter = { viewModel.onViewSecurityCenter() }
            )
        }

        // Security Alert Card (only when real alerts exist)
        if (uiState.hasSecurityAlert) {
            item {
                SecurityAlertCard(
                    alertCount = uiState.alertCount,
                    onViewAlerts = { viewModel.onViewAlerts() }
                )
            }
        }

        // Quick Actions - 2x2 Grid
        item {
            QuickActionsSection(
                onLinkScan = { viewModel.onQuickActionClick(QuickAction.CheckLink) },
                onCheckWeb = { viewModel.onQuickActionClick(QuickAction.CheckWeb) },
                onCheckCall = { viewModel.onQuickActionClick(QuickAction.CheckNumber) },
                onCheckMessage = { viewModel.onQuickActionClick(QuickAction.AnalyzeMessage) },
                onViewAll = { viewModel.onViewAllActions() }
            )
        }

        // Recent Activity
        item {
            RecentActivitySection(
                activities = uiState.recentActivity,
                onActivityClick = { activity -> viewModel.onActivityClick(activity) },
                onViewAll = { viewModel.onViewActivityHistory() }
            )
        }

        // Ask Secyra AI Box
        item {
            AskSecyraAIBox(
                onAskSecyra = { viewModel.onQuickActionClick(QuickAction.TalkToSecyra) }
            )
        }
    }
}

@Composable
fun SecyraHeader(
    onNotificationClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // SECYRA shield logo (using vector drawable)
            Icon(
                painter = painterResource(R.drawable.ic_shield_logo_large),
                contentDescription = "SECYRA Shield",
                tint = TiffanyBlue,
                modifier = Modifier.size(36.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "SECYRA",
                        style = MaterialTheme.typography.headlineSmall,
                        color = SecyraWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                    Text(
                        text = "AI",
                        style = MaterialTheme.typography.headlineSmall,
                        color = TiffanyBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }
                Text(
                    text = "See. Understand. Stay Secure.",
                    style = MaterialTheme.typography.bodySmall,
                    color = SecyraTextSecondary,
                    fontSize = 12.sp
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            // Notifications icon
            IconButton(
                onClick = onNotificationClick,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notifications",
                    tint = TiffanyBlue,
                    modifier = Modifier.size(28.dp)
                )
            }
            // Profile icon
            IconButton(
                onClick = onProfileClick,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Profile",
                    tint = AzureBlue,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
fun TimeAwareGreeting(userName: String) {
    val calendar = Calendar.getInstance()
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    
    val greeting = when (hour) {
        in 0..11 -> "Good morning"
        in 12..17 -> "Good afternoon"
        in 18..23 -> "Good evening"
        else -> "Good night"
    }
    
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = "$greeting,",
            style = MaterialTheme.typography.headlineMedium,
            color = SecyraWhite,
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp
        )
        Text(
            text = userName,
            style = MaterialTheme.typography.headlineMedium,
            color = TiffanyBlue,
            fontWeight = FontWeight.Bold,
            fontSize = 30.sp
        )
        Text(
            text = "Your security is our priority.",
            style = MaterialTheme.typography.bodyLarge,
            color = SecyraTextSecondary,
            fontSize = 16.sp
        )
    }
}

@Composable
fun SecurityStatusCard(
    securityScore: Int,
    securityStatus: SecurityStatus,
    onViewSecurityCenter: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(155.dp)
            .clickable(onClick = onViewSecurityCenter),
        colors = CardDefaults.cardColors(
            containerColor = SecyraSurface
        ),
        border = BorderStroke(2.dp, TiffanyBlue),
        shape = RoundedCornerShape(24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Security Score Ring (LEFT)
            SecurityScoreRing(
                score = securityScore,
                modifier = Modifier.size(110.dp)
            )
            
            Spacer(modifier = Modifier.width(24.dp))
            
            // Status and Shield (RIGHT)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Security Score",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SecyraTextSecondary,
                    fontWeight = FontWeight.Medium,
                    fontSize = 18.sp
                )
                
                // Shield icon
                Icon(
                    painter = painterResource(R.drawable.ic_shield_logo_large),
                    contentDescription = null,
                    tint = TiffanyBlue,
                    modifier = Modifier.size(24.dp)
                )
                
                Text(
                    text = securityStatus.displayText,
                    style = MaterialTheme.typography.headlineSmall,
                    color = TiffanyBlue,
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp
                )
                
                Text(
                    text = securityStatus.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = SecyraTextSecondary,
                    fontSize = 16.sp
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Chevron button
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(TiffanyBlue.copy(alpha = 0.1f))
                    .clickable(onClick = onViewSecurityCenter),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "View Security Center",
                    tint = TiffanyBlue,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun SecurityScoreRing(
    score: Int,
    modifier: Modifier = Modifier
) {
    val progress = score / 100f
    
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxSize(),
            color = TiffanyBlue,
            strokeWidth = 12.dp,
            trackColor = SecyraBorder,
            strokeCap = StrokeCap.Round
        )
        
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = score.toString(),
                style = MaterialTheme.typography.displayLarge,
                color = SecyraWhite,
                fontWeight = FontWeight.Bold,
                fontSize = 36.sp
            )
            Text(
                text = "/100",
                style = MaterialTheme.typography.bodyLarge,
                color = SecyraTextSecondary,
                fontSize = 18.sp
            )
        }
    }
}

@Composable
fun SecurityAlertCard(
    alertCount: Int,
    onViewAlerts: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onViewAlerts),
        colors = CardDefaults.cardColors(
            containerColor = SecyraSurface
        ),
        border = BorderStroke(1.dp, RiskModerate),
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
                Icon(
                    painter = painterResource(R.drawable.ic_shield_logo_large),
                    contentDescription = null,
                    tint = RiskModerate,
                    modifier = Modifier.size(32.dp)
                )
                Column {
                    Text(
                        text = "$alertCount security alert${if (alertCount > 1) "s" else ""} found",
                        style = MaterialTheme.typography.titleMedium,
                        color = SecyraWhite,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp
                    )
                    Text(
                        text = "Review and take action to stay safe.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SecyraTextSecondary,
                        fontSize = 14.sp
                    )
                }
            }
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null,
                tint = SecyraTextMuted,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun QuickActionsSection(
    onLinkScan: () -> Unit,
    onCheckWeb: () -> Unit,
    onCheckCall: () -> Unit,
    onCheckMessage: () -> Unit,
    onViewAll: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Quick Actions",
                style = MaterialTheme.typography.titleLarge,
                color = SecyraWhite,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "View all",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TiffanyBlue,
                    fontSize = 13.sp
                )
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    tint = TiffanyBlue,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        
        // 2x2 Grid - NO horizontal scrolling
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp)
        ) {
            item {
                QuickActionCard(
                    icon = Icons.Default.Info,
                    title = "Link Scan",
                    description = "Check suspicious links",
                    onClick = onLinkScan,
                    borderColor = TiffanyBlue
                )
            }
            item {
                QuickActionCard(
                    icon = Icons.Default.Search,
                    title = "Screen Scan",
                    description = "Go on any page live to check for threat",
                    onClick = onCheckWeb,
                    borderColor = TiffanyBlue
                )
            }
            item {
                QuickActionCard(
                    icon = Icons.Default.Phone,
                    title = "Check Call",
                    description = "Analyze calls & caller ID",
                    onClick = onCheckCall,
                    borderColor = AzureBlue
                )
            }
            item {
                QuickActionCard(
                    icon = Icons.Default.Info,
                    title = "Check Message",
                    description = "Detect spam & malware",
                    onClick = onCheckMessage,
                    borderColor = AzureBlue
                )
            }
        }
    }
}

@Composable
fun QuickActionCard(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit,
    borderColor: Color = SecyraBorder
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .clickable(onClick = onClick)
            .drawBehind {
                drawRoundRect(
                    color = borderColor.copy(alpha = 0.3f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(
                        x = 20.dp.toPx(),
                        y = 20.dp.toPx()
                    ),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                        width = 4.dp.toPx()
                    )
                )
            },
        colors = CardDefaults.cardColors(
            containerColor = SecyraSurface
        ),
        border = BorderStroke(2.dp, borderColor),
        shape = RoundedCornerShape(20.dp)
    ) {
        Box {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Icon container
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(borderColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = borderColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = SecyraWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                )
                
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = SecyraTextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 16.sp
                )
            }
            
            // Arrow button
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .size(40.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(borderColor.copy(alpha = 0.1f))
                    .clickable(onClick = onClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    tint = borderColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun RecentActivitySection(
    activities: List<SecurityActivityItem>,
    onActivityClick: (SecurityActivityItem) -> Unit,
    onViewAll: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recent Activity",
                style = MaterialTheme.typography.titleLarge,
                color = SecyraWhite,
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "View all",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TiffanyBlue,
                    fontSize = 18.sp
                )
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    tint = TiffanyBlue,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        
        if (activities.isEmpty()) {
            // Compact empty state
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = SecyraSurface
                ),
                border = BorderStroke(1.dp, AzureBlue.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(24.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(SecyraBorder),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = SecyraTextMuted,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "No recent activity",
                            style = MaterialTheme.typography.bodyLarge,
                            color = SecyraWhite,
                            fontWeight = FontWeight.Medium,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "Your recent security activity will appear here.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SecyraTextSecondary,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                activities.take(3).forEach { activity ->
                    SecurityActivityItemRow(
                        activity = activity,
                        onClick = { onActivityClick(activity) }
                    )
                }
            }
        }
    }
}

@Composable
fun SecurityActivityItemRow(
    activity: SecurityActivityItem,
    onClick: () -> Unit
) {
    val iconColor = when (activity.riskLevel) {
        RiskLevel.LOW -> RiskLow
        RiskLevel.MODERATE -> RiskModerate
        RiskLevel.HIGH -> RiskHigh
        RiskLevel.CRITICAL -> RiskCritical
        RiskLevel.UNKNOWN -> SecyraTextMuted
    }
    
    val statusIcon = when (activity.riskLevel) {
        RiskLevel.LOW -> Icons.Default.Check
        RiskLevel.MODERATE -> Icons.Default.Warning
        RiskLevel.HIGH -> Icons.Default.Warning
        RiskLevel.CRITICAL -> Icons.Default.Warning
        RiskLevel.UNKNOWN -> Icons.Default.Info
    }
    
    val statusText = when (activity.riskLevel) {
        RiskLevel.LOW -> "Safe"
        RiskLevel.MODERATE -> "Warning"
        RiskLevel.HIGH -> "Alert"
        RiskLevel.CRITICAL -> "Critical"
        RiskLevel.UNKNOWN -> "Unknown"
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = SecyraSurface
        ),
        border = BorderStroke(1.dp, SecyraBorder),
        shape = RoundedCornerShape(16.dp)
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
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = statusIcon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = activity.type,
                        style = MaterialTheme.typography.bodyLarge,
                        color = SecyraWhite,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "${activity.description} ${activity.timestamp}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SecyraTextSecondary,
                        fontSize = 13.sp
                    )
                }
            }
            Text(
                text = statusText,
                style = MaterialTheme.typography.labelLarge,
                color = iconColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun AskSecyraAIBox(
    onAskSecyra: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(85.dp)
            .clickable(onClick = onAskSecyra),
        colors = CardDefaults.cardColors(
            containerColor = SecyraSurface
        ),
        border = BorderStroke(2.dp, TiffanyBlue),
        shape = RoundedCornerShape(24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Shield logo on left
            Icon(
                painter = painterResource(R.drawable.ic_shield_logo_large),
                contentDescription = null,
                tint = TiffanyBlue,
                modifier = Modifier.size(32.dp)
            )
            
            // Centered text
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Need Help?",
                    style = MaterialTheme.typography.titleMedium,
                    color = SecyraWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                )
                Text(
                    text = "Ask Secyra AI for any security concern.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SecyraTextSecondary,
                    fontSize = 16.sp
                )
            }
            
            // Send button on right
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(TiffanyBlue)
                    .clickable(onClick = onAskSecyra),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
