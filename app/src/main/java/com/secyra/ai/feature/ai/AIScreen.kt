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
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.secyra.ai.R
import com.secyra.ai.core.ui.theme.*
import com.secyra.ai.navigation.Screen

// Gradient text composable
@Composable
fun GradientText(
    text: String,
    colors: List<Color>,
    style: TextStyle = TextStyle.Default,
    modifier: Modifier = Modifier
) {
    val brush = Brush.horizontalGradient(colors = colors)
    Text(
        text = text,
        style = style.copy(brush = brush),
        modifier = modifier
    )
}

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
    var isRecording by remember { mutableStateOf(false) }
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

    // Custom voice input placeholder
    val startVoiceInput: () -> Unit = {
        isRecording = true
        // TODO: Implement custom voice recognition backend
        // This will connect to a custom backend for speech-to-text
        // Currently shows recording state
    }

    val stopVoiceInput: () -> Unit = {
        isRecording = false
        // TODO: Process audio and get text from backend
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
        onStopVoiceInput = stopVoiceInput,
        isRecording = isRecording,
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
    onStopVoiceInput: () -> Unit,
    isRecording: Boolean,
    attachments: List<String>,
    onRemoveAttachment: (Int) -> Unit
) {
    var isInputFocused by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current
    
    Box(modifier = Modifier.fillMaxSize()) {
        if (showChat) {
            // Chat view
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(SecyraBackground),
            ) {
                // Chat messages (hide when input is focused)
                if (!isInputFocused) {
                    if (uiState.messages.isEmpty()) {
                        // Welcome state with suggested prompts
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(1f)
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(24.dp)
                            ) {
                                Text(
                                    text = "Your security. Your intelligence.",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = SecyraWhite,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "Ask anything, understand risks, and make safer decisions with Secyra AI.",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = SecyraTextSecondary,
                                    fontSize = 16.sp,
                                    textAlign = TextAlign.Center
                                )
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                // Suggested prompts
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    SuggestedPrompt(
                                        text = "Is this message a scam?",
                                        onClick = { onInputChange("Is this message a scam?") }
                                    )
                                    SuggestedPrompt(
                                        text = "Explain this suspicious link",
                                        onClick = { onInputChange("Explain this suspicious link") }
                                    )
                                    SuggestedPrompt(
                                        text = "How can I secure my phone?",
                                        onClick = { onInputChange("How can I secure my phone?") }
                                    )
                                    SuggestedPrompt(
                                        text = "Help me understand this screen",
                                        onClick = { onInputChange("Help me understand this screen") }
                                    )
                                }
                            }
                        }
                    } else {
                        val listState = rememberLazyListState()
                        
                        // Auto-scroll to bottom when new message arrives
                        LaunchedEffect(uiState.messages.size) {
                            if (uiState.messages.isNotEmpty()) {
                                listState.animateScrollToItem(uiState.messages.size - 1)
                            }
                        }
                        
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(1f)
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(uiState.messages) { message ->
                                ChatMessageItem(message)
                            }
                        }
                    }
                } else {
                    // Spacer when input is focused to push composer to bottom
                    Spacer(modifier = Modifier.weight(1f))
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
                        onStartVoiceInput = onStartVoiceInput,
                        onStopVoiceInput = onStopVoiceInput,
                        isRecording = isRecording,
                        onFocusChange = { isInputFocused = it }
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
                            painter = painterResource(R.drawable.shield_logo),
                            contentDescription = null,
                            modifier = Modifier.size(46.dp),
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
                                GradientText(
                                    text = "AI",
                                    colors = listOf(AzureBlue, TiffanyBlue),
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 22.sp
                                    )
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
                                .size(44.dp)
                                .clickable { onNavigate(Screen.Notifications) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                tint = SecyraWhite,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        IconButton(
                            onClick = onToggleSidebar,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu",
                                tint = SecyraWhite,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(48.dp))

                // App Name and Greeting
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "SECYRA ",
                            style = MaterialTheme.typography.headlineLarge,
                            color = SecyraWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 32.sp
                        )
                        GradientText(
                            text = "AI",
                            colors = listOf(AzureBlue, TiffanyBlue),
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 32.sp
                            )
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "How can I ",
                            style = MaterialTheme.typography.titleMedium,
                            color = SecyraTextMuted,
                            fontSize = 18.sp
                        )
                        GradientText(
                            text = "help you today?",
                            colors = listOf(AzureBlue, TiffanyBlue),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = 18.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

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

                Spacer(modifier = Modifier.height(100.dp))

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
                        onStartVoiceInput = onStartVoiceInput,
                        onStopVoiceInput = onStopVoiceInput,
                        isRecording = isRecording
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
            .height(110.dp)
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
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(TiffanyBlue.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = TiffanyBlue,
                    modifier = Modifier.size(18.dp)
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = SecyraWhite,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = SecyraTextMuted,
                    fontSize = 11.sp,
                    lineHeight = 14.sp
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TiffanyBlue,
                modifier = Modifier
                    .size(16.dp)
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
    onStartVoiceInput: () -> Unit,
    onStopVoiceInput: () -> Unit,
    isRecording: Boolean,
    onFocusChange: (Boolean) -> Unit = {}
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    var text by remember { mutableStateOf(inputText) }
    var isFocused by remember { mutableStateOf(false) }
    
    LaunchedEffect(inputText) {
        text = inputText
    }
    
    LaunchedEffect(isFocused) {
        onFocusChange(isFocused)
    }
    
    val containerHeight = if (isFocused) 160.dp else 72.dp
    val inputHeight = if (isFocused) 120.dp else 48.dp
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(containerHeight)
            .background(Color(0xFF030A12), RoundedCornerShape(36.dp))
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(AzureBlue, TiffanyBlue)
                ),
                shape = RoundedCornerShape(36.dp)
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Multiline text input area
        OutlinedTextField(
            value = text,
            onValueChange = { 
                text = it
                onInputChange(it)
            },
            placeholder = { 
                Text(
                    "Ask Secyra anything...",
                    color = SecyraTextMuted,
                    fontSize = 15.sp
                ) 
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(inputHeight)
                .onFocusChanged { focusState ->
                    isFocused = focusState.isFocused
                },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedTextColor = SecyraWhite,
                unfocusedTextColor = SecyraWhite
            ),
            textStyle = TextStyle(
                color = SecyraWhite,
                fontSize = 15.sp
            ),
            maxLines = if (isFocused) 6 else 1,
            singleLine = !isFocused
        )
        
        // Bottom controls row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(
                    onClick = onAddAttachment,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add attachment",
                        tint = SecyraWhite,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                IconButton(
                    onClick = { if (isRecording) onStopVoiceInput() else onStartVoiceInput() },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                        contentDescription = if (isRecording) "Stop recording" else "Voice input",
                        tint = SecyraWhite,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            
            IconButton(
                onClick = { 
                    keyboardController?.hide()
                    onSend(text)
                },
                enabled = text.isNotBlank(),
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        if (text.isNotBlank()) AzureBlue else AzureBlue.copy(alpha = 0.4f),
                        CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowUpward,
                    contentDescription = "Send",
                    tint = SecyraWhite,
                    modifier = Modifier.size(24.dp)
                )
            }
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
fun SuggestedPrompt(
    text: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        border = BorderStroke(
            1.dp,
            SecyraBorder
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.ChatBubbleOutline,
                contentDescription = null,
                tint = TiffanyBlue,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = SecyraWhite,
                fontSize = 15.sp
            )
        }
    }
}

@Composable
fun ChatMessageItem(message: ChatMessage) {
    val isUser = message.role == "user"
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Column(
            modifier = Modifier.widthIn(max = 300.dp),
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
        ) {
            // Message bubble
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color.Transparent
                ),
                border = if (isUser) {
                    BorderStroke(
                        1.dp,
                        Brush.horizontalGradient(
                            colors = listOf(AzureBlue, TiffanyBlue)
                        )
                    )
                } else {
                    null
                },
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = message.content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = SecyraWhite,
                        fontSize = 15.sp,
                        lineHeight = 22.sp
                    )
                }
            }
            
            // Timestamp
            Text(
                text = formatTimestamp(message.timestamp),
                style = MaterialTheme.typography.labelSmall,
                color = SecyraTextMuted,
                fontSize = 11.sp,
                modifier = Modifier.padding(
                    start = if (isUser) 0.dp else 4.dp,
                    end = if (isUser) 4.dp else 0.dp,
                    top = 4.dp
                )
            )
            
            // Action toolbar for AI responses
            if (!isUser) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    // Copy
                    IconButton(
                        onClick = { /* TODO: Implement copy */ },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy",
                            tint = SecyraTextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    // Listen
                    IconButton(
                        onClick = { /* TODO: Implement TTS */ },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "Listen",
                            tint = SecyraTextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    // Share
                    IconButton(
                        onClick = { /* TODO: Implement share */ },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = SecyraTextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    // Helpful
                    IconButton(
                        onClick = { /* TODO: Implement feedback */ },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ThumbUp,
                            contentDescription = "Helpful",
                            tint = SecyraTextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    // Not helpful
                    IconButton(
                        onClick = { /* TODO: Implement feedback */ },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ThumbDown,
                            contentDescription = "Not helpful",
                            tint = SecyraTextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    // Regenerate
                    IconButton(
                        onClick = { /* TODO: Implement regenerate */ },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Regenerate",
                            tint = SecyraTextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    // More
                    IconButton(
                        onClick = { /* TODO: Implement more options */ },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More",
                            tint = SecyraTextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

