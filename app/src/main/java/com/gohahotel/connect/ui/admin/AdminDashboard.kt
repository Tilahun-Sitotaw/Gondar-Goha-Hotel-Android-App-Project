package com.gohahotel.connect.ui.admin

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gohahotel.connect.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboard(
    onNavigateToRooms: () -> Unit,
    onNavigateToMenu: () -> Unit,
    onNavigateToOrders: () -> Unit,
    onNavigateToUsers: () -> Unit,
    onNavigateToPromotions: () -> Unit,
    onNavigateToContent: () -> Unit,
    onNavigateToChat: () -> Unit = {},
    onLogout: () -> Unit,
    onBack: () -> Unit,
    viewModel: AdminViewModel = hiltViewModel()
) {
    val liveOrders by viewModel.liveOrdersCount.collectAsState(initial = 0)
    val occupancy by viewModel.occupancyRate.collectAsState(initial = 0)
    Scaffold(
        containerColor = SurfaceDark,
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Goha Control Center", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = OnSurfaceDark)
                        Text("Management Portal", style = MaterialTheme.typography.labelSmall, color = GoldPrimary.copy(alpha = 0.7f))
                    }
                },
                navigationIcon = { 
                    IconButton(onClick = onBack) { 
                        Icon(Icons.Default.Dashboard, null, tint = GoldPrimary) 
                    } 
                },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.Logout, "Logout", tint = GoldPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceDark.copy(alpha = 0.95f),
                    titleContentColor = GoldPrimary
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(SurfaceDark, Color(0xFF0A1424), Color.Black)))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // ── Executive Stats Row ──────────────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatMiniCard("Live Orders", String.format("%02d", liveOrders), Icons.Default.ShoppingCart, GoldPrimary, Modifier.weight(1f))
                    StatMiniCard("Occupancy", "$occupancy%", Icons.Default.Home, TealPrimary, Modifier.weight(1f))
                }

                Spacer(Modifier.height(24.dp))

                Text(
                    "MANAGEMENT TERMINAL",
                    style = MaterialTheme.typography.labelMedium,
                    color = GoldPrimary.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )

                Spacer(Modifier.height(16.dp))

                // ── Grid Layout for Tools ─────────────────────────────────────────
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        DashboardToolCard(
                            title = "Rooms",
                            subtitle = "Inventory",
                            icon = Icons.Default.Home,
                            color = GoldPrimary,
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToRooms
                        )
                        DashboardToolCard(
                            title = "Dining",
                            subtitle = "Restaurant",
                            icon = Icons.Default.List,
                            color = TealPrimary,
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToMenu
                        )
                    }
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        DashboardToolCard(
                            title = "Orders",
                            subtitle = "Active",
                            icon = Icons.Default.Assignment,
                            color = Color(0xFFE24A4A),
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToOrders
                        )
                        DashboardToolCard(
                            title = "Users",
                            subtitle = "Staff/Guests",
                            icon = Icons.Default.People,
                            color = Color(0xFF4A90E2),
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToUsers
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        DashboardToolCard(
                            title = "Events",
                            subtitle = "Promos",
                            icon = Icons.Default.Info,
                            color = Color(0xFFE29B4A),
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToPromotions
                        )
                        DashboardToolCard(
                            title = "Experiences",
                            subtitle = "Cultural",
                            icon = Icons.Default.Place,
                            color = Color(0xFFB04AE2),
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToContent
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        DashboardToolCard(
                            title = "Guest Chat",
                            subtitle = "Messages",
                            icon = Icons.Default.Chat,
                            color = Color(0xFF4A90E2),
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToChat
                        )
                        Spacer(Modifier.weight(1f))
                    }
                }

                Spacer(Modifier.height(32.dp))

                // ── System Status ────────────────────────────────────────────────
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = CardDark.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.1f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(SuccessGreen, CircleShape)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "Secure Admin Session Active · GOHA v1.0.4",
                            style = MaterialTheme.typography.labelSmall,
                            color = OnSurfaceDark.copy(alpha = 0.5f)
                        )
                    }
                }
                
                Spacer(Modifier.height(20.dp))

                // ── Switch to Guest View ─────────────────────────────────────────
                Button(
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.05f)),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.2f))
                ) {
                    Icon(Icons.Default.Visibility, null, tint = GoldPrimary)
                    Spacer(Modifier.width(12.dp))
                    Text("Enter Guest Mode", color = GoldPrimary)
                }

                Spacer(Modifier.height(40.dp))
            }
        }
    }
}

@Composable
private fun StatMiniCard(label: String, value: String, icon: ImageVector, color: Color, modifier: Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = CardDark.copy(alpha = 0.7f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Column(Modifier.padding(16.dp)) {
            Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(Modifier.height(8.dp))
            Text(value, fontSize = 24.sp, fontWeight = FontWeight.Black, color = OnSurfaceDark)
            Text(label, style = MaterialTheme.typography.labelSmall, color = OnSurfaceDark.copy(alpha = 0.6f))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DashboardToolCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = CardDark,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(56.dp),
                shape = CircleShape,
                color = color.copy(alpha = 0.15f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = color, modifier = Modifier.size(28.dp))
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(title, fontWeight = FontWeight.Bold, color = OnSurfaceDark, fontSize = 16.sp)
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = OnSurfaceDark.copy(alpha = 0.4f))
        }
    }
}
