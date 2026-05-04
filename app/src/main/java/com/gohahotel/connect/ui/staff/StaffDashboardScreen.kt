package com.gohahotel.connect.ui.staff

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gohahotel.connect.domain.model.Booking
import com.gohahotel.connect.domain.model.Order
import com.gohahotel.connect.domain.model.OrderStatus
import com.gohahotel.connect.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffDashboardScreen(
    viewModel: StaffViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            val title = when (uiState.userRole) {
                "KITCHEN" -> "Kitchen Operations"
                "RECEPTION" -> "Front Desk & Reception"
                "HOUSEKEEPING" -> "Housekeeping"
                else -> "Staff Dashboard"
            }
            TopAppBar(
                title = { Text(title, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") }
                },
                actions = {
                    IconButton(onClick = {}) { Icon(Icons.Default.Notifications, null) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceDark)
            )
        },
        floatingActionButton = {
            if (uiState.userRole == "RECEPTION" && uiState.selectedTab == 0) {
                val context = androidx.compose.ui.platform.LocalContext.current
                FloatingActionButton(
                    onClick = { 
                        android.widget.Toast.makeText(context, "Walk-in Guest Registration coming soon!", android.widget.Toast.LENGTH_SHORT).show()
                    },
                    containerColor = GoldPrimary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Register Walk-in Guest", tint = SurfaceDark)
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (uiState.userRole) {
                "KITCHEN" -> {
                    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                        ActiveOrdersList(uiState.orders, viewModel::updateOrderStatus)
                    }
                }
                "HOUSEKEEPING" -> {
                    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                        EmptyState("All rooms are currently clean.", Icons.Default.CleaningServices)
                    }
                }
                else -> { // RECEPTION or STAFF or ADMIN
                    TabRow(
                        selectedTabIndex = uiState.selectedTab,
                        containerColor = SurfaceDark,
                        contentColor = GoldPrimary,
                        indicator = { tabPositions ->
                            TabRowDefaults.Indicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[uiState.selectedTab]),
                                color = GoldPrimary
                            )
                        }
                    ) {
                        Tab(
                            selected = uiState.selectedTab == 0,
                            onClick = { viewModel.selectTab(0) },
                            text = { Text("Bookings (${uiState.bookings.size})") }
                        )
                        Tab(
                            selected = uiState.selectedTab == 1,
                            onClick = { viewModel.selectTab(1) },
                            text = { Text("Active Orders (${uiState.orders.size})") }
                        )
                    }

                    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                        when (uiState.selectedTab) {
                            0 -> BookingsList(uiState.bookings)
                            1 -> ActiveOrdersList(uiState.orders, viewModel::updateOrderStatus)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ActiveOrdersList(orders: List<Order>, onUpdateStatus: (String, OrderStatus) -> Unit) {
    if (orders.isEmpty()) {
        EmptyState("No active orders", Icons.Default.Restaurant)
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(orders) { order ->
                StaffOrderCard(order, onUpdateStatus)
            }
        }
    }
}

@Composable
private fun BookingsList(bookings: List<Booking>) {
    if (bookings.isEmpty()) {
        EmptyState("No bookings found", Icons.Default.Hotel)
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(bookings) { booking ->
                StaffBookingCard(booking)
            }
        }
    }
}

@Composable
private fun StaffOrderCard(order: Order, onUpdateStatus: (String, OrderStatus) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Room ${order.roomNumber}", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = GoldPrimary)
                    Text(order.guestName, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(0.6f))
                }
                Surface(
                    shape = RoundedCornerShape(50),
                    color = when(order.status) {
                        OrderStatus.RECEIVED -> StatusReceived.copy(0.2f)
                        OrderStatus.PREPARING -> StatusPreparing.copy(0.2f)
                        OrderStatus.ON_THE_WAY -> StatusOnWay.copy(0.2f)
                        else -> SuccessGreen.copy(0.2f)
                    }
                ) {
                    Text(
                        text = order.status.displayName,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        color = when(order.status) {
                            OrderStatus.RECEIVED -> StatusReceived
                            OrderStatus.PREPARING -> StatusPreparing
                            OrderStatus.ON_THE_WAY -> StatusOnWay
                            else -> SuccessGreen
                        },
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = Color.White.copy(0.1f))
            Spacer(Modifier.height(12.dp))

            order.items.forEach { item ->
                Text("• ${item.quantity}x ${item.menuItemName}", style = MaterialTheme.typography.bodyMedium)
            }

            if (order.specialInstructions.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text("Note: ${order.specialInstructions}", style = MaterialTheme.typography.bodySmall, color = GoldLight.copy(0.7f))
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = { onUpdateStatus(order.id, order.status) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
            ) {
                val nextAction = when(order.status) {
                    OrderStatus.RECEIVED -> "Start Preparing"
                    OrderStatus.PREPARING -> "Mark as Sent"
                    OrderStatus.ON_THE_WAY -> "Confirm Delivery"
                    else -> "Completed"
                }
                Text(nextAction, color = SurfaceDark, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun StaffBookingCard(booking: Booking) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(booking.guestName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("${booking.roomName} (${booking.roomType})", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(0.6f))
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DateRange, null, Modifier.size(14.dp), tint = GoldPrimary)
                    Spacer(Modifier.width(4.dp))
                    Text("${booking.checkInDate} — ${booking.checkOutDate}", style = MaterialTheme.typography.labelSmall, color = GoldPrimary)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("ETB ${booking.totalPrice.toInt()}", fontWeight = FontWeight.Bold, color = GoldPrimary)
                Text("${booking.numberOfNights} nights", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(0.4f))
            }
        }
    }
}

@Composable
private fun EmptyState(message: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, modifier = Modifier.size(64.dp), tint = Color.White.copy(0.1f))
            Spacer(Modifier.height(16.dp))
            Text(message, color = Color.White.copy(0.3f))
        }
    }
}
