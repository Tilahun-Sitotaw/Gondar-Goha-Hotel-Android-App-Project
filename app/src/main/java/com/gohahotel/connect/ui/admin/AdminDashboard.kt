package com.gohahotel.connect.ui.admin

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gohahotel.connect.domain.model.Booking
import com.gohahotel.connect.domain.model.BookingStatus
import com.gohahotel.connect.domain.model.Order
import com.gohahotel.connect.domain.model.OrderStatus
import com.gohahotel.connect.ui.theme.*
import java.util.Locale

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
    val allBookings by viewModel.allBookings.collectAsState(initial = emptyList())
    val allOrders by viewModel.allOrders.collectAsState(initial = emptyList())
    val isLoading by viewModel.isLoading.collectAsState(initial = false)
    
    var selectedTab by remember { mutableIntStateOf(0) }
    var bookingFilter by remember { mutableStateOf(BookingStatus.CONFIRMED) }
    var orderFilter by remember { mutableStateOf(OrderStatus.RECEIVED) }

    LaunchedEffect(Unit) {
        viewModel.loadAllBookings()
        viewModel.loadAllOrders()
    }
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
                        Icon(Icons.AutoMirrored.Filled.Logout, "Logout", tint = GoldPrimary)
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
            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GoldPrimary)
                }
            } else {
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
                        StatMiniCard("Live Orders", String.format(Locale.US, "%02d", liveOrders), Icons.Default.ShoppingCart, GoldPrimary, Modifier.weight(1f))
                        StatMiniCard("Occupancy", "$occupancy%", Icons.Default.Home, TealPrimary, Modifier.weight(1f))
                        StatMiniCard("Bookings", allBookings.size.toString(), Icons.Default.BookOnline, Color(0xFF4A90E2), Modifier.weight(1f))
                    }

                    Spacer(Modifier.height(24.dp))

                    // ── Tab Navigation ──────────────────────────────────────────────
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = CardDark,
                        contentColor = GoldPrimary,
                        indicator = { tabPositions ->
                            TabRowDefaults.Indicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                height = 3.dp,
                                color = GoldPrimary
                            )
                        }
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = { Text("Management", fontWeight = FontWeight.Bold) }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = { Text("Bookings", fontWeight = FontWeight.Bold) }
                        )
                        Tab(
                            selected = selectedTab == 2,
                            onClick = { selectedTab = 2 },
                            text = { Text("Orders", fontWeight = FontWeight.Bold) }
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    when (selectedTab) {
                        0 -> ManagementTab(
                            onNavigateToRooms = onNavigateToRooms,
                            onNavigateToMenu = onNavigateToMenu,
                            onNavigateToOrders = onNavigateToOrders,
                            onNavigateToUsers = onNavigateToUsers,
                            onNavigateToPromotions = onNavigateToPromotions,
                            onNavigateToContent = onNavigateToContent,
                            onNavigateToChat = onNavigateToChat,
                            onBack = onBack
                        )
                        1 -> BookingsTab(
                            bookings = allBookings,
                            selectedFilter = bookingFilter,
                            onFilterChange = { bookingFilter = it }
                        )
                        2 -> OrdersTab(
                            orders = allOrders,
                            selectedFilter = orderFilter,
                            onFilterChange = { orderFilter = it }
                        )
                    }

                    Spacer(Modifier.height(40.dp))
                }
            }
        }
    }
}

@Composable
private fun ManagementTab(
    onNavigateToRooms: () -> Unit,
    onNavigateToMenu: () -> Unit,
    onNavigateToOrders: () -> Unit,
    onNavigateToUsers: () -> Unit,
    onNavigateToPromotions: () -> Unit,
    onNavigateToContent: () -> Unit,
    onNavigateToChat: () -> Unit,
    onBack: () -> Unit
) {
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
                icon = Icons.AutoMirrored.Filled.List,
                color = TealPrimary,
                modifier = Modifier.weight(1f),
                onClick = onNavigateToMenu
            )
        }
        
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            DashboardToolCard(
                title = "Orders",
                subtitle = "Active",
                icon = Icons.AutoMirrored.Filled.Assignment,
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
                icon = Icons.AutoMirrored.Filled.Chat,
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
}

@Composable
private fun BookingsTab(
    bookings: List<Booking>,
    selectedFilter: BookingStatus,
    onFilterChange: (BookingStatus) -> Unit
) {
    Column {
        // Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BookingStatus.entries.forEach { status ->
                FilterChip(
                    selected = selectedFilter == status,
                    onClick = { onFilterChange(status) },
                    enabled = true,
                    label = { Text(status.displayName) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GoldPrimary,
                        selectedLabelColor = SurfaceDark,
                        containerColor = CardDark,
                        labelColor = OnSurfaceDark
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selectedFilter == status,
                        selectedBorderColor = GoldPrimary,
                        borderColor = GoldPrimary.copy(0.3f)
                    )
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        val filteredBookings = bookings.filter { it.status == selectedFilter }
        
        if (filteredBookings.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No ${selectedFilter.displayName.lowercase(Locale.US)} bookings", color = OnSurfaceDark.copy(0.5f))
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                filteredBookings.forEach { booking ->
                    AdminBookingCard(booking)
                }
            }
        }
    }
}

@Composable
private fun OrdersTab(
    orders: List<Order>,
    selectedFilter: OrderStatus,
    onFilterChange: (OrderStatus) -> Unit
) {
    Column {
        // Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                OrderStatus.RECEIVED,
                OrderStatus.PREPARING,
                OrderStatus.READY,
                OrderStatus.DELIVERED
            ).forEach { status ->
                FilterChip(
                    selected = selectedFilter == status,
                    onClick = { onFilterChange(status) },
                    enabled = true,
                    label = { Text(status.displayName) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GoldPrimary,
                        selectedLabelColor = SurfaceDark,
                        containerColor = CardDark,
                        labelColor = OnSurfaceDark
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selectedFilter == status,
                        selectedBorderColor = GoldPrimary,
                        borderColor = GoldPrimary.copy(0.3f)
                    )
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        val filteredOrders = orders.filter { it.status == selectedFilter }
        
        if (filteredOrders.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No ${selectedFilter.displayName.lowercase(Locale.US)} orders", color = OnSurfaceDark.copy(0.5f))
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                filteredOrders.forEach { order ->
                    AdminOrderCard(order)
                }
            }
        }
    }
}

@Composable
private fun AdminBookingCard(booking: Booking) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = CardDark,
        border = BorderStroke(1.dp, Color.White.copy(0.05f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(booking.guestName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(booking.roomName, style = MaterialTheme.typography.labelMedium, color = GoldPrimary)
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = parseColor(booking.status.color).copy(0.2f)
                ) {
                    Text(
                        booking.status.displayName,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = parseColor(booking.status.color),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                BookingDetailItem("Check-In", booking.checkInDate)
                BookingDetailItem("Check-Out", booking.checkOutDate)
                BookingDetailItem("Guests", booking.numberOfGuests.toString())
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(booking.guestEmail, style = MaterialTheme.typography.labelSmall, color = OnSurfaceDark.copy(0.7f))
                Text("${booking.currency} ${booking.totalPrice.toInt()}", fontWeight = FontWeight.Bold, color = GoldPrimary)
            }
        }
    }
}

@Composable
private fun AdminOrderCard(order: Order) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = CardDark,
        border = BorderStroke(1.dp, Color.White.copy(0.05f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Room ${order.roomNumber}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(order.guestName, style = MaterialTheme.typography.labelMedium, color = GoldPrimary)
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = getOrderStatusColor(order.status).copy(0.2f)
                ) {
                    Text(
                        order.status.userFriendlyName,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = getOrderStatusColor(order.status),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Text("${order.items.size} items", style = MaterialTheme.typography.labelSmall, color = OnSurfaceDark.copy(0.7f))

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Total: ${order.currency} ${order.totalAmount.toInt()}", fontWeight = FontWeight.Bold, color = GoldPrimary)
                Text("~${order.estimatedDeliveryMinutes} min", style = MaterialTheme.typography.labelSmall, color = OnSurfaceDark.copy(0.6f))
            }
        }
    }
}

@Composable
private fun BookingDetailItem(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall, color = OnSurfaceDark.copy(0.6f))
        Text(value, style = MaterialTheme.typography.labelMedium, color = OnSurfaceDark, fontWeight = FontWeight.Bold)
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

private fun getOrderStatusColor(status: OrderStatus): Color {
    return when (status) {
        OrderStatus.RECEIVED -> Color(0xFFFF9800)
        OrderStatus.PREPARING -> Color(0xFFFF6B6B)
        OrderStatus.READY -> SuccessGreen
        OrderStatus.ON_THE_WAY -> Color(0xFF2196F3)
        OrderStatus.DELIVERED -> SuccessGreen
        OrderStatus.PENDING -> Color(0xFFFF9800)
        OrderStatus.CANCELLED -> Color(0xFFF44336)
    }
}

private fun parseColor(hexColor: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(hexColor))
    } catch (e: Exception) {
        GoldPrimary
    }
}
