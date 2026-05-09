package com.gohahotel.connect.ui.events

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.gohahotel.connect.domain.model.Promotion
import com.gohahotel.connect.domain.model.PromotionType
import com.gohahotel.connect.ui.components.VideoPlayer
import com.gohahotel.connect.ui.theme.*

/**
 * EventDetailScreen — shows full details of a Promotion/Event from Firestore
 * and lets authenticated users register interest / book a spot.
 * Guests are prompted to sign in before booking.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    eventId: String,
    viewModel: EventViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(eventId) { viewModel.loadEvent(eventId) }

    val event = uiState.event
    val accentColor = event?.let { promoAccent(it.type) } ?: GoldPrimary

    Scaffold(
        containerColor = Color(0xFF050D18),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        event?.title ?: "Event Details",
                        color = OnSurfaceDark,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = GoldPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF050D18))
            )
        },
        bottomBar = {
            if (event != null && event.type == PromotionType.EVENT) {
                Surface(
                    color = Color(0xFF0A1424),
                    border = BorderStroke(1.dp, accentColor.copy(0.2f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        if (uiState.isBooked) {
                            // Confirmed state
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                color = Color(0xFF50C878).copy(0.12f),
                                border = BorderStroke(1.dp, Color(0xFF50C878).copy(0.4f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Icon(Icons.Default.CheckCircle, null,
                                        tint = Color(0xFF50C878), modifier = Modifier.size(24.dp))
                                    Column {
                                        Text("You're registered!",
                                            color = Color(0xFF50C878),
                                            fontWeight = FontWeight.Bold)
                                        Text("We'll see you at the event.",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(0xFF50C878).copy(0.7f))
                                    }
                                }
                            }
                        } else {
                            Button(
                                onClick = {
                                    if (viewModel.isGuest) onNavigateToLogin()
                                    else viewModel.registerForEvent(eventId)
                                },
                                modifier = Modifier.fillMaxWidth().height(54.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                                enabled = !uiState.isLoading
                            ) {
                                if (uiState.isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = Color.Black, strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(
                                        if (viewModel.isGuest) Icons.Default.Login else Icons.Default.EventAvailable,
                                        null, tint = Color.Black,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        if (viewModel.isGuest) "Sign In to Register" else "Register for Event",
                                        color = Color.Black, fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            if (viewModel.isGuest) {
                                Text(
                                    "Sign in to save your spot",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 6.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = OnSurfaceDark.copy(0.4f),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                        uiState.error?.let {
                            Text(it, color = Color(0xFFE24A4A),
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(top = 6.dp))
                        }
                    }
                }
            }
        }
    ) { padding ->
        if (event == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = GoldPrimary)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
            ) {
                // Hero image / banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .background(accentColor.copy(0.08f))
                ) {
                    // Show video with controls if available, otherwise image
                    if (event.videoUrl.isNotBlank()) {
                        VideoPlayer(
                            videoUrl     = event.videoUrl,
                            modifier     = Modifier.fillMaxSize(),
                            autoPlay     = true,
                            muted        = false,
                            showControls = true
                        )
                    } else {
                        val imageUrl = event.allImages.firstOrNull() ?: ""
                        if (imageUrl.isNotBlank()) {
                            AsyncImage(
                                model = imageUrl,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(accentColor.copy(0.2f), Color(0xFF050D18))
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(promoEmoji(event.type), fontSize = 72.sp)
                            }
                        }
                    }
                    // Gradient overlay (only for images, not video)
                    if (event.videoUrl.isBlank()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color.Transparent, Color.Black.copy(0.7f))
                                    )
                                )
                        )
                    }
                    // Type badge
                    Surface(
                        modifier = Modifier.align(Alignment.TopStart).padding(16.dp),
                        shape = RoundedCornerShape(10.dp),
                        color = accentColor.copy(0.9f)
                    ) {
                        Text(
                            "${promoEmoji(event.type)} ${event.type.name}",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.Black, fontWeight = FontWeight.Bold
                        )
                    }
                    // Live badge
                    if (event.isActive) {
                        Surface(
                            modifier = Modifier.align(Alignment.TopEnd).padding(16.dp),
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF50C878).copy(0.9f)
                        ) {
                            Text("● LIVE",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Title
                    Text(event.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = OnSurfaceDark)

                    // Info chips
                    if (event.date.isNotBlank() || event.type == PromotionType.EVENT) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (event.date.isNotBlank()) {
                                EventInfoChip(Icons.Default.CalendarToday, event.date, accentColor)
                            }
                            EventInfoChip(Icons.Default.LocationOn, "Goha Hotel, Gondar", TealLight)
                        }
                    }

                    HorizontalDivider(color = Color.White.copy(0.08f))

                    // Description
                    if (event.description.isNotBlank()) {
                        Text("About This Event",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = OnSurfaceDark)
                        Text(event.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = OnSurfaceDark.copy(0.75f),
                            lineHeight = 22.sp)
                    }

                    // Registration count (if available)
                    if (uiState.registrationCount > 0) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = accentColor.copy(0.08f),
                            border = BorderStroke(1.dp, accentColor.copy(0.2f))
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(Icons.Default.People, null,
                                    tint = accentColor, modifier = Modifier.size(20.dp))
                                Text("${uiState.registrationCount} people registered",
                                    color = accentColor, fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }

                    // Venue info
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFF0E1B2A),
                        border = BorderStroke(1.dp, Color.White.copy(0.06f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Venue Details",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = OnSurfaceDark)
                            VenueRow(Icons.Default.LocationOn, "Goha Hotel", "Gondar, Ethiopia", TealLight)
                            VenueRow(Icons.Default.Phone, "Contact", "+251 58 111 0000", GoldPrimary)
                            VenueRow(Icons.Default.Language, "Website", "www.gohahotel.com", Color(0xFF4A90E2))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EventInfoChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    color: Color
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = color.copy(0.1f),
        border = BorderStroke(1.dp, color.copy(0.25f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(14.dp))
            Text(text, style = MaterialTheme.typography.labelMedium, color = color)
        }
    }
}

@Composable
private fun VenueRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier.size(32.dp).background(color.copy(0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(16.dp))
        }
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = OnSurfaceDark.copy(0.5f))
            Text(value, style = MaterialTheme.typography.bodySmall, color = OnSurfaceDark, fontWeight = FontWeight.Medium)
        }
    }
}

private fun promoAccent(type: PromotionType): Color = when (type) {
    PromotionType.EVENT     -> Color(0xFFE29B4A)
    PromotionType.PROMOTION -> Color(0xFF50C878)
    PromotionType.CULTURAL  -> Color(0xFF9B59B6)
    PromotionType.VIDEO     -> Color(0xFF4A90E2)
}

private fun promoEmoji(type: PromotionType): String = when (type) {
    PromotionType.EVENT     -> "🎉"
    PromotionType.PROMOTION -> "🏷️"
    PromotionType.CULTURAL  -> "🎭"
    PromotionType.VIDEO     -> "🎬"
}
