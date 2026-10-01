package com.secyra.ai.feature.settings

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.secyra.ai.core.ui.theme.*

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier
) {
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
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium,
            color = SecyraWhite,
            fontWeight = FontWeight.Bold
        )

        // Account Section
        SettingsSection(title = "Account") {
            SettingsItem(
                icon = Icons.Default.Person,
                title = "Account",
                description = "Manage your account",
                onClick = { /* TODO: Navigate to account */ }
            )
            SettingsItem(
                icon = Icons.Default.Info,
                title = "Subscription",
                description = "Manage your subscription",
                onClick = { /* TODO: Navigate to subscription */ }
            )
        }

        // Privacy Section
        SettingsSection(title = "Privacy") {
            SettingsItem(
                icon = Icons.Default.Info,
                title = "Privacy Center",
                description = "Privacy controls and data",
                onClick = { /* TODO: Navigate to privacy */ }
            )
            SettingsItem(
                icon = Icons.Default.Info,
                title = "Security History",
                description = "View your security analysis history",
                onClick = { /* TODO: Navigate to history */ }
            )
        }

        // Security Section
        SettingsSection(title = "Security") {
            SettingsItem(
                icon = Icons.Default.Lock,
                title = "Security Center",
                description = "Device and app security",
                onClick = { /* TODO: Navigate to security */ }
            )
            SettingsItem(
                icon = Icons.Default.Lock,
                title = "Permissions",
                description = "Manage app permissions",
                onClick = { /* TODO: Navigate to permissions */ }
            )
        }

        // AI Section
        SettingsSection(title = "AI Settings") {
            SettingsItem(
                icon = Icons.Default.Info,
                title = "AI Preferences",
                description = "Customize AI behavior",
                onClick = { /* TODO: Navigate to AI settings */ }
            )
            SettingsItem(
                icon = Icons.Default.Phone,
                title = "Voice Settings",
                description = "Voice interaction preferences",
                onClick = { /* TODO: Navigate to voice settings */ }
            )
        }

        // Notifications Section
        SettingsSection(title = "Notifications") {
            SettingsItem(
                icon = Icons.Default.Notifications,
                title = "Security Alerts",
                description = "Configure security notifications",
                onClick = { /* TODO: Navigate to notifications */ }
            )
        }

        // Support Section
        SettingsSection(title = "Support") {
            SettingsItem(
                icon = Icons.Default.Info,
                title = "Help & Support",
                description = "Get help with the app",
                onClick = { /* TODO: Navigate to help */ }
            )
            SettingsItem(
                icon = Icons.Default.Info,
                title = "Send Feedback",
                description = "Share your feedback",
                onClick = { /* TODO: Navigate to feedback */ }
            )
        }

        // About Section
        SettingsSection(title = "About") {
            SettingsItem(
                icon = Icons.Default.Info,
                title = "About SECYRA AI",
                description = "App version and information",
                onClick = { /* TODO: Navigate to about */ }
            )
            SettingsItem(
                icon = Icons.Default.Info,
                title = "Terms of Service",
                description = "Read our terms",
                onClick = { /* TODO: Navigate to terms */ }
            )
            SettingsItem(
                icon = Icons.Default.Info,
                title = "Privacy Policy",
                description = "Read our privacy policy",
                onClick = { /* TODO: Navigate to privacy policy */ }
            )
        }

        // App version
        Card(
            colors = CardDefaults.cardColors(
                containerColor = SecyraSurface
            ),
            border = BorderStroke(1.dp, SecyraBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Version",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SecyraTextSecondary
                )
                Text(
                    text = "1.0.0",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SecyraWhite
                )
            }
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = SecyraTextMuted,
            fontWeight = FontWeight.SemiBold
        )
        Card(
            colors = CardDefaults.cardColors(
                containerColor = SecyraSurfaceElevated
            ),
            border = BorderStroke(1.dp, SecyraBorder)
        ) {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                content()
            }
        }
    }
}

@Composable
fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(12.dp, 8.dp),
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
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
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
        Icon(
            imageVector = Icons.Default.ArrowForward,
            contentDescription = "Navigate",
            tint = SecyraTextMuted,
            modifier = Modifier.size(20.dp)
        )
    }
}


