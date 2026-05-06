package com.gohahotel.connect.ui.concierge

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
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
    val uiState   by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.lastIndex)
        }
    }

    Scaffold(
        containerColor = SurfaceDark,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    Brush.linearGradient(listOf(GoldLight, GoldPrimary)),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) { Text("G", color = SurfaceDark, fontWeight = FontWeight.ExtraBold) }
                        Column {
                            Text("Goha Concierge", fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium, color = GoldPrimary)
                            Text("Online · Real-time Support",
                                style = MaterialTheme.typography.labelSmall,
                                color = SuccessGreen)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back", tint = GoldPrimary) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceDark)
            )
        },
        bottomBar = {
            Surface(
                color = CardDark,
                tonalElevation = 8.dp,
                border = BorderStroke(1.dp, Color.White.copy(0.05f)),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TextField(
                        value = uiState.inputText,
                        onValueChange = viewModel::onInputChange,
                        placeholder = { Text("Ask about services, tours...", color = Color.White.copy(0.4f)) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp),
                        maxLines = 4,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = { viewModel.sendMessage() }),
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
                        onClick  = viewModel::sendMessage,
                        modifier = Modifier
                            .size(52.dp)
                            .background(GoldPrimary, CircleShape),
                        enabled  = uiState.inputText.isNotBlank()
                    ) {
                        Icon(Icons.Default.Send, "Send",
                            tint = SurfaceDark, modifier = Modifier.size(24.dp))
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .background(Brush.verticalGradient(listOf(SurfaceDark, Color(0xFF0A1114))))
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
            items(uiState.messages, key = { it.id }) { msg ->
                ChatBubble(message = msg)
            }

            if (uiState.isTyping) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(start = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier.size(28.dp)
                                .background(GoldPrimary.copy(0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) { Text("G", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = GoldPrimary) }
                        Card(
                            shape  = RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp),
                            colors = CardDefaults.cardColors(containerColor = CardDark)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                repeat(3) { i ->
                                    val delay = i * 150
                                    val alpha by rememberInfiniteTransition(label = "dot$i")
                                        .animateFloat(
                                            0.2f, 1f,
                                            animationSpec = infiniteRepeatable(
                                                tween(600, delayMillis = delay),
                                                RepeatMode.Reverse
                                            ),
                                            label = "dot_alpha_$i"
                                        )
                                    Box(
                                        modifier = Modifier.size(6.dp)
                                            .background(GoldPrimary.copy(alpha), CircleShape)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
}

private val timeFormatter = SimpleDateFormat("h:mm a", Locale.getDefault())

@Composable
private fun ChatBubble(message: ChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isFromUser) Arrangement.End else Arrangement.Start,
        verticalAlignment     = Alignment.Bottom
    ) {
        if (!message.isFromUser) {
            Box(
                modifier = Modifier.size(28.dp)
                    .background(GoldPrimary.copy(0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) { Text("G", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = GoldPrimary) }
            Spacer(Modifier.width(6.dp))
        }

        Column(horizontalAlignment = if (message.isFromUser) Alignment.End else Alignment.Start) {
            Card(
                shape = if (message.isFromUser)
                    RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp)
                else
                    RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (message.isFromUser) GoldPrimary else CardDark
                ),
                modifier = Modifier.widthIn(max = 280.dp)
            ) {
                Text(
                    text = message.text,
                    modifier = Modifier.padding(12.dp, 10.dp),
                    color = if (message.isFromUser) SurfaceDark
                            else MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Text(
                text  = timeFormatter.format(Date(message.timestamp)),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onBackground.copy(0.4f),
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }

        if (message.isFromUser) Spacer(Modifier.width(6.dp))
    }
}
