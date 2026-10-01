package com.secyra.ai.feature.security
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
fun SecurityScreen(
    onNavigate: (Screen) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SecurityViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SecyraBackground)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Text(
            text = "Security Center",
            style = MaterialTheme.typography.headlineMedium,
            color = SecyraWhite,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Device security and recommendations",
            style = MaterialTheme.typography.bodyMedium,
            color = SecyraTextSecondary
        )

        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = TiffanyBlue,
                    strokeWidth = 2.dp
                )
            }
        } else {
            uiState.securityInfo?.let { info ->
                // Device Security Card
                DeviceSecurityCard(
                    info = info,
                    onRefresh = { viewModel.refreshSecurityInfo() }
                )

                // Security Items
                Text(
                    text = "Security Items",
                    style = MaterialTheme.typography.titleLarge,
                    color = SecyraWhite,
                    fontWeight = FontWeight.Bold
                )

                val securityItems = createSecurityItems(info)
                securityItems.forEach { item ->
                    SecurityItemCard(
                        item = item,
                        onClick = { viewModel.onSecurityItemClicked(item) }
                    )
                }

                // Quick Actions
                Text(
                    text = "Quick Actions",
                    style = MaterialTheme.typography.titleLarge,
                    color = SecyraWhite,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { /* TODO: Implement full security scan */ },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = TiffanyBlue
                        ),
                        border = BorderStroke(1.dp, TiffanyBlue),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Full Scan")
                    }
                    OutlinedButton(
                        onClick = { onNavigate(Screen.Privacy) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = SecyraTextSecondary
                        ),
                        border = BorderStroke(1.dp, SecyraBorder),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Privacy")
                    }
                }
            }
        }
    }
}

@Composable
fun DeviceSecurityCard(
    info: SecurityInfo,
    onRefresh: () -> Unit
) {
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Device Security",
                    style = MaterialTheme.typography.titleMedium,
                    color = SecyraWhite,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onRefresh) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = SecyraTextSecondary
                    )
                }
            }

            Divider(color = SecyraBorder)

            SecurityInfoRow(
                icon = Icons.Default.Info,
                label = "Android Version",
                value = info.androidVersion
            )
            SecurityInfoRow(
                icon = Icons.Default.Lock,
                label = "Security Patch",
                value = info.securityPatch
            )
            SecurityInfoRow(
                icon = Icons.Default.Lock,
                label = "Screen Lock",
                value = if (info.screenLockEnabled) "Enabled" else "Disabled",
                isSecure = info.screenLockEnabled
            )
            SecurityInfoRow(
                icon = Icons.Default.Warning,
                label = "Unknown Sources",
                value = if (info.unknownSourcesEnabled) "Enabled" else "Disabled",
                isSecure = !info.unknownSourcesEnabled
            )
            SecurityInfoRow(
                icon = Icons.Default.Info,
                label = "Developer Options",
                value = if (info.developerOptionsEnabled) "Enabled" else "Disabled",
                isSecure = !info.developerOptionsEnabled
            )
            SecurityInfoRow(
                icon = Icons.Default.Lock,
                label = "VPN Status",
                value = if (info.vpnConnected) "Connected" else "Not Connected"
            )
        }
    }
}

@Composable
fun SecurityInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    isSecure: Boolean? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = SecyraTextSecondary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = SecyraTextSecondary
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = when (isSecure) {
                true -> RiskLow
                false -> RiskHigh
                null -> SecyraWhite
            },
            fontWeight = if (isSecure != null) FontWeight.Medium else FontWeight.Normal
        )
    }
}

@Composable
fun SecurityItemCard(
    item: SecurityItem,
    onClick: () -> Unit
) {
    val statusColor = when (item.status) {
        SecurityStatus.SECURE -> RiskLow
        SecurityStatus.WARNING -> RiskModerate
        SecurityStatus.RISK -> RiskHigh
        SecurityStatus.UNKNOWN -> SecyraTextMuted
    }

    val statusIcon = when (item.status) {
        SecurityStatus.SECURE -> Icons.Default.CheckCircle
        SecurityStatus.WARNING -> Icons.Default.Warning
        SecurityStatus.RISK -> Icons.Default.Warning
        SecurityStatus.UNKNOWN -> Icons.Default.Info
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = SecyraSurface
        ),
        border = BorderStroke(1.dp, statusColor)
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
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = statusIcon,
                    contentDescription = null,
                    tint = statusColor,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.bodyMedium,
                        color = SecyraWhite,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = item.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = SecyraTextSecondary
                    )
                }
            }
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "View details",
                tint = SecyraTextMuted
            )
        }
    }
}

fun createSecurityItems(info: SecurityInfo): List<SecurityItem> {
    return listOf(
        SecurityItem(
            id = "screen_lock",
            title = "Screen Lock",
            description = if (info.screenLockEnabled) "Screen lock is enabled" else "Screen lock is disabled",
            status = if (info.screenLockEnabled) SecurityStatus.SECURE else SecurityStatus.RISK,
            recommendation = if (info.screenLockEnabled) "Your device is protected with screen lock" else "Enable screen lock for better security"
        ),
        SecurityItem(
            id = "unknown_sources",
            title = "Unknown Sources",
            description = if (info.unknownSourcesEnabled) "Installation from unknown sources enabled" else "Installation from unknown sources disabled",
            status = if (info.unknownSourcesEnabled) SecurityStatus.RISK else SecurityStatus.SECURE,
            recommendation = if (info.unknownSourcesEnabled) "Disable unknown sources for better security" else "Your device is protected from unknown app sources"
        ),
        SecurityItem(
            id = "developer_options",
            title = "Developer Options",
            description = if (info.developerOptionsEnabled) "Developer options are enabled" else "Developer options are disabled",
            status = if (info.developerOptionsEnabled) SecurityStatus.WARNING else SecurityStatus.SECURE,
            recommendation = if (info.developerOptionsEnabled) "Disable developer options if not needed" else "Your device is more secure with developer options disabled"
        ),
        SecurityItem(
            id = "device_admin",
            title = "Device Admin Apps",
            description = if (info.hasDeviceAdmin) "Device admin apps are present" else "No device admin apps",
            status = if (info.hasDeviceAdmin) SecurityStatus.WARNING else SecurityStatus.SECURE,
            recommendation = if (info.hasDeviceAdmin) "Review device admin apps" else "No concerning device admin apps found"
        ),
        SecurityItem(
            id = "vpn",
            title = "VPN Connection",
            description = if (info.vpnConnected) "VPN is currently connected" else "No VPN connection",
            status = SecurityStatus.UNKNOWN,
            recommendation = if (info.vpnConnected) "VPN is active for network security" else "Consider using VPN for better privacy"
        ),
        SecurityItem(
            id = "accessibility",
            title = "Accessibility Services",
            description = "${info.accessibilityServices} accessibility services enabled",
            status = if (info.accessibilityServices > 2) SecurityStatus.WARNING else SecurityStatus.SECURE,
            recommendation = if (info.accessibilityServices > 2) "Review accessibility services for security" else "Accessibility services are within normal range"
        )
    )
}



