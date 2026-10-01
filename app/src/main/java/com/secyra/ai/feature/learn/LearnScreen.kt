package com.secyra.ai.feature.learn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
fun LearnScreen(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SecyraBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Text(
            text = "Security Education",
            style = MaterialTheme.typography.headlineMedium,
            color = SecyraWhite,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Learn about common security threats and how to stay safe",
            style = MaterialTheme.typography.bodyMedium,
            color = SecyraTextSecondary
        )

        // Topics
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(learnTopics) { topic ->
                LearnTopicCard(topic)
            }
        }
    }
}

@Composable
fun LearnTopicCard(topic: LearnTopic) {
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
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = topic.icon,
                    contentDescription = null,
                    tint = TiffanyBlue,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = topic.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = SecyraWhite,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = topic.description,
                style = MaterialTheme.typography.bodyMedium,
                color = SecyraTextSecondary
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                topic.keyPoints.forEach { point ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = SecyraSurface
                        ),
                        border = BorderStroke(1.dp, SecyraBorder)
                    ) {
                        Text(
                            text = point,
                            style = MaterialTheme.typography.bodySmall,
                            color = SecyraTextSecondary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

data class LearnTopic(
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val title: String,
    val description: String,
    val keyPoints: List<String>
)

val learnTopics = listOf(
    LearnTopic(
        icon = Icons.Default.Info,
        title = "Phishing",
        description = "Learn how to identify and avoid phishing attacks that try to steal your personal information.",
        keyPoints = listOf("Check sender email", "Verify URLs", "Don't click suspicious links", "Verify with official channels")
    ),
    LearnTopic(
        icon = Icons.Default.Phone,
        title = "Scam Calls",
        description = "Recognize common phone scams and how to protect yourself from fraudulent callers.",
        keyPoints = listOf("Don't share personal info", "Verify caller identity", "Be suspicious of urgency", "Hang up on suspicious calls")
    ),
    LearnTopic(
        icon = Icons.Default.Info,
        title = "Fake Messages",
        description = "Identify fake SMS and messaging scams that try to trick you into revealing sensitive information.",
        keyPoints = listOf("Check for urgency", "Verify sender", "Don't click links", "Verify with official sources")
    ),
    LearnTopic(
        icon = Icons.Default.Lock,
        title = "Password Safety",
        description = "Create strong passwords and protect your accounts from unauthorized access.",
        keyPoints = listOf("Use unique passwords", "Enable 2FA", "Use password manager", "Change passwords regularly")
    ),
    LearnTopic(
        icon = Icons.Default.Lock,
        title = "Multi-Factor Authentication",
        description = "Understand the importance of MFA and how it adds an extra layer of security to your accounts.",
        keyPoints = listOf("Enable everywhere possible", "Use authenticator apps", "Backup recovery codes", "Don't share codes")
    ),
    LearnTopic(
        icon = Icons.Default.Info,
        title = "Social Engineering",
        description = "Learn about psychological manipulation techniques used by scammers to trick people.",
        keyPoints = listOf("Be skeptical", "Verify identities", "Don't act on urgency", "Trust your instincts")
    ),
    LearnTopic(
        icon = Icons.Default.Lock,
        title = "Mobile Security",
        description = "Protect your mobile device from malware, unauthorized access, and other security threats.",
        keyPoints = listOf("Keep software updated", "Use screen lock", "Review app permissions", "Avoid unknown sources")
    ),
    LearnTopic(
        icon = Icons.Default.Info,
        title = "AI Scams",
        description = "Understand how AI is being used by scammers and how to identify AI-generated content.",
        keyPoints = listOf("Deepfake awareness", "AI voice cloning", "Verify authenticity", "Be skeptical of AI content")
    ),
    LearnTopic(
        icon = Icons.Default.Info,
        title = "Deepfake Awareness",
        description = "Learn about deepfake technology and how to detect AI-generated fake content.",
        keyPoints = listOf("Check for inconsistencies", "Verify sources", "Use reverse image search", "Be cautious with media")
    ),
    LearnTopic(
        icon = Icons.Default.Info,
        title = "Safe Digital Payments",
        description = "Protect yourself when making digital payments and avoid payment-related scams.",
        keyPoints = listOf("Use secure platforms", "Verify recipient", "Use credit cards", "Monitor transactions")
    ),
    LearnTopic(
        icon = Icons.Default.Info,
        title = "QR Scams",
        description = "Understand QR code security and how to scan QR codes safely.",
        keyPoints = listOf("Check URL before opening", "Be suspicious of random QRs", "Use secure QR scanner", "Verify source")
    )
)


