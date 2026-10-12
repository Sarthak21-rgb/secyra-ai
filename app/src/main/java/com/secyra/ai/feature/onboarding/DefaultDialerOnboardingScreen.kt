package com.secyra.ai.feature.onboarding

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.secyra.ai.core.ui.theme.*

@Composable
fun DefaultDialerOnboardingScreen(
    viewModel: DefaultDialerOnboardingViewModel = hiltViewModel(),
    onComplete: () -> Unit = {},
    onSkip: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    // Permission launcher for ANSWER_PHONE_CALLS
    val answerCallPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.handleAnswerCallPermissionResult(isGranted)
    }

    LaunchedEffect(Unit) {
        viewModel.checkDefaultPhoneStatus()
    }

    LaunchedEffect(uiState.shouldRequestRole) {
        if (uiState.shouldRequestRole) {
            viewModel.requestDefaultPhoneRole()
        }
    }

    LaunchedEffect(uiState.shouldRequestAnswerCallPermission) {
        if (uiState.shouldRequestAnswerCallPermission) {
            answerCallPermissionLauncher.launch(android.Manifest.permission.ANSWER_PHONE_CALLS)
        }
    }

    LaunchedEffect(uiState.shouldComplete) {
        if (uiState.shouldComplete) {
            onComplete()
        }
    }

    LaunchedEffect(uiState.shouldSkip) {
        if (uiState.shouldSkip) {
            onSkip()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SecyraBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onSkip) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Skip",
                        tint = SecyraTextSecondary
                    )
                }
                Text(
                    text = "Set Up Phone Features",
                    style = MaterialTheme.typography.titleLarge,
                    color = SecyraWhite,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(48.dp))
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Icon
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .align(Alignment.CenterHorizontally)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                AzureBlue.copy(alpha = 0.3f),
                                TiffanyBlue.copy(alpha = 0.1f)
                            )
                        ),
                        shape = RoundedCornerShape(60.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = null,
                    tint = TiffanyBlue,
                    modifier = Modifier.size(64.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Title
            Text(
                text = "Make Secyra Your Default Phone App",
                style = MaterialTheme.typography.headlineMedium,
                color = SecyraWhite,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Description
            Text(
                text = "To provide a complete calling experience with custom incoming call screens, call controls, and security features, Secyra needs to be set as your default phone app.",
                style = MaterialTheme.typography.bodyLarge,
                color = SecyraTextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Benefits Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF0A1015)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    Color(0xFF1A2530)
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "What You'll Get",
                        style = MaterialTheme.typography.titleMedium,
                        color = SecyraWhite,
                        fontWeight = FontWeight.SemiBold
                    )

                    BenefitItem(
                        icon = Icons.Default.PhoneInTalk,
                        title = "Custom Incoming Call Screen",
                        description = "Beautiful Secyra-designed interface when calls come in"
                    )

                    BenefitItem(
                        icon = Icons.Default.Shield,
                        title = "Real-Time Call Protection",
                        description = "Spam detection and security warnings during calls"
                    )

                    BenefitItem(
                        icon = Icons.Default.Call,
                        title = "Complete Call Controls",
                        description = "Mute, speaker, hold, and call waiting support"
                    )

                    BenefitItem(
                        icon = Icons.Default.History,
                        title = "Integrated Call History",
                        description = "All your calls managed in one place"
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Important Note
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1A0A0A)
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = Color(0xFFFFAA33),
                        modifier = Modifier.size(24.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Important",
                            style = MaterialTheme.typography.titleSmall,
                            color = Color(0xFFFFAA33),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "You can change this setting anytime in Android system settings",
                            style = MaterialTheme.typography.bodySmall,
                            color = SecyraTextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Permission Info
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF0A1015)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Required Permission",
                        style = MaterialTheme.typography.titleSmall,
                        color = SecyraWhite,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "• Answer Phone Calls - Allows Secyra to answer and decline incoming calls",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SecyraTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Current Status
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (uiState.isDefaultPhoneApp) 
                        Color(0xFF0A2D20) 
                    else 
                        Color(0xFF1A0A0A)
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Current Status",
                            style = MaterialTheme.typography.titleSmall,
                            color = SecyraWhite,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (uiState.isDefaultPhoneApp) 
                                "Secyra is your default phone app" 
                            else 
                                "Secyra is not the default phone app",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (uiState.isDefaultPhoneApp) 
                                TiffanyBlue 
                            else 
                                SecyraTextSecondary
                        )
                    }
                    Icon(
                        imageVector = if (uiState.isDefaultPhoneApp) 
                            Icons.Default.CheckCircle 
                        else 
                            Icons.Default.Circle,
                        contentDescription = null,
                        tint = if (uiState.isDefaultPhoneApp) 
                            TiffanyBlue 
                        else 
                            SecyraTextMuted,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Action Buttons
            if (uiState.isDefaultPhoneApp) {
                Button(
                    onClick = { viewModel.complete() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TiffanyBlue
                    )
                ) {
                    Text(
                        text = "Continue",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Button(
                    onClick = { viewModel.requestDefaultPhoneRole() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TiffanyBlue
                    ),
                    enabled = uiState.canRequestRole && !uiState.isLoading
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.Black
                        )
                    } else {
                        Text(
                            text = "Set as Default Phone App",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = onSkip,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        SecyraTextMuted
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = SecyraTextSecondary
                    )
                ) {
                    Text(
                        text = "Skip for Now",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (uiState.errorMessage != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF2D0A0A)
                    )
                ) {
                    val errorMsg = uiState.errorMessage
                    Text(
                        text = errorMsg ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFFFAA33),
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
private fun BenefitItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    Color(0xFF0A1520),
                    RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = TiffanyBlue,
                modifier = Modifier.size(20.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
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
}
