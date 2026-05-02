package com.gohahotel.connect.ui.admin

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Console", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceDark, titleContentColor = GoldPrimary)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Brush.verticalGradient(listOf(SurfaceDark, Color.Black)))
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "System Management",
                style = MaterialTheme.typography.titleLarge,
                color = OnSurfaceDark,
                fontWeight = FontWeight.Bold
            )

            AdminCard(
                title = "Room Inventory",
                subtitle = "Manage rooms, prices, and status",
                icon = Icons.Default.Hotel,
                color = GoldPrimary,
                onClick = onNavigateToRooms
            )

            AdminCard(
                title = "Dining & Menu",
                subtitle = "Edit menu items and availability",
                icon = Icons.Default.RestaurantMenu,
                color = GoldPrimary,
                onClick = onNavigateToMenu
            )

            AdminCard(
                title = "Order Tracking",
                subtitle = "Monitor active room service orders",
                icon = Icons.Default.DeliveryDining,
                color = GoldPrimary,
                onClick = onNavigateToOrders
            )

            AdminCard(
                title = "Promotions & Events",
                subtitle = "Daily events and hotel videos",
                icon = Icons.Default.Campaign,
                color = GoldPrimary,
                onClick = onNavigateToPromotions
            )

            AdminCard(
                title = "Guest Directory",
                subtitle = "View and manage user accounts",
                icon = Icons.Default.People,
                color = GoldPrimary,
                onClick = onNavigateToUsers
            )

            AdminCard(
                title = "Content & Experiences",
                subtitle = "Manage cultural & sunset experiences",
                icon = Icons.Default.Tour,
                color = GoldPrimary,
                onClick = onNavigateToContent
            )
            
            Spacer(Modifier.height(24.dp))
            
            Text(
                "Goha Hotel Administration · v1.0",
                style = MaterialTheme.typography.labelSmall,
                color = OnSurfaceDark.copy(alpha = 0.4f),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = CardDark,
        border = BorderStroke(1.dp, color.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(50.dp),
                shape = RoundedCornerShape(12.dp),
                color = color.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
                }
            }
            Spacer(Modifier.width(20.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, color = OnSurfaceDark, fontSize = 18.sp)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = OnSurfaceDark.copy(alpha = 0.6f))
            }
            Icon(Icons.Default.ChevronRight, null, tint = OnSurfaceDark.copy(alpha = 0.3f))
        }
    }
}
