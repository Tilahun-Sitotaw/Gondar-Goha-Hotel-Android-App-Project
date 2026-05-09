package com.gohahotel.connect.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gohahotel.connect.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuestChatScreen(
    onBack: () -> Unit,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(Unit) { viewModel.openGuestChat() }

    // Auto-scroll to bottom on new messages
    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.lastIndex)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .background(GoldPrimary.copy(0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.SupportAgent, null,
                                tint = GoldPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                "Hotel Concierge",
                                fontWeight = FontWeight.Bold,
                                color = GoldPrimary
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(Color(0xFF50C878), CircleShape)
                                )
                                Text(
                                    "Online · Typically replies instantly",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF50C878)
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = GoldPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceDark.copy(0.97f)
                )
            )
        },
        bottomBar = {
            Surface(
                color = SurfaceDark.copy(0.97f),
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                        .navigationBarsPadding(),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = {
                            Text(
                                "Message hotel staff...",
                                color = OnSurfaceDark.copy(0.35f)
                            )
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp),
                        maxLines = 4,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor   = GoldPrimary.copy(0.5f),
                            unfocusedBorderColor = Color.White.copy(0.1f),
                            focusedTextColor     = OnSurfaceDark,
                            unfocusedTextColor   = OnSurfaceDark,
                            cursorColor          = GoldPrimary,
                            focusedContainerColor   = Color.White.copy(0.05f),
                            unfocusedContainerColor = Color.White.copy(0.03f)
                        )
                    )
                    FloatingActionButton(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                viewModel.sendMessage(inputText)
                                inputText = ""
                            }
                        },
                        modifier = Modifier.size(48.dp),
                        containerColor = if (inputText.isNotBlank()) GoldPrimary
                                         else Color.White.copy(0.08f),
                        shape = CircleShape,
                        elevation = FloatingActionButtonDefaults.elevation(0.dp)
                    ) {
                        if (uiState.isSending) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = SurfaceDark,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                Icons.AutoMirrored.Filled.Send, null,
                                tint = if (inputText.isNotBlank()) SurfaceDark
                                       else OnSurfaceDark.copy(0.3f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF071520), Color(0xFF050D18))
                    )
                )
                .padding(padding)
        ) {
            if (uiState.messages.isEmpty()) {
                // Welcome state
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("🏨", fontSize = 56.sp)
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Welcome to Goha Hotel",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = GoldPrimary
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Our concierge team is here to help you 24/7. Ask about room service, local attractions, reservations, or anything else.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceDark.copy(0.5f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(Modifier.height(24.dp))
                    // Quick suggestion chips
                    listOf(
                        "🍽️ Room service menu",
                        "🗺️ Local attractions",
                        "🛎️ Housekeeping request",
                        "🚗 Airport transfer"
                    ).forEach { suggestion ->
                        SuggestionChip(
                            onClick = { inputText = suggestion.drop(3) },
                            label = { Text(suggestion, color = GoldPrimary.copy(0.8f)) },
                            modifier = Modifier.padding(vertical = 3.dp),
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = GoldPrimary.copy(0.08f)
                            ),
                            border = SuggestionChipDefaults.suggestionChipBorder(
                                enabled = true,
                                borderColor = GoldPrimary.copy(0.2f)
                            )
                        )
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.messages, key = { it.id }) { message ->
                        ChatBubble(
                            message = message,
                            isOwn = !message.isAdmin
                        )
                    }
                    item { Spacer(Modifier.height(8.dp)) }
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage, isOwn: Boolean) {
    val timeFmt = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val timeStr = timeFmt.format(Date(message.timestamp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isOwn) Arrangement.End else Arrangement.Start
    ) {
        if (!isOwn) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(GoldPrimary.copy(0.15f), CircleShape)
                    .align(Alignment.Bottom),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.SupportAgent, null,
                    tint = GoldPrimary,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(Modifier.width(8.dp))
        }

        Column(
            horizontalAlignment = if (isOwn) Alignment.End else Alignment.Start,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            if (!isOwn) {
                Text(
                    message.senderName,
                    style = MaterialTheme.typography.labelSmall,
                    color = GoldPrimary.copy(0.7f),
                    modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
                )
            }
            Surface(
                shape = RoundedCornerShape(
                    topStart = 18.dp,
                    topEnd = 18.dp,
                    bottomStart = if (isOwn) 18.dp else 4.dp,
                    bottomEnd = if (isOwn) 4.dp else 18.dp
                ),
                color = if (isOwn) GoldPrimary.copy(0.85f) else Color(0xFF0E1B2A),
                border = if (!isOwn) androidx.compose.foundation.BorderStroke(
                    1.dp, GoldPrimary.copy(0.15f)
                ) else null
            ) {
                Text(
                    message.text,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    color = if (isOwn) Color(0xFF050D18) else OnSurfaceDark,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 2.dp, start = 4.dp, end = 4.dp)
            ) {
                Text(
                    timeStr,
                    style = MaterialTheme.typography.labelSmall,
                    color = OnSurfaceDark.copy(0.3f),
                    fontSize = 10.sp
                )
                if (isOwn) {
                    Icon(
                        if (message.isRead) Icons.Default.DoneAll else Icons.Default.Done,
                        null,
                        tint = if (message.isRead) GoldPrimary.copy(0.7f)
                               else OnSurfaceDark.copy(0.3f),
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }

        if (isOwn) {
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(GoldPrimary.copy(0.1f), CircleShape)
                    .align(Alignment.Bottom),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person, null,
                    tint = GoldPrimary.copy(0.7f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
