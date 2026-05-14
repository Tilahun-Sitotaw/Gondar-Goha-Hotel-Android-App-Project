package com.gohahotel.connect.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.gohahotel.connect.domain.model.Promotion
import com.gohahotel.connect.domain.model.PromotionType
import com.gohahotel.connect.ui.components.AutoScrollingCarousel
import com.gohahotel.connect.ui.components.VideoPlayer
import com.gohahotel.connect.ui.theme.*

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToRooms: () -> Unit,
    onNavigateToMenu: () -> Unit,
    onNavigateToConcierge: () -> Unit,
    onNavigateToGuide: () -> Unit,
    onNavigateToQr: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToAdmin: () -> Unit,
    onNavigateToTracking: (String) -> Unit,
    onNavigateToMyOrders: () -> Unit,
    onNavigateToEvent: (String) -> Unit = {},
    onNavigateToChat: () -> Unit = {},
    onNavigateToMyReservations: () -> Unit = {}
) {
    val uiState      by viewModel.uiState.collectAsState()
    var searchQuery  by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    // Filter events by search — but always show the section
    val filteredPromos = remember(searchQuery, uiState.promotions) {
        if (searchQuery.isBlank()) uiState.promotions
        else uiState.promotions.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
            it.description.contains(searchQuery, ignoreCase = true) ||
            it.type.name.contains(searchQuery, ignoreCase = true)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF071520), Color(0xFF050D18))))
            .verticalScroll(rememberScrollState())
    ) {

        // ── Compact Header ───────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(Color(0xFF0D2235), Color(0xFF071520))))
                .padding(horizontal = 20.dp)
                .padding(top = 48.dp, bottom = 16.dp)
        ) {
            Column {
                // Top row: greeting + icons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        // Greeting line
                        Text(
                            "Good ${uiState.greeting} ☀️",
                            style = MaterialTheme.typography.labelMedium,
                            color = GoldPrimary.copy(0.75f),
                            letterSpacing = 0.3.sp
                        )
                        // Name — slightly below greeting
                        Spacer(Modifier.height(2.dp))
                        Text(
                            uiState.guestName,
                            style = MaterialTheme.typography.headlineSmall,
                            color = OnSurfaceDark,
                            fontWeight = FontWeight.ExtraBold
                        )
                        if (uiState.isGuest) {
                            Spacer(Modifier.height(3.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = GoldPrimary.copy(0.1f),
                                border = BorderStroke(1.dp, GoldPrimary.copy(0.25f))
                            ) {
                                Text("Guest Mode",
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = GoldPrimary)
                            }
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (uiState.userRole == "ADMIN") {
                            IconButton(
                                onClick = onNavigateToAdmin,
                                modifier = Modifier.size(38.dp)
                                    .background(GoldPrimary.copy(0.15f), CircleShape)
                                    .border(1.dp, GoldPrimary.copy(0.3f), CircleShape)
                            ) {
                                Icon(Icons.Default.AdminPanelSettings, null,
                                    tint = GoldPrimary, modifier = Modifier.size(18.dp))
                            }
                        }
                        IconButton(
                            onClick = onNavigateToQr,
                            modifier = Modifier.size(38.dp).background(Color.White.copy(0.06f), CircleShape)
                        ) {
                            Icon(Icons.Default.QrCodeScanner, null,
                                tint = GoldLight, modifier = Modifier.size(18.dp))
                        }
                        IconButton(
                            onClick = onNavigateToSettings,
                            modifier = Modifier.size(38.dp).background(Color.White.copy(0.06f), CircleShape)
                        ) {
                            Icon(Icons.Default.Settings, null,
                                tint = GoldLight, modifier = Modifier.size(18.dp))
                        }
                    }
                }

                // Active booking card
                if (uiState.roomNumber.isNotBlank()) {
                    Spacer(Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color.White.copy(0.05f),
                        border = BorderStroke(1.dp, GoldPrimary.copy(0.2f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier.size(34.dp).background(GoldPrimary.copy(0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) { Icon(Icons.Default.Hotel, null, tint = GoldPrimary, modifier = Modifier.size(18.dp)) }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Room ${uiState.roomNumber}",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = GoldPrimary, fontWeight = FontWeight.Bold)
                                Text("${uiState.checkIn} → ${uiState.checkOut}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = OnSurfaceDark.copy(0.5f))
                            }
                            Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFF50C878).copy(0.15f)) {
                                Text("● Active",
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF50C878), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // ── Search bar ────────────────────────────────────────────────
                Spacer(Modifier.height(12.dp))
                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text("Search events, services...",
                            color = OnSurfaceDark.copy(0.35f),
                            style = MaterialTheme.typography.bodyMedium)
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Search, null,
                            tint = GoldPrimary.copy(0.7f), modifier = Modifier.size(18.dp))
                    },
                    trailingIcon = if (searchQuery.isNotBlank()) {
                        {
                            IconButton(onClick = { searchQuery = ""; focusManager.clearFocus() }) {
                                Icon(Icons.Default.Close, null,
                                    tint = OnSurfaceDark.copy(0.5f), modifier = Modifier.size(16.dp))
                            }
                        }
                    } else null,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                    shape = RoundedCornerShape(14.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor   = Color.White.copy(0.07f),
                        unfocusedContainerColor = Color.White.copy(0.05f),
                        focusedIndicatorColor   = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor             = GoldPrimary,
                        focusedTextColor        = OnSurfaceDark,
                        unfocusedTextColor      = OnSurfaceDark
                    )
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        // ── Events & Highlights — always visible, filtered by search ─────────
        if (uiState.promotions.isNotEmpty() || searchQuery.isNotBlank()) {
            Row(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Celebration, null, tint = GoldPrimary, modifier = Modifier.size(16.dp))
                Text("Events & Highlights",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = OnSurfaceDark,
                    modifier = Modifier.weight(1f))
                if (searchQuery.isNotBlank()) {
                    Text("${filteredPromos.size} result${if (filteredPromos.size != 1) "s" else ""}",
                        style = MaterialTheme.typography.labelSmall,
                        color = GoldPrimary.copy(0.7f))
                }
            }
            Spacer(Modifier.height(10.dp))

            if (filteredPromos.isEmpty() && searchQuery.isNotBlank()) {
                // Show empty state inline — don't hide the section
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .height(80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.SearchOff, null,
                            tint = OnSurfaceDark.copy(0.3f), modifier = Modifier.size(18.dp))
                        Text("No events match \"$searchQuery\"",
                            color = OnSurfaceDark.copy(0.4f),
                            style = MaterialTheme.typography.bodySmall)
                    }
                }
            } else {
                // Auto-scrolling carousel for events & highlights
                AutoScrollingCarousel(
                    items = filteredPromos,
                    modifier = Modifier.fillMaxWidth(),
                    onItemClick = { onNavigateToEvent(it) },
                    autoScrollDurationMillis = 4000
                )
            }
            Spacer(Modifier.height(18.dp))
        }

        // ── Hotel Services ────────────────────────────────────────────────────
        Row(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(Icons.Default.Apartment, null, tint = TealLight, modifier = Modifier.size(16.dp))
            Text("Hotel Services",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = OnSurfaceDark)
        }
        Spacer(Modifier.height(10.dp))

        val services = listOf(
            ServiceItem("Rooms & Booking",  Icons.Default.Bed,           GoldPrimary,       onNavigateToRooms),
            ServiceItem("Restaurant",       Icons.Default.Restaurant,    TerracottaLight,   onNavigateToMenu),
            ServiceItem("AI Concierge",     Icons.Default.SupportAgent,  TealLight,         onNavigateToConcierge),
            ServiceItem("Cultural Guide",   Icons.Default.Map,           Color(0xFF9B59B6), onNavigateToGuide),
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxWidth()
                .height(272.dp)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement   = Arrangement.spacedBy(12.dp),
            userScrollEnabled     = false
        ) {
            items(services) { service -> ServiceCard(service) }
        }

        Spacer(Modifier.height(18.dp))

        // ── Quick Actions ─────────────────────────────────────────────────────
        Row(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(Icons.Default.Bolt, null, tint = Color(0xFFE29B4A), modifier = Modifier.size(16.dp))
            Text("Quick Actions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = OnSurfaceDark)
        }
        Spacer(Modifier.height(8.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item { QuickChip("My Orders",      Icons.Default.ReceiptLong,    Color(0xFFE29B4A), onNavigateToMyOrders) }
            item { QuickChip("Reservations",   Icons.Default.Hotel,          GoldPrimary,       onNavigateToMyReservations) }
            item { QuickChip("Menu",           Icons.Default.RestaurantMenu, TerracottaLight,   onNavigateToMenu) }
            item { QuickChip("Concierge",      Icons.Default.SupportAgent,   TealLight,         onNavigateToConcierge) }
            item { QuickChip("Guide",          Icons.Default.Map,            Color(0xFF9B59B6), onNavigateToGuide) }
            item { QuickChip("Chat",           Icons.Default.Chat,           Color(0xFF4A90E2), onNavigateToChat) }
        }

        Spacer(Modifier.height(24.dp))

        // ── Compact Footer ────────────────────────────────────────────────────
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF040C14)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp, horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Thin gold divider
                Box(
                    modifier = Modifier
                        .width(60.dp)
                        .height(1.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color.Transparent, GoldPrimary.copy(0.5f), Color.Transparent)
                            )
                        )
                )
                Spacer(Modifier.height(14.dp))
                // Logo + name in one row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = GoldPrimary,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("G", color = Color(0xFF050D18), fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold)
                        }
                    }
                    Column {
                        Text("GOHA HOTEL",
                            style = MaterialTheme.typography.labelLarge,
                            color = GoldPrimary,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 2.sp)
                        Text("GONDAR · ETHIOPIA",
                            style = MaterialTheme.typography.labelSmall,
                            color = GoldLight.copy(0.5f),
                            letterSpacing = 1.5.sp)
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text("High Above the Historic City of Gondar",
                    style = MaterialTheme.typography.labelSmall,
                    color = OnSurfaceDark.copy(0.35f),
                    fontStyle = FontStyle.Italic,
                    textAlign = TextAlign.Center)
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    repeat(5) {
                        Icon(Icons.Default.Star, null,
                            tint = GoldPrimary.copy(0.45f), modifier = Modifier.size(11.dp))
                    }
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    "© ${java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)} Goha Hotel",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(0.18f))
            }
        }
    }
}

// ── Shared composables ────────────────────────────────────────────────────────

private data class ServiceItem(
    val label: String,
    val icon: ImageVector,
    val color: Color,
    val onClick: () -> Unit
)

@Composable
private fun ServiceCard(item: ServiceItem) {
    Surface(
        onClick = item.onClick,
        modifier = Modifier.fillMaxWidth().height(122.dp),
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFF0E1B2A),
        border = BorderStroke(1.dp, item.color.copy(0.18f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier.size(40.dp).background(item.color.copy(0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(item.icon, null, tint = item.color, modifier = Modifier.size(20.dp))
            }
            Text(item.label,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = OnSurfaceDark)
        }
    }
}

@Composable
private fun QuickChip(label: String, icon: ImageVector, color: Color, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = color.copy(0.1f),
        border = BorderStroke(1.dp, color.copy(0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(15.dp))
            Text(label,
                style = MaterialTheme.typography.labelMedium,
                color = OnSurfaceDark,
                fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun EventCard(promo: Promotion, onClick: () -> Unit) {
    val accentColor = when (promo.type) {
        PromotionType.EVENT     -> Color(0xFFE29B4A)
        PromotionType.PROMOTION -> Color(0xFF50C878)
        PromotionType.CULTURAL  -> Color(0xFF9B59B6)
        PromotionType.VIDEO     -> Color(0xFF4A90E2)
    }
    val typeEmoji = when (promo.type) {
        PromotionType.EVENT     -> "🎉"
        PromotionType.PROMOTION -> "🏷️"
        PromotionType.CULTURAL  -> "🎭"
        PromotionType.VIDEO     -> "🎬"
    }
    val isRecommended = promo.type == PromotionType.EVENT || promo.type == PromotionType.PROMOTION

    Surface(
        onClick = onClick,
        modifier = Modifier.width(230.dp).height(172.dp),
        shape = RoundedCornerShape(18.dp),
        color = Color(0xFF0E1B2A),
        border = BorderStroke(1.dp, accentColor.copy(0.25f))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background
            if (promo.type == PromotionType.VIDEO && promo.videoUrl.isNotBlank()) {
                VideoPlayer(
                    videoUrl = promo.videoUrl,
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(18.dp)),
                    autoPlay = true, muted = true, showControls = false
                )
            } else {
                val imageUrl = promo.allImages.firstOrNull() ?: ""
                if (imageUrl.isNotBlank()) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(18.dp))
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize()
                            .background(Brush.verticalGradient(
                                listOf(accentColor.copy(0.15f), Color(0xFF050D18))
                            )),
                        contentAlignment = Alignment.Center
                    ) { Text(typeEmoji, fontSize = 40.sp) }
                }
            }

            // Gradient overlay
            Box(
                modifier = Modifier.fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black.copy(0.72f)),
                            startY = 50f
                        )
                    )
            )

            // Top badges
            Row(
                modifier = Modifier.align(Alignment.TopStart).padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Surface(shape = RoundedCornerShape(7.dp), color = accentColor.copy(0.88f)) {
                    Text("$typeEmoji ${promo.type.name}",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Black, fontWeight = FontWeight.Bold)
                }
                if (isRecommended) {
                    Surface(shape = RoundedCornerShape(7.dp), color = Color(0xFFFFD700).copy(0.88f)) {
                        Row(
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Icon(Icons.Default.Star, null,
                                tint = Color.Black, modifier = Modifier.size(9.dp))
                            Text("Recommended",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Bottom content
            Column(
                modifier = Modifier.align(Alignment.BottomStart).padding(10.dp)
            ) {
                Text(promo.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis)
                if (promo.date.isNotBlank()) {
                    Text("📅 ${promo.date}",
                        style = MaterialTheme.typography.labelSmall,
                        color = accentColor.copy(0.9f))
                } else if (promo.description.isNotBlank()) {
                    Text(promo.description,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(0.55f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}

// BasicTextField is from androidx.compose.foundation.text — no wrapper needed

