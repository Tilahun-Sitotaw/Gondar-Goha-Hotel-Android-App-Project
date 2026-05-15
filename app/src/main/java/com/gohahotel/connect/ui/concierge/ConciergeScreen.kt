package com.gohahotel.connect.ui.concierge

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gohahotel.connect.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConciergeScreen(
    viewModel: ConciergeViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    // Auto-scroll logic: scroll to bottom whenever messages list changes or typing state changes
    LaunchedEffect(uiState.messages.size, uiState.isTyping) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(
                if (uiState.isTyping) uiState.messages.size else uiState.messages.lastIndex
            )
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = SurfaceDark,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .background(
                                    Brush.linearGradient(listOf(GoldLight, GoldPrimary)),
                                    CircleShape
                                )
                                .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("G", color = SurfaceDark, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                        }
                        Column {
                            Text(
                                "Goha Concierge",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                                color = GoldPrimary
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(SuccessGreen, CircleShape)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    "AI Assistant Online",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SuccessGreen.copy(alpha = 0.9f)
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = GoldPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceDark.copy(alpha = 0.95f),
                    scrolledContainerColor = SurfaceDark
                )
            )
        },
        bottomBar = {
            ChatInputBar(
                text = uiState.inputText,
                onValueChange = viewModel::onInputChange,
                onSend = viewModel::sendMessage,
                onQuickSuggestion = { suggestion ->
                    viewModel.onInputChange(suggestion)
                    viewModel.sendMessage()
                },
                enabled = uiState.inputText.isNotBlank()
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(SurfaceDark, Color(0xFF080D0F))
                    )
                )
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(uiState.messages, key = { it.id }) { msg ->
                    ChatBubble(message = msg)
                }

                if (uiState.isTyping) {
                    item(key = "typing_indicator") {
                        TypingIndicator()
                    }
                }
            }
        }
    }
}

@Composable
fun ChatInputBar(
    text: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    onQuickSuggestion: (String) -> Unit,
    enabled: Boolean
) {
    Surface(
        color = CardDark,
        tonalElevation = 8.dp,
        border = BorderStroke(1.dp, Color.White.copy(0.05f)),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        modifier = Modifier.navigationBarsPadding().imePadding()
    ) {
        Column(modifier = Modifier.padding(top = 12.dp, bottom = 16.dp, start = 16.dp, end = 16.dp)) {
            // Quick Action Chips - Modern UX for faster interaction
            if (text.isBlank()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 12.dp)
                ) {
                    val suggestions = listOf("🌅 Sunset", "🍽️ Menu", "📶 Wi-Fi", "🚗 Taxi", "🏨 Rooms")
                    items(suggestions) { suggestion ->
                        SuggestionChip(
                            label = suggestion,
                            onClick = { onQuickSuggestion(suggestion.substringAfter(" ")) }
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TextField(
                    value = text,
                    onValueChange = onValueChange,
                    placeholder = {
                        Text("Ask Goha AI anything...", color = Color.White.copy(0.3f))
                    },
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(24.dp)),
                    maxLines = 4,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Send,
                        autoCorrect = true
                    ),
                    keyboardActions = KeyboardActions(onSend = { if (enabled) onSend() }),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = SurfaceDark,
                        unfocusedContainerColor = SurfaceDark,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = GoldPrimary
                    )
                )

                IconButton(
                    onClick = onSend,
                    modifier = Modifier
                        .size(52.dp)
                        .background(
                            if (enabled) Brush.linearGradient(listOf(GoldPrimary, GoldLight))
                            else SolidColor(GoldPrimary.copy(alpha = 0.2f)),
                            CircleShape
                        ),
                    enabled = enabled
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Send,
                        contentDescription = "Send",
                        tint = if (enabled) SurfaceDark else Color.White.copy(alpha = 0.2f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SuggestionChip(label: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = SurfaceDark,
        border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.3f))
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            color = GoldPrimary
        )
    }
}

private val timeFormatter = SimpleDateFormat("h:mm a", Locale.getDefault())

@Composable
private fun ChatBubble(message: ChatMessage) {
    val isUser = message.isFromUser
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (!isUser) {
                BotAvatar()
                Spacer(Modifier.width(8.dp))
            }

            Surface(
                shape = if (isUser)
                    RoundedCornerShape(20.dp, 20.dp, 4.dp, 20.dp)
                else
                    RoundedCornerShape(20.dp, 20.dp, 20.dp, 4.dp),
                color = if (isUser) Color.Transparent else CardDark,
                modifier = if (isUser) {
                    Modifier
                        .background(
                            brush = Brush.linearGradient(listOf(GoldPrimary, GoldLight)),
                            shape = RoundedCornerShape(20.dp, 20.dp, 4.dp, 20.dp)
                        )
                        .widthIn(max = screenWidth * 0.75f)
                } else {
                    Modifier
                        .border(
                            0.5.dp,
                            GoldPrimary.copy(alpha = 0.2f),
                            RoundedCornerShape(20.dp, 20.dp, 20.dp, 4.dp)
                        )
                        .widthIn(max = screenWidth * 0.75f)
                },
                shadowElevation = if (isUser) 4.dp else 1.dp
            ) {
                SelectionContainer {
                    Text(
                        text = message.text,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        color = if (isUser) SurfaceDark else Color.White.copy(alpha = 0.9f),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            lineHeight = 22.sp
                        )
                    )
                }
            }

            if (isUser) {
                Spacer(Modifier.width(4.dp))
            }
        }

        Text(
            text = timeFormatter.format(Date(message.timestamp)),
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(0.4f),
            modifier = Modifier.padding(
                start = if (isUser) 0.dp else 44.dp,
                end = if (isUser) 8.dp else 0.dp,
                top = 4.dp
            )
        )
    }
}

@Composable
private fun BotAvatar() {
    Box(
        modifier = Modifier
            .size(32.dp)
            .background(GoldPrimary.copy(alpha = 0.15f), CircleShape)
            .border(1.dp, GoldPrimary.copy(alpha = 0.4f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            "G",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = GoldPrimary
        )
    }
}

@Composable
private fun TypingIndicator() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BotAvatar()
        Spacer(Modifier.width(8.dp))
        Surface(
            shape = RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp),
            color = CardDark,
            modifier = Modifier.border(
                0.5.dp,
                GoldPrimary.copy(alpha = 0.1f),
                RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp)
            )
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                repeat(3) { i ->
                    val infiniteTransition = rememberInfiniteTransition(label = "dots")
                    val alpha by infiniteTransition.animateFloat(
                        initialValue = 0.3f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(600, delayMillis = i * 150),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "alpha"
                    )
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(GoldPrimary.copy(alpha = alpha), CircleShape)
                    )
                }
            }
        }
    }
}
