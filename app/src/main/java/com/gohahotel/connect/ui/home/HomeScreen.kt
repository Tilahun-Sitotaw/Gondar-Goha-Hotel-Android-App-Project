package com.gohahotel.connect.ui.home

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
    onNavigateToTracking: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        // ── Luxury Hero Header ────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .background(
                    Brush.verticalGradient(listOf(TealDark, SurfaceDark, SurfaceDark))
                )
        ) {
            // Subtle texture/light effect
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.radialGradient(listOf(GoldPrimary.copy(0.1f), Color.Transparent)))
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .padding(top = 40.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Good ${uiState.greeting},",
                            style = MaterialTheme.typography.bodyMedium,
                            color = GoldLight.copy(alpha = 0.6f)
                        )
                        Text(
                            text = uiState.guestName,
                            style = MaterialTheme.typography.headlineMedium,
                            color = GoldPrimary,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (uiState.userRole == "ADMIN") {
                            IconButton(
                                onClick = onNavigateToAdmin,
                                modifier = Modifier.background(GoldPrimary.copy(0.15f), CircleShape)
                            ) {
                                Icon(Icons.Default.AdminPanelSettings, "Admin", tint = GoldPrimary)
                            }
                        }
                        IconButton(
                            onClick = onNavigateToQr,
                            modifier = Modifier.background(Color.White.copy(0.05f), CircleShape)
                        ) {
                            Icon(Icons.Default.QrCodeScanner, "QR", tint = GoldLight)
                        }
                        IconButton(
                            onClick = onNavigateToSettings,
                            modifier = Modifier.background(Color.White.copy(0.05f), CircleShape)
                        ) {
                            Icon(Icons.Default.Settings, "Settings", tint = GoldLight)
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Modern Stay Info Card
                if (uiState.roomNumber.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White.copy(alpha = 0.05f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier.size(40.dp).background(GoldPrimary.copy(0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Hotel, null, tint = GoldPrimary, modifier = Modifier.size(20.dp))
                            }
                            Column {
                                Text("Your Sanctuary: Room ${uiState.roomNumber}",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = GoldPrimary, fontWeight = FontWeight.Bold)
                                Text("${uiState.checkIn} — ${uiState.checkOut}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = OnSurfaceDark.copy(alpha = 0.5f))
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── Daily Events & Promotions ────────────────────────────────────────
        if (uiState.promotions.isNotEmpty()) {
            Text(
                "Events & Highlights",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(uiState.promotions) { promo ->
                    PromotionCard(promo)
                }
            }
        }

        // ── Main Services Grid ────────────────────────────────────────────────
        Text(
            "Hotel Services",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
        )

        val services = listOf(
            ServiceItem("Rooms & Booking", Icons.Default.Bed, GoldPrimary, onNavigateToRooms),
            ServiceItem("Restaurant", Icons.Default.Restaurant, TerracottaLight, onNavigateToMenu),
            ServiceItem("Concierge", Icons.Default.SupportAgent, GoldLight, onNavigateToConcierge),
            ServiceItem("Cultural Guide", Icons.Default.Map, TealLight, onNavigateToGuide),
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            userScrollEnabled = false
        ) {
            items(services) { service ->
                ServiceCard(service)
            }
        }

        Spacer(Modifier.height(24.dp))

        // ── Quick Features ────────────────────────────────────────────────────
        Text(
            "Quick Actions",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(Modifier.height(12.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { QuickActionChip("Scan Menu", Icons.Default.QrCodeScanner, onNavigateToQr) }
            item { QuickActionChip("Service", Icons.Default.RoomService, onNavigateToMenu) }
            item { QuickActionChip("Sunset View", Icons.Default.WbSunny) { viewModel.scheduleSunsetAlert() } }
        }

        Spacer(Modifier.height(40.dp))

        // ── Professional Luxury Footer ────────────────────────────────────────
        Column(
            modifier = Modifier.fillMaxWidth().padding(bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "High Above the Historic City of Gondar",
                style = MaterialTheme.typography.labelMedium,
                color = GoldPrimary.copy(0.6f),
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
            )
            Spacer(Modifier.height(8.dp))
            Row {
                repeat(5) {
                    Icon(Icons.Default.Star, null, tint = GoldPrimary, modifier = Modifier.size(14.dp))
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(
                "© Goha Hotel · Gondar, Ethiopia",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
            )
        }
    }
}

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
        modifier = Modifier.fillMaxWidth().height(140.dp),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(item.color.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(item.icon, null, tint = item.color, modifier = Modifier.size(24.dp))
            }
            Text(
                text = item.label,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun QuickActionChip(label: String, icon: ImageVector, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = GoldPrimary, modifier = Modifier.size(18.dp))
            Text(label, style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun PromotionCard(promo: Promotion) {
    Surface(
        modifier = Modifier
            .width(280.dp)
            .height(160.dp),
        shape = RoundedCornerShape(24.dp),
        color = CardDark,
        border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.1f))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (promo.type == PromotionType.VIDEO && promo.videoUrl.isNotBlank()) {
                VideoPlayer(videoUrl = promo.videoUrl)
            } else {
                AsyncImage(
                    model = promo.imageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(24.dp))
                )
            }
            
            // Overlay gradient
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)),
                            startY = 100f
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Surface(
                    color = GoldPrimary,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = promo.type.name,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = promo.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = promo.description,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.7f),
                    maxLines = 1
                )
            }
        }
    }
}
