package com.gohahotel.connect.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gohahotel.connect.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminChatScreen(
    onBack: () -> Unit,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedThread by remember { mutableStateOf<ChatThread?>(null) }
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(Unit) { viewModel.loadAllThreads() }

    // When a thread is selected, open it
    LaunchedEffect(selectedThread) {
        selectedThread?.let {
            viewModel.openAdminChat(it.guestId, it.guestName)
        }
    }

    // Auto-scroll to bottom on new messages
    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.lastIndex)
        }
    }

    if (selectedThread == null) {
        // ── Thread list view ──────────────────────────────────────────────────
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                "Guest Messages",
                                fontWeight = FontWeight.Bold,
                                color = GoldPrimary
                            )
                            Text(
                                "${uiState.threads.size} conversations",
                                style = MaterialTheme.typography.labelSmall,
                                color = OnSurfaceDark.copy(0.5f)
                            )
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
                if (uiState.threads.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("💬", fontSize = 56.sp)
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "No guest messages yet",
                                color = OnSurfaceDark.copy(0.4f),
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                "Guest conversations will appear here",
                                color = OnSurfaceDark.copy(0.25f),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(uiState.threads, key = { it.guestId }) { thread ->
                            ChatThreadCard(
                                thread = thread,
                                onClick = { selectedThread = thread }
                            )
                        }
                    }
                }
            }
        }
    } else {
        // ── Chat conversation view ────────────────────────────────────────────
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
                                    .size(36.dp)
                                    .background(GoldPrimary.copy(0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    selectedThread!!.guestName.firstOrNull()?.uppercase() ?: "G",
                                    color = GoldPrimary,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 16.sp
                                )
                            }
                            Column {
                                Text(
                                    selectedThread!!.guestName,
                                    fontWeight = FontWeight.Bold,
                                    color = OnSurfaceDark
                                )
                                Text(
                                    "Guest",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = OnSurfaceDark.copy(0.4f)
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { selectedThread = null }) {
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
                                    "Reply to ${selectedThread!!.guestName}...",
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
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("💬", fontSize = 40.sp)
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "No messages yet",
                                color = OnSurfaceDark.copy(0.3f),
                                style = MaterialTheme.typography.bodyMedium
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
                            // From admin's perspective: admin messages are "own"
                            ChatBubble(
                                message = message,
                                isOwn = message.isAdmin
                            )
                        }
                        item { Spacer(Modifier.height(8.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatThreadCard(thread: ChatThread, onClick: () -> Unit) {
    val timeFmt = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val timeStr = if (thread.lastTimestamp > 0)
        timeFmt.format(Date(thread.lastTimestamp)) else ""

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF0C1A28),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (thread.unreadCount > 0) GoldPrimary.copy(0.3f)
            else Color.White.copy(0.06f)
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(GoldPrimary.copy(0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    thread.guestName.firstOrNull()?.uppercase() ?: "G",
                    color = GoldPrimary,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        thread.guestName,
                        fontWeight = if (thread.unreadCount > 0) FontWeight.ExtraBold
                                     else FontWeight.SemiBold,
                        color = OnSurfaceDark,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        timeStr,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (thread.unreadCount > 0) GoldPrimary.copy(0.7f)
                                else OnSurfaceDark.copy(0.3f),
                        fontSize = 11.sp
                    )
                }
                Spacer(Modifier.height(3.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        thread.lastMessage.ifBlank { "No messages yet" },
                        style = MaterialTheme.typography.bodySmall,
                        color = if (thread.unreadCount > 0) OnSurfaceDark.copy(0.7f)
                                else OnSurfaceDark.copy(0.4f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (thread.unreadCount > 0) {
                        Spacer(Modifier.width(8.dp))
                        Badge(containerColor = GoldPrimary) {
                            Text(
                                "${thread.unreadCount}",
                                color = Color(0xFF050D18),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
