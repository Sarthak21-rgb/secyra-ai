package com.secyra.ai.feature.emails

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.secyra.ai.core.ui.theme.*
import com.secyra.ai.data.repository.Email
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun EmailScreen(
    viewModel: EmailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showConnectionForm by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var isConnecting by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SecyraBackground)
    ) {
        when {
            uiState.isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = TiffanyBlue
                )
            }
            !uiState.isAccountConnected && !showConnectionForm -> {
                // Empty state with feature card (show by default)
                EmailEmptyState(
                    onAddAccount = { showConnectionForm = true }
                )
            }
            !uiState.isAccountConnected && showConnectionForm -> {
                // Connection form
                EmailConnectionForm(
                    email = email,
                    onEmailChange = { email = it },
                    password = password,
                    onPasswordChange = { password = it },
                    showPassword = showPassword,
                    onTogglePassword = { showPassword = !showPassword },
                    isConnecting = isConnecting,
                    onConnect = {
                        isConnecting = true
                        // TODO: Implement actual OAuth connection
                        // For now, simulate connection
                        viewModel.onAccountConnected()
                        isConnecting = false
                        showConnectionForm = false
                    },
                    onCancel = {
                        showConnectionForm = false
                        email = ""
                        password = ""
                    }
                )
            }
            uiState.emails.isEmpty() -> {
                EmptyEmailsContent()
            }
            else -> {
                EmailsListContent(emails = uiState.emails)
            }
        }
    }
}

@Composable
fun EmailEmptyState(
    onAddAccount: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Envelope illustration with + badge
        Box(
            modifier = Modifier.size(120.dp),
            contentAlignment = Alignment.Center
        ) {
            // Background container
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(
                        Color(0xFF0A1520).copy(alpha = 0.8f),
                        RoundedCornerShape(24.dp)
                    )
                    .border(
                        width = 1.dp,
                        brush = Brush.horizontalGradient(
                            colors = listOf(AzureBlue.copy(alpha = 0.3f), TiffanyBlue.copy(alpha = 0.3f))
                        ),
                        shape = RoundedCornerShape(24.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = null,
                    tint = TiffanyBlue.copy(alpha = 0.6f),
                    modifier = Modifier.size(48.dp)
                )
            }
            
            // + badge
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 8.dp, y = (-8).dp)
                    .size(32.dp)
                    .background(TiffanyBlue, CircleShape)
                    .border(2.dp, Color.Black, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Title
        Text(
            text = "No email account connected",
            color = SecyraWhite,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Description
        Text(
            text = "Connect your email account to view, manage, and stay protected from suspicious emails.",
            color = SecyraTextSecondary,
            fontSize = 15.sp,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Feature card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF0A1520).copy(alpha = 0.5f)
            ),
            border = BorderStroke(
                1.dp,
                Brush.horizontalGradient(
                    colors = listOf(AzureBlue.copy(alpha = 0.3f), TiffanyBlue.copy(alpha = 0.3f))
                )
            ),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Row 1: View your emails
                FeatureRow(
                    icon = Icons.Default.Email,
                    title = "View your emails",
                    subtitle = "Access your inbox, sent items and more."
                )
                
                HorizontalDivider(
                    color = Color(0xFF202020),
                    thickness = 1.dp
                )
                
                // Row 2: Detect threats
                FeatureRow(
                    icon = Icons.Default.Shield,
                    title = "Detect threats",
                    subtitle = "Get alerts for phishing, scams and suspicious links."
                )
                
                HorizontalDivider(
                    color = Color(0xFF202020),
                    thickness = 1.dp
                )
                
                // Row 3: Keep your data safe
                FeatureRow(
                    icon = Icons.Default.Lock,
                    title = "Keep your data safe",
                    subtitle = "Your email stays private and secure."
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Add Email Account button
        Button(
            onClick = onAddAccount,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .border(
                    width = 1.dp,
                    brush = Brush.horizontalGradient(
                        colors = listOf(AzureBlue, TiffanyBlue)
                    ),
                    shape = RoundedCornerShape(28.dp)
                ),
            shape = RoundedCornerShape(28.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = SecyraWhite,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Add Email Account",
                    color = SecyraWhite,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = "Supports Gmail, Outlook and other compatible email providers",
            color = SecyraTextMuted,
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun FeatureRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(
                    TiffanyBlue.copy(alpha = 0.15f),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = TiffanyBlue,
                modifier = Modifier.size(22.dp)
            )
        }
        
        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                color = SecyraWhite,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                color = SecyraTextSecondary,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
fun EmailConnectionForm(
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    showPassword: Boolean,
    onTogglePassword: () -> Unit,
    isConnecting: Boolean,
    onConnect: () -> Unit,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF0A1520).copy(alpha = 0.8f)
            ),
            border = BorderStroke(
                1.dp,
                Brush.horizontalGradient(
                    colors = listOf(AzureBlue.copy(alpha = 0.3f), TiffanyBlue.copy(alpha = 0.3f))
                )
            ),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Title
                Text(
                    text = "Connect your email",
                    color = SecyraWhite,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "Sign in to connect your inbox to Secyra AI.",
                    color = SecyraTextSecondary,
                    fontSize = 14.sp
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Email field
                OutlinedTextField(
                    value = email,
                    onValueChange = onEmailChange,
                    label = {
                        Text(
                            "Email address",
                            color = SecyraTextMuted
                        )
                    },
                    placeholder = {
                        Text(
                            "Enter your email address",
                            color = SecyraTextMuted
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF0A1520),
                        unfocusedContainerColor = Color(0xFF0A1520),
                        focusedIndicatorColor = TiffanyBlue,
                        unfocusedIndicatorColor = SecyraBorder,
                        focusedTextColor = SecyraWhite,
                        unfocusedTextColor = SecyraWhite,
                        cursorColor = TiffanyBlue
                    ),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                
                // Password field
                OutlinedTextField(
                    value = password,
                    onValueChange = onPasswordChange,
                    label = {
                        Text(
                            "Password",
                            color = SecyraTextMuted
                        )
                    },
                    placeholder = {
                        Text(
                            "Enter your password",
                            color = SecyraTextMuted
                        )
                    },
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = onTogglePassword) {
                            Icon(
                                imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (showPassword) "Hide password" else "Show password",
                                tint = SecyraTextMuted
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF0A1520),
                        unfocusedContainerColor = Color(0xFF0A1520),
                        focusedIndicatorColor = TiffanyBlue,
                        unfocusedIndicatorColor = SecyraBorder,
                        focusedTextColor = SecyraWhite,
                        unfocusedTextColor = SecyraWhite,
                        cursorColor = TiffanyBlue
                    ),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Add Account button
                Button(
                    onClick = onConnect,
                    enabled = email.isNotBlank() && password.isNotBlank() && !isConnecting,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TiffanyBlue
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(25.dp)
                ) {
                    if (isConnecting) {
                        CircularProgressIndicator(
                            color = Color.Black,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text(
                            text = "Add Account",
                            color = Color.Black,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                
                // Cancel button
                TextButton(
                    onClick = onCancel,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Cancel",
                        color = SecyraTextSecondary,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}

@Composable
fun PermissionRequestContent(
    onRequestPermission: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Email,
            contentDescription = null,
            tint = TiffanyBlue,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Email Access Required",
            color = SecyraWhite,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Grant storage permission to access your emails",
            color = SecyraTextSecondary,
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onRequestPermission,
            colors = ButtonDefaults.buttonColors(
                containerColor = TiffanyBlue
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Grant Permission",
                color = Color.Black,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun EmailNotAvailableContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.ErrorOutline,
            contentDescription = null,
            tint = Color(0xFFFFB020),
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Email Integration Not Available",
            color = SecyraWhite,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "This device doesn't support email integration through Gmail provider",
            color = SecyraTextSecondary,
            fontSize = 14.sp
        )
    }
}

@Composable
fun EmptyEmailsContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Email,
            contentDescription = null,
            tint = Color(0xFF3A3A3A),
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No Emails Found",
            color = SecyraWhite,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Your emails will appear here",
            color = SecyraTextSecondary,
            fontSize = 14.sp
        )
    }
}

@Composable
fun EmailsListContent(emails: List<Email>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(emails) { email ->
            EmailRowCard(email = email)
        }
    }
}

@Composable
fun EmailRowCard(email: Email) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF0A0A0A)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            Color(0xFF202020)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = email.sender,
                    color = SecyraWhite,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = formatEmailTimestamp(email.timestamp),
                    color = SecyraTextSecondary,
                    fontSize = 12.sp
                )
            }
            Text(
                text = email.subject,
                color = SecyraWhite,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = email.preview,
                color = SecyraTextSecondary,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun formatEmailTimestamp(timestamp: Long): String {
    return try {
        val date = Date(timestamp)
        val now = Date()
        val calendar = java.util.Calendar.getInstance()
        calendar.time = now
        val currentDay = calendar.get(java.util.Calendar.DAY_OF_YEAR)

        calendar.time = date
        val emailDay = calendar.get(java.util.Calendar.DAY_OF_YEAR)

        val dayDiff = currentDay - emailDay

        when {
            dayDiff == 0 -> {
                val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
                timeFormat.format(date)
            }
            dayDiff == 1 -> "Yesterday"
            dayDiff in 2..6 -> {
                val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())
                dayFormat.format(date)
            }
            else -> {
                val dateFormat = SimpleDateFormat("MMM d", Locale.getDefault())
                dateFormat.format(date)
            }
        }
    } catch (e: Exception) {
        "Unknown"
    }
}
