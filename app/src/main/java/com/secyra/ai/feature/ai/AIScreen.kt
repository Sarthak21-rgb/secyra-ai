package com.secyra.ai.feature.ai

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.secyra.ai.R
import com.secyra.ai.core.ui.theme.*
import com.secyra.ai.navigation.Screen

@Composable
fun AIScreen(
    modifier: Modifier = Modifier,
    viewModel: AIViewModel = hiltViewModel(),
    onNavigate: (Screen) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var attachments by remember { mutableStateOf<List<String>>(emptyList()) }
    var showChat by remember { mutableStateOf(false) }
    var showSidebar by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Handle navigation to Pro subscription
    LaunchedEffect(uiState.shouldNavigateToPro) {
        if (uiState.shouldNavigateToPro) {
            onNavigate(Screen.ProSubscription)
            viewModel.onNavigationHandled()
        }
    }

    // File picker launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            // Handle file selection
            attachments = attachments + it.toString()
        }
    }

    // Speech recognition launcher
    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        // Handle speech recognition result
        val results = result.data?.getStringArrayListExtra(android.speech.RecognizerIntent.EXTRA_RESULTS)
        results?.firstOrNull()?.let {
            viewModel.onInputChange(uiState.inputText + " " + it)
        }
    }

    val startVoiceInput: () -> Unit = {
        val intent = android.content.Intent(android.speech.RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE_MODEL, android.speech.RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE, java.util.Locale.getDefault())
            putExtra(android.speech.RecognizerIntent.EXTRA_PROMPT, "Speak now")
        }
        try {
            speechRecognizerLauncher.launch(intent)
        } catch (e: Exception) {
            // Speech recognition not available
        }
    }

    AIAssistantHomeScreen(
        uiState = uiState,
        onNavigate = onNavigate,
        onStartChat = { showChat = true },
        onQuickAction = { action ->
            when (action) {
                QuickAction.CheckNumber -> viewModel.onInputChange("Check a Number")
                QuickAction.AnalyzeCall -> viewModel.onInputChange("Analyze a Call")
                QuickAction.CheckMessage -> viewModel.onInputChange("Check a Message")
                QuickAction.SecurityTips -> viewModel.onInputChange("Security Tips")
            }
        },
        onInputChange = { viewModel.onInputChange(it) },
        onSendMessage = { viewModel.sendMessage(it) },
        showChat = showChat,
        onToggleSidebar = { showSidebar = !showSidebar },
        onAddAttachment = { filePickerLauncher.launch("*/*") },
        onStartVoiceInput = startVoiceInput,
        attachments = attachments,
        onRemoveAttachment = { index ->
            attachments = attachments.toMutableList().apply { removeAt(index) }
        }
    )

    // Sidebar for chat history
    if (showSidebar) {
        ChatHistorySidebar(
            onDismiss = { showSidebar = false },
            onNavigate = onNavigate
        )
    }
}

enum class QuickAction {
    CheckNumber,
    AnalyzeCall,
    CheckMessage,
    SecurityTips
}

@Composable
fun AIAssistantHomeScreen(
    uiState: AIUiState,
    onNavigate: (Screen) -> Unit,
    onStartChat: () -> Unit,
    onQuickAction: (QuickAction) -> Unit,
    onInputChange: (String) -> Unit,
    onSendMessage: (String) -> Unit,
    showChat: Boolean,
    onToggleSidebar: () -> Unit,
    onAddAttachment: () -> Unit,
    onStartVoiceInput: () -> Unit,
    attachments: List<String>,
    onRemoveAttachment: (Int) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        if (showChat) {
            // Chat view
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(SecyraBackground)
            ) {
                // Chat messages
                if (uiState.messages.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            androidx.compose.foundation.Image(
                                painter = painterResource(R.drawable.robot),
                                contentDescription = null,
                                modifier = Modifier.size(120.dp),
                                contentScale = ContentScale.Fit
                            )
                            Text(
                                text = "Start a conversation",
                                style = MaterialTheme.typography.titleMedium,
                                color = SecyraTextSecondary
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.messages) { message ->
                            ChatMessageItem(message)
                        }
                    }
                }

                // Error message
                uiState.error?.let { error ->
                    Card(
                        modifier = Modifier.padding(16.dp),
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

                // Input area at bottom
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SecyraSurface)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Attachments preview
                    if (attachments.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            attachments.forEachIndexed { index, attachment ->
                                AttachmentChip(
                                    attachment = attachment,
                                    onRemove = { onRemoveAttachment(index) }
                                )
                            }
                        }
                    }
                    
                    ChatComposer(
                        inputText = uiState.inputText,
                        onInputChange = onInputChange,
                        onSend = { onSendMessage(it) },
                        onAddAttachment = onAddAttachment,
                        onStartVoiceInput = onStartVoiceInput
                    )
                }
            }
        } else {
            // Home view
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(SecyraBackground)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        androidx.compose.foundation.Image(
                            painter = painterResource(R.drawable.ic_shield_logo),
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            contentScale = ContentScale.Fit
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "SECYRA ",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = SecyraWhite,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 22.sp
                                )
                                Text(
                                    text = "AI",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = TiffanyBlue,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 22.sp
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
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clickable { onNavigate(Screen.Notifications) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                tint = TiffanyBlue,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        IconButton(
                            onClick = onToggleSidebar,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu",
                                tint = TiffanyBlue,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                // Greeting and Robot
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Hello,",
                            style = MaterialTheme.typography.headlineMedium,
                            color = SecyraWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp
                        )
                        Text(
                            text = buildAnnotatedString {
                                append("How can I ")
                                withStyle(SpanStyle(color = TiffanyBlue)) {
                                    append("help you today?")
                                }
                            },
                            style = MaterialTheme.typography.headlineSmall,
                            color = SecyraWhite,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 24.sp
                        )
                        Text(
                            text = "Ask anything about your security, calls, messages, or online activity.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SecyraTextMuted,
                            fontSize = 15.sp,
                            lineHeight = 20.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    androidx.compose.foundation.Image(
                        painter = painterResource(R.drawable.robot),
                        contentDescription = "Secyra AI Robot",
                        modifier = Modifier
                            .width(180.dp)
                            .height(180.dp),
                        contentScale = ContentScale.Fit
                    )
                }

                // Quick Actions Header
                Text(
                    text = "Quick Actions",
                    style = MaterialTheme.typography.titleLarge,
                    color = SecyraWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )

                // Quick Actions Grid
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        QuickActionCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.Shield,
                            title = "Check a Number",
                            description = "Is this number safe or spam?",
                            onClick = { onQuickAction(QuickAction.CheckNumber) }
                        )
                        QuickActionCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.Phone,
                            title = "Analyze a Call",
                            description = "Get details about a caller.",
                            onClick = { onQuickAction(QuickAction.AnalyzeCall) }
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        QuickActionCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.Message,
                            title = "Check a Message",
                            description = "Detect scam or harmful links.",
                            onClick = { onQuickAction(QuickAction.CheckMessage) }
                        )
                        QuickActionCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.Public,
                            title = "Security Tips",
                            description = "Learn how to stay safer.",
                            onClick = { onQuickAction(QuickAction.SecurityTips) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Chat Composer
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Attachments preview
                    if (attachments.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            attachments.forEachIndexed { index, attachment ->
                                AttachmentChip(
                                    attachment = attachment,
                                    onRemove = { onRemoveAttachment(index) }
                                )
                            }
                        }
                    }
                    
                    ChatComposer(
                        inputText = uiState.inputText,
                        onInputChange = onInputChange,
                        onSend = { onSendMessage(it) },
                        onAddAttachment = onAddAttachment,
                        onStartVoiceInput = onStartVoiceInput
                    )
                }
            }
        }
    }
}

@Composable
fun QuickActionCard(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(130.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF050B12)
        ),
        border = BorderStroke(1.dp, BrushColors.horizontalGradient),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(TiffanyBlue.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = TiffanyBlue,
                    modifier = Modifier.size(24.dp)
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = SecyraWhite,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = SecyraTextMuted,
                    fontSize = 13.sp,
                    lineHeight = 16.sp
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TiffanyBlue,
                modifier = Modifier
                    .size(20.dp)
                    .align(Alignment.End)
            )
        }
    }
}

@Composable
fun ChatComposer(
    inputText: String,
    onInputChange: (String) -> Unit,
    onSend: (String) -> Unit,
    onAddAttachment: () -> Unit,
    onStartVoiceInput: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .background(Color(0xFF030A12), RoundedCornerShape(36.dp))
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(AzureBlue, TiffanyBlue)
                ),
                shape = RoundedCornerShape(36.dp)
            )
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onAddAttachment,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add attachment",
                tint = SecyraWhite,
                modifier = Modifier.size(28.dp)
            )
        }
        
        OutlinedTextField(
            value = inputText,
            onValueChange = onInputChange,
            placeholder = { 
                Text(
                    "Type your question...",
                    color = SecyraTextMuted,
                    fontSize = 14.sp
                ) 
            },
            modifier = Modifier.weight(1f),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedTextColor = SecyraWhite,
                unfocusedTextColor = SecyraWhite
            ),
            singleLine = true
        )
        
        IconButton(
            onClick = onStartVoiceInput,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = "Voice input",
                tint = SecyraWhite,
                modifier = Modifier.size(24.dp)
            )
        }
        
        IconButton(
            onClick = { onSend(inputText) },
            enabled = inputText.isNotBlank(),
            modifier = Modifier
                .size(40.dp)
                .background(
                    if (inputText.isNotBlank()) AzureBlue else Color.Gray,
                    CircleShape
                )
        ) {
            Icon(
                imageVector = Icons.Default.ArrowUpward,
                contentDescription = "Send",
                tint = SecyraWhite,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun AttachmentChip(
    attachment: String,
    onRemove: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = TiffanyBlue.copy(alpha = 0.2f)
        ),
        border = BorderStroke(1.dp, TiffanyBlue),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.AttachFile,
                contentDescription = null,
                tint = TiffanyBlue,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = attachment.substringAfterLast("/").take(20),
                style = MaterialTheme.typography.bodySmall,
                color = SecyraWhite,
                maxLines = 1
            )
            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(20.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove",
                    tint = TiffanyBlue,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
fun ChatHistorySidebar(
    onDismiss: () -> Unit,
    onNavigate: (Screen) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(onClick = onDismiss)
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .width(300.dp)
                .align(Alignment.CenterEnd)
                .background(SecyraSurface)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Chat History",
                    style = MaterialTheme.typography.titleLarge,
                    color = SecyraWhite,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = SecyraWhite
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Placeholder for chat history
            Text(
                text = "No previous conversations",
                style = MaterialTheme.typography.bodyMedium,
                color = SecyraTextMuted
            )
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val date = java.util.Date(timestamp)
    return java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(date)
}

@Composable
fun ChatMessageItem(message: ChatMessage) {
    val isUser = message.role == "user"
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isUser) {
                    TiffanyBlue.copy(alpha = 0.2f)
                } else {
                    SecyraSurfaceElevated
                }
            ),
            border = BorderStroke(
                1.dp,
                if (isUser) TiffanyBlue else SecyraBorder
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp).widthIn(max = 280.dp)
            ) {
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = SecyraWhite
                )
                Text(
                    text = formatTimestamp(message.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = SecyraTextMuted
                )
            }
        }
    }
}

