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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gohahotel.connect.domain.model.LoyaltyTier
import com.gohahotel.connect.ui.loyalty.LoyaltyViewModel
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
    onNavigateToTracking: (String) -> Unit
)
 {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        // ── Hero Header ───────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .background(
                    Brush.verticalGradient(listOf(TealDark, SurfaceDark))
                )
        ) {
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
                            color = GoldLight.copy(alpha = 0.7f)
                        )
                        Text(
                            text = uiState.guestName,
                            style = MaterialTheme.typography.headlineSmall,
                            color = GoldPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(onClick = onNavigateToQr) {
                            Icon(Icons.Default.QrCodeScanner, "QR Scanner",
                                tint = GoldLight, modifier = Modifier.size(28.dp))
                        }
                        IconButton(onClick = onNavigateToSettings) {
                            Icon(Icons.Default.Settings, "Settings",
                                tint = GoldLight, modifier = Modifier.size(28.dp))
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Stay info card
                if (uiState.roomNumber.isNotBlank()) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White.copy(alpha = 0.08f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Hotel, null, tint = GoldPrimary)
                            Column {
                                Text("Room ${uiState.roomNumber}",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = GoldPrimary, fontWeight = FontWeight.Bold)
                                Text("${uiState.checkIn} → ${uiState.checkOut}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                            }
                        }
                    }
                }
            }
        }

        // ── Sunset Alert Banner ───────────────────────────────────────────────
        AnimatedVisibility(uiState.showSunsetAlert) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .offset(y = (-20).dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = TerracottaDark)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🌅", fontSize = 28.sp)
                    Column(Modifier.weight(1f)) {
                        Text("Sunset Alert!", style = MaterialTheme.typography.titleSmall,
                            color = Color.White, fontWeight = FontWeight.Bold)
                        Text("Best viewing time in ${uiState.sunsetMinutes} min — head to the terrace!",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.85f))
                    }
                    IconButton(onClick = viewModel::dismissSunsetAlert) {
                        Icon(Icons.Default.Close, null, tint = Color.White.copy(alpha = 0.7f))
                    }
                }
            }
        }

        Spacer(Modifier.height(if (uiState.showSunsetAlert) 0.dp else 20.dp))

        // ── Main Services Grid ────────────────────────────────────────────────
        Text(
            "Hotel Services",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
        )

        val services = listOf(
            ServiceItem("Rooms & Booking", Icons.Default.Hotel, GoldPrimary, TealDark, onNavigateToRooms),
            ServiceItem("Restaurant", Icons.Default.Restaurant, TerracottaLight, TerracottaDark, onNavigateToMenu),
            ServiceItem("Concierge", Icons.Default.SupportAgent, GoldLight, GoldDark, onNavigateToConcierge),
            ServiceItem("Cultural Guide", Icons.Default.Map, TealLight, TealDark, onNavigateToGuide),
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            userScrollEnabled = false
        ) {
            items(services) { service ->
                ServiceCard(service)
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── Quick Features ────────────────────────────────────────────────────
        Text(
            "Quick Actions",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { QuickActionChip("Scan QR", Icons.Default.QrCodeScanner, onNavigateToQr) }
            item { QuickActionChip("In-Room Request", Icons.Default.RoomService, onNavigateToMenu) }
            item { QuickActionChip("Sunset Alert", Icons.Default.WbSunny) { viewModel.scheduleSunsetAlert() } }
            item { QuickActionChip("My Orders", Icons.Default.ReceiptLong) {} }
        }

        Spacer(Modifier.height(32.dp))

        // ── Hotel Tagline ─────────────────────────────────────────────────────
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("🌄", fontSize = 36.sp)
                Spacer(Modifier.height(8.dp))
                Text(
                    "Perched on Gondar's historic hilltop,\nGoha Hotel has welcomed guests since 1968.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

// ─── Sub-components ────────────────────────────────────────────────────────────
private data class ServiceItem(
    val label: String,
    val icon: ImageVector,
    val iconTint: Color,
    val bgColor: Color,
    val onClick: () -> Unit
)

@Composable
private fun ServiceCard(item: ServiceItem) {
    Card(
        onClick = item.onClick,
        modifier = Modifier.fillMaxWidth().height(130.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(item.iconTint.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(item.icon, null, tint = item.iconTint, modifier = Modifier.size(24.dp))
            }
            Text(item.label, style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun QuickActionChip(label: String, icon: ImageVector, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = GoldPrimary, modifier = Modifier.size(16.dp))
            Text(label, style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
