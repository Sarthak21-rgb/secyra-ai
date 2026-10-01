package com.secyra.ai.feature.message
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
import com.secyra.ai.domain.model.RiskLevel

@Composable
fun MessageSecurityScreen(
    modifier: Modifier = Modifier,
    viewModel: MessageSecurityViewModel = hiltViewModel()
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
            text = "Message Security",
            style = MaterialTheme.typography.headlineMedium,
            color = SecyraWhite,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Analyze messages for scams and phishing",
            style = MaterialTheme.typography.bodyMedium,
            color = SecyraTextSecondary
        )

        // Message input
        OutlinedTextField(
            value = uiState.message,
            onValueChange = { viewModel.onMessageChange(it) },
            label = { Text("Enter Message") },
            placeholder = { Text("Paste or type the message here...") },
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = TiffanyBlue,
                unfocusedBorderColor = SecyraBorder,
                cursorColor = TiffanyBlue,
                focusedTextColor = SecyraWhite,
                unfocusedTextColor = SecyraWhite
            ),
            shape = RoundedCornerShape(8.dp),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = SecyraTextSecondary
                )
            }
        )

        // Analyze button
        Button(
            onClick = { viewModel.analyzeMessage() },
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState.message.isNotBlank() && !uiState.isLoading,
            colors = ButtonDefaults.buttonColors(
                containerColor = TiffanyBlue,
                contentColor = SecyraWhite
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = SecyraWhite,
                    strokeWidth = 2.dp
                )
            } else {
                Text("Analyze Message")
            }
        }

        // Error message
        uiState.error?.let { error ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = RiskCritical.copy(alpha = 0.2f)
                ),
                border = BorderStroke(1.dp, RiskCritical)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = RiskCritical,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodyMedium,
                        color = SecyraWhite
                    )
                }
            }
        }

        // Analysis result
        uiState.analysisResult?.let { result ->
            MessageAnalysisResultCard(
                result = result,
                onClear = { viewModel.clearResult() }
            )
        }

        // Information card
        MessageInfoCard()
    }
}

@Composable
fun MessageAnalysisResultCard(
    result: MessageAnalysisResult,
    onClear: () -> Unit
) {
    val riskColor = when (result.riskLevel) {
        RiskLevel.LOW -> RiskLow
        RiskLevel.MODERATE -> RiskModerate
        RiskLevel.HIGH -> RiskHigh
        RiskLevel.CRITICAL -> RiskCritical
        RiskLevel.UNKNOWN -> SecyraTextMuted
    }

    val riskIcon = when (result.riskLevel) {
        RiskLevel.LOW -> Icons.Default.CheckCircle
        RiskLevel.MODERATE -> Icons.Default.Warning
        RiskLevel.HIGH -> Icons.Default.Warning
        RiskLevel.CRITICAL -> Icons.Default.Warning
        RiskLevel.UNKNOWN -> Icons.Default.Info
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = SecyraSurfaceElevated
        ),
        border = BorderStroke(1.dp, riskColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = riskIcon,
                        contentDescription = null,
                        tint = riskColor,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = result.title,
                        style = MaterialTheme.typography.titleLarge,
                        color = riskColor,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = onClear) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear",
                        tint = SecyraTextSecondary
                    )
                }
            }

            Divider(color = SecyraBorder)

            // Message preview
            Text(
                text = "Message",
                style = MaterialTheme.typography.labelMedium,
                color = SecyraTextMuted,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = if (result.message.length > 100) {
                    result.message.take(100) + "..."
                } else {
                    result.message
                },
                style = MaterialTheme.typography.bodyMedium,
                color = SecyraWhite
            )

            // Explanation
            Text(
                text = "Analysis",
                style = MaterialTheme.typography.labelMedium,
                color = SecyraTextMuted,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = result.explanation,
                style = MaterialTheme.typography.bodyMedium,
                color = SecyraTextSecondary
            )

            // Evidence
            if (result.evidence.isNotEmpty()) {
                Text(
                    text = "Suspicious Patterns Detected",
                    style = MaterialTheme.typography.labelMedium,
                    color = SecyraTextMuted,
                    fontWeight = FontWeight.Medium
                )
                result.evidence.forEach { evidence ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null,
                            tint = riskColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = evidence,
                            style = MaterialTheme.typography.bodySmall,
                            color = SecyraTextSecondary
                        )
                    }
                }
            }

            // Confidence
            Text(
                text = "Confidence: ${(result.confidence * 100).toInt()}%",
                style = MaterialTheme.typography.bodySmall,
                color = SecyraTextMuted
            )

            // Recommendation
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = when (result.riskLevel) {
                        RiskLevel.LOW -> RiskLow.copy(alpha = 0.1f)
                        RiskLevel.MODERATE -> RiskModerate.copy(alpha = 0.1f)
                        RiskLevel.HIGH -> RiskHigh.copy(alpha = 0.1f)
                        RiskLevel.CRITICAL -> RiskCritical.copy(alpha = 0.1f)
                        RiskLevel.UNKNOWN -> SecyraSurface.copy(alpha = 0.1f)
                    }
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    Text(
                        text = "Recommendation",
                        style = MaterialTheme.typography.labelMedium,
                        color = riskColor,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = result.recommendation,
                        style = MaterialTheme.typography.bodyMedium,
                        color = SecyraWhite
                    )
                }
            }

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { /* TODO: Copy message */ },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = SecyraTextSecondary
                    ),
                    border = BorderStroke(1.dp, SecyraBorder),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Copy")
                }
                OutlinedButton(
                    onClick = { /* TODO: Report */ },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = TiffanyBlue
                    ),
                    border = BorderStroke(1.dp, TiffanyBlue),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Report")
                }
            }
        }
    }
}

@Composable
fun MessageInfoCard() {
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
                    text = "Common Scam Patterns",
                    style = MaterialTheme.typography.titleSmall,
                    color = SecyraWhite,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Text(
                text = "• Urgency indicators (act now, limited time)\n• Fake payment requests\n• Impersonation of banks/government\n• Suspicious links and attachments\n• Requests for personal information",
                style = MaterialTheme.typography.bodySmall,
                color = SecyraTextSecondary
            )
        }
    }
}


