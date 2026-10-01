package com.secyra.ai.feature.scan

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
fun ScanScreen(
    modifier: Modifier = Modifier,
    viewModel: ScanViewModel = hiltViewModel(),
    onNavigate: (Screen) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    // Handle navigation
    LaunchedEffect(uiState.selectedScanType) {
        uiState.selectedScanType?.let { type ->
            when (type) {
                ScanType.LINK -> onNavigate(Screen.LinkSecurity)
                ScanType.QR -> onNavigate(Screen.Scan) // TODO: Implement QR scanning
                ScanType.MESSAGE -> onNavigate(Screen.MessageSecurity)
                ScanType.SCREENSHOT -> onNavigate(Screen.ProSubscription) // Pro feature
                ScanType.FILE -> onNavigate(Screen.Scan) // TODO: Implement file scanning
                ScanType.PHONE -> onNavigate(Screen.Calls)
            }
            viewModel.onNavigationHandled()
        }
    }

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
            text = "Scan",
            style = MaterialTheme.typography.headlineMedium,
            color = SecyraWhite,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Choose what to scan",
            style = MaterialTheme.typography.bodyMedium,
            color = SecyraTextSecondary
        )

        // Scan options
        ScanOptionCard(
            icon = Icons.Default.Info,
            title = "Scan Link",
            description = "Check URL for security risks",
            onClick = { viewModel.selectScanType(ScanType.LINK) }
        )

        ScanOptionCard(
            icon = Icons.Default.Info,
            title = "Scan QR Code",
            description = "Decode and analyze QR codes",
            onClick = { viewModel.selectScanType(ScanType.QR) }
        )

        ScanOptionCard(
            icon = Icons.Default.Info,
            title = "Analyze Message",
            description = "Check message for scam patterns",
            onClick = { viewModel.selectScanType(ScanType.MESSAGE) }
        )

        ScanOptionCard(
            icon = Icons.Default.Info,
            title = "Scan Screenshot",
            description = "AI-powered image analysis (Pro)",
            isPro = true,
            onClick = { viewModel.selectScanType(ScanType.SCREENSHOT) }
        )

        ScanOptionCard(
            icon = Icons.Default.Info,
            title = "Scan File",
            description = "Check file for security issues",
            onClick = { viewModel.selectScanType(ScanType.FILE) }
        )

        ScanOptionCard(
            icon = Icons.Default.Phone,
            title = "Check Phone Number",
            description = "Analyze number for spam/scam",
            onClick = { viewModel.selectScanType(ScanType.PHONE) }
        )
    }
}

@Composable
fun ScanOptionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    isPro: Boolean = false,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = SecyraSurfaceElevated
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = TiffanyBlue,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            color = SecyraWhite,
                            fontWeight = FontWeight.Medium
                        )
                        if (isPro) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = TiffanyBlue.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "PRO",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TiffanyBlue,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = SecyraTextSecondary
                    )
                }
            }
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null,
                tint = SecyraTextMuted
            )
        }
    }
}
