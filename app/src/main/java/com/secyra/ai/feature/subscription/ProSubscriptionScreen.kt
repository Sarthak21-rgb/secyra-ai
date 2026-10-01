package com.secyra.ai.feature.subscription

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.secyra.ai.core.ui.theme.*

@Composable
fun ProSubscriptionScreen(
    modifier: Modifier = Modifier,
    viewModel: ProSubscriptionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SecyraBackground)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Header
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = TiffanyBlue,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Unlock SECYRA AI",
                style = MaterialTheme.typography.headlineMedium,
                color = SecyraWhite,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = "AI-Powered Security Analysis",
                style = MaterialTheme.typography.bodyLarge,
                color = SecyraTextSecondary,
                textAlign = TextAlign.Center
            )
        }

        // Pro Features
        ProFeaturesCard()

        // Pricing
        PricingSection(
            isProUser = uiState.isProUser,
            isLoading = uiState.isLoading,
            onPurchaseMonthlyIndia = { viewModel.purchaseMonthlyIndia() },
            onPurchaseAnnualIndia = { viewModel.purchaseAnnualIndia() },
            onPurchaseMonthlyGlobal = { viewModel.purchaseMonthlyGlobal() },
            onPurchaseAnnualGlobal = { viewModel.purchaseAnnualGlobal() }
        )

        // Restore Purchases
        OutlinedButton(
            onClick = { viewModel.restorePurchases() },
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLoading,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = TiffanyBlue
            ),
            border = BorderStroke(1.dp, TiffanyBlue),
            shape = RoundedCornerShape(8.dp)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = TiffanyBlue,
                    strokeWidth = 2.dp
                )
            } else {
                Text("Restore Purchases")
            }
        }

        // Purchase Result
        uiState.purchaseResult?.let { result ->
            when (result) {
                is PurchaseResult.Success -> {
                    PurchaseSuccessCard(onDismiss = { viewModel.clearPurchaseResult() })
                }
                is PurchaseResult.Restored -> {
                    PurchaseSuccessCard(
                        message = "Purchases restored successfully",
                        onDismiss = { viewModel.clearPurchaseResult() }
                    )
                }
                is PurchaseResult.Error -> {
                    PurchaseErrorCard(
                        message = result.message,
                        onDismiss = { viewModel.clearPurchaseResult() }
                    )
                }
            }
        }

        // RevenueCat configuration notice
        Card(
            colors = CardDefaults.cardColors(
                containerColor = RiskModerate.copy(alpha = 0.2f)
            ),
            border = BorderStroke(1.dp, RiskModerate)
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
                        tint = RiskModerate,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Subscription Configuration",
                        style = MaterialTheme.typography.titleSmall,
                        color = RiskModerate,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "RevenueCat integration is not currently configured. The app is running in free mode. Premium features will be available when subscription is properly configured.",
                    style = MaterialTheme.typography.bodySmall,
                    color = SecyraWhite
                )
            }
        }

        // Terms and Privacy
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TextButton(onClick = { /* TODO: Navigate to Terms */ }) {
                Text("Terms", color = SecyraTextSecondary)
            }
            TextButton(onClick = { /* TODO: Navigate to Privacy */ }) {
                Text("Privacy", color = SecyraTextSecondary)
            }
        }
    }
}

@Composable
fun ProFeaturesCard() {
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
            Text(
                text = "Pro Features",
                style = MaterialTheme.typography.titleLarge,
                color = SecyraWhite,
                fontWeight = FontWeight.Bold
            )

            ProFeature(
                icon = Icons.Default.Info,
                title = "AI Security Chat",
                description = "Talk to Secyra AI about security concerns"
            )
            ProFeature(
                icon = Icons.Default.Info,
                title = "Voice AI",
                description = "Voice interaction with security assistant"
            )
            ProFeature(
                icon = Icons.Default.Info,
                title = "Screen Security Session",
                description = "Real-time AI screen analysis"
            )
            ProFeature(
                icon = Icons.Default.Info,
                title = "Screenshot Analysis",
                description = "AI-powered image security analysis"
            )
            ProFeature(
                icon = Icons.Default.Info,
                title = "Advanced Reports",
                description = "Detailed security analysis and reports"
            )
        }
    }
}

@Composable
fun ProFeature(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = TiffanyBlue,
            modifier = Modifier.size(24.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
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
}

@Composable
fun PricingSection(
    isProUser: Boolean,
    isLoading: Boolean,
    onPurchaseMonthlyIndia: () -> Unit,
    onPurchaseAnnualIndia: () -> Unit,
    onPurchaseMonthlyGlobal: () -> Unit,
    onPurchaseAnnualGlobal: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // India Pricing
        PricingCard(
            title = "India Pricing",
            monthlyPrice = "₹149/month",
            annualPrice = "₹1,499/year",
            annualSavings = "Save ₹289",
            isProUser = isProUser,
            isLoading = isLoading,
            onPurchaseMonthly = onPurchaseMonthlyIndia,
            onPurchaseAnnual = onPurchaseAnnualIndia
        )

        // Global Pricing
        PricingCard(
            title = "Global Pricing",
            monthlyPrice = "$2/month",
            annualPrice = "$20/year",
            annualSavings = "Save $4",
            isProUser = isProUser,
            isLoading = isLoading,
            onPurchaseMonthly = onPurchaseMonthlyGlobal,
            onPurchaseAnnual = onPurchaseAnnualGlobal
        )
    }
}

@Composable
fun PricingCard(
    title: String,
    monthlyPrice: String,
    annualPrice: String,
    annualSavings: String,
    isProUser: Boolean,
    isLoading: Boolean,
    onPurchaseMonthly: () -> Unit,
    onPurchaseAnnual: () -> Unit
) {
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = SecyraWhite,
                fontWeight = FontWeight.Bold
            )

            if (isProUser) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = RiskLow.copy(alpha = 0.2f)
                    ),
                    border = BorderStroke(1.dp, RiskLow)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = RiskLow
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "You are a Pro user",
                            style = MaterialTheme.typography.bodyMedium,
                            color = RiskLow,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else {
                // Monthly Option
                Button(
                    onClick = onPurchaseMonthly,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SecyraSurfaceElevated,
                        contentColor = SecyraWhite
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = monthlyPrice,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Annual Option
                Button(
                    onClick = onPurchaseAnnual,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TiffanyBlue,
                        contentColor = SecyraWhite
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = annualPrice,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = annualSavings,
                            style = MaterialTheme.typography.bodySmall,
                            color = SecyraWhite.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PurchaseSuccessCard(
    message: String = "Purchase successful!",
    onDismiss: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = RiskLow.copy(alpha = 0.2f)
        ),
        border = BorderStroke(1.dp, RiskLow)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = RiskLow
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = RiskLow,
                    fontWeight = FontWeight.Medium
                )
            }
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss",
                    tint = RiskLow
                )
            }
        }
    }
}

@Composable
fun PurchaseErrorCard(
    message: String,
    onDismiss: () -> Unit
) {
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
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = RiskCritical
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = RiskCritical,
                    fontWeight = FontWeight.Medium
                )
            }
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss",
                    tint = RiskCritical
                )
            }
        }
    }
}

