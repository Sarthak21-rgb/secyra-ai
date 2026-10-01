package com.secyra.ai.feature.privacy
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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

@Composable
fun PrivacyScreen(
    modifier: Modifier = Modifier,
    viewModel: PrivacyViewModel = hiltViewModel()
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
            text = "Privacy Center",
            style = MaterialTheme.typography.headlineMedium,
            color = SecyraWhite,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Control your data and privacy settings",
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
            uiState.privacyInfo?.let { info ->
                // Permissions Status
                PermissionsCard(info)

                // Data Settings
                DataSettingsCard(
                    info = info,
                    onToggleHistoryRetention = { viewModel.toggleHistoryRetention(it) },
                    onToggleAnalytics = { viewModel.toggleAnalytics(it) },
                    onToggleCrashReporting = { viewModel.toggleCrashReporting(it) }
                )

                // Data Management
                DataManagementCard(
                    onClearHistory = { viewModel.clearHistory() },
                    onExportData = { viewModel.exportData() }
                )

                // Privacy Information
                PrivacyInfoCard()
            }
        }
    }
}

@Composable
fun PermissionsCard(info: PrivacyInfo) {
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = TiffanyBlue,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Permissions Status",
                    style = MaterialTheme.typography.titleMedium,
                    color = SecyraWhite,
                    fontWeight = FontWeight.Bold
                )
            }

            Divider(color = SecyraBorder)

            PermissionRow(
                icon = Icons.Default.Info,
                label = "Camera",
                granted = info.cameraPermissionGranted
            )
            PermissionRow(
                icon = Icons.Default.Phone,
                label = "Microphone",
                granted = info.microphonePermissionGranted
            )
            PermissionRow(
                icon = Icons.Default.LocationOn,
                label = "Location",
                granted = info.locationPermissionGranted
            )
            PermissionRow(
                icon = Icons.Default.Info,
                label = "Contacts",
                granted = info.contactsPermissionGranted
            )
            PermissionRow(
                icon = Icons.Default.Info,
                label = "Storage",
                granted = info.storagePermissionGranted
            )
        }
    }
}

@Composable
fun PermissionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    granted: Boolean
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
                color = SecyraWhite
            )
        }
        Icon(
            imageVector = if (granted) Icons.Default.CheckCircle else Icons.Default.Close,
            contentDescription = null,
            tint = if (granted) RiskLow else RiskHigh,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun DataSettingsCard(
    info: PrivacyInfo,
    onToggleHistoryRetention: (Boolean) -> Unit,
    onToggleAnalytics: (Boolean) -> Unit,
    onToggleCrashReporting: (Boolean) -> Unit
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = TiffanyBlue,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Data Settings",
                    style = MaterialTheme.typography.titleMedium,
                    color = SecyraWhite,
                    fontWeight = FontWeight.Bold
                )
            }

            Divider(color = SecyraBorder)

            SettingToggleRow(
                icon = Icons.Default.Info,
                label = "History Retention",
                description = "Keep security analysis history",
                enabled = info.historyRetentionEnabled,
                onToggle = onToggleHistoryRetention
            )
            SettingToggleRow(
                icon = Icons.Default.Info,
                label = "Analytics",
                description = "Help improve the app",
                enabled = info.analyticsEnabled,
                onToggle = onToggleAnalytics
            )
            SettingToggleRow(
                icon = Icons.Default.Info,
                label = "Crash Reporting",
                description = "Help fix issues automatically",
                enabled = info.crashReportingEnabled,
                onToggle = onToggleCrashReporting
            )
        }
    }
}

@Composable
fun SettingToggleRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    description: String,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = SecyraTextSecondary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = SecyraWhite
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = SecyraTextSecondary
                )
            }
        }
        Switch(
            checked = enabled,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = TiffanyBlue,
                checkedTrackColor = TiffanyBlue.copy(alpha = 0.5f),
                uncheckedThumbColor = SecyraTextMuted,
                uncheckedTrackColor = SecyraBorder
            )
        )
    }
}

@Composable
fun DataManagementCard(
    onClearHistory: () -> Unit,
    onExportData: () -> Unit
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = TiffanyBlue,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Data Management",
                    style = MaterialTheme.typography.titleMedium,
                    color = SecyraWhite,
                    fontWeight = FontWeight.Bold
                )
            }

            Divider(color = SecyraBorder)

            OutlinedButton(
                onClick = onClearHistory,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = RiskHigh
                ),
                border = BorderStroke(1.dp, RiskHigh),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Clear History")
            }

            OutlinedButton(
                onClick = onExportData,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = TiffanyBlue
                ),
                border = BorderStroke(1.dp, TiffanyBlue),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Export Data")
            }
        }
    }
}

@Composable
fun PrivacyInfoCard() {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = SecyraSurface
        ),
        border = BorderStroke(1.dp, SecyraBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = SecyraTextMuted,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Privacy Information",
                    style = MaterialTheme.typography.titleSmall,
                    color = SecyraWhite,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Text(
                text = "• Your security analysis is processed locally when possible\n• AI features require server communication\n• We do not collect passwords, OTPs, or sensitive credentials\n• You can delete your history at any time\n• Export your data to understand what we store",
                style = MaterialTheme.typography.bodySmall,
                color = SecyraTextSecondary
            )
        }
    }
}


