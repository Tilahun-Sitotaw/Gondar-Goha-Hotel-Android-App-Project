package com.gohahotel.connect.ui.kitchen

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gohahotel.connect.domain.model.*
import com.gohahotel.connect.ui.theme.GoldPrimary
import com.gohahotel.connect.ui.theme.SurfaceDark
import com.gohahotel.connect.ui.theme.SuccessGreen
import com.gohahotel.connect.ui.theme.OnSurfaceDark
import com.gohahotel.connect.ui.theme.CardDark
import com.gohahotel.connect.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KitchenDashboard(
    onBack: () -> Unit,
    onLogout: () -> Unit,
    viewModel: KitchenViewModel = hiltViewModel()
) {
    val foodOrders by viewModel.foodOrders.collectAsState(initial = emptyList())
    val isLoading by viewModel.isLoading.collectAsState(initial = false)
    
    var selectedOrder by remember { mutableStateOf<Order?>(null) }
    var showOrderDetails by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadFoodOrders()
    }

    Scaffold(
        containerColor = SurfaceDark,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Kitchen Dashboard", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                        Text("Food Order Management", style = MaterialTheme.typography.labelSmall, color = GoldPrimary.copy(0.7f))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = GoldPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.Logout, "Logout", tint = GoldPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceDark.copy(0.95f)
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(SurfaceDark, Color(0xFF0A1424))))
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
                    // ── Quick Stats ──────────────────────────────────────────────
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        KitchenStatCard(
                            label = "Total Orders",
                            value = foodOrders.size.toString(),
                            icon = Icons.Default.ShoppingCart,
                            color = GoldPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        KitchenStatCard(
                            label = "Preparing",
                            value = foodOrders.count { it.status == OrderStatus.PREPARING }.toString(),
                            icon = Icons.Default.LocalFireDepartment,
                            color = StatusPreparing,
                            modifier = Modifier.weight(1f)
                        )
                        KitchenStatCard(
                            label = "Ready",
                            value = foodOrders.count { it.status == OrderStatus.READY }.toString(),
                            icon = Icons.Default.CheckCircle,
                            color = SuccessGreen,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(Modifier.height(24.dp))

                    // ── Order Status Legend ──────────────────────────────────────
                    Text(
                        "ORDER PREPARATION STATUS",
                        style = MaterialTheme.typography.labelMedium,
                        color = GoldPrimary.copy(0.6f),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                    Spacer(Modifier.height(12.dp))

                    // ── Horizontal Scrollable Order Cards ────────────────────────
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(foodOrders) { order ->
                            PerfectOrderCard(
                                order = order,
                                onClick = {
                                    selectedOrder = order
                                    showOrderDetails = true
                                }
                            )
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    // ── Detailed Order List ──────────────────────────────────────
                    Text(
                        "ACTIVE ORDERS",
                        style = MaterialTheme.typography.labelMedium,
                        color = GoldPrimary.copy(0.6f),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                    Spacer(Modifier.height(12.dp))

                    if (foodOrders.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.RestaurantMenu,
                                    null,
                                    modifier = Modifier.size(48.dp),
                                    tint = GoldPrimary.copy(0.3f)
                                )
                                Spacer(Modifier.height(12.dp))
                                Text(
                                    "No active food orders",
                                    color = OnSurfaceDark.copy(0.5f)
                                )
                            }
                        }
                    } else {
                        foodOrders.forEach { order ->
                            DetailedOrderCard(
                                order = order,
                                onStatusUpdate = { newStatus ->
                                    viewModel.updateOrderStatus(order.id, newStatus)
                                },
                                onClick = {
                                    selectedOrder = order
                                    showOrderDetails = true
                                }
                            )
                            Spacer(Modifier.height(12.dp))
                        }
                    }

                    Spacer(Modifier.height(40.dp))
                }
            }
        }
    }

    // ── Order Details Dialog ─────────────────────────────────────────────────
    if (showOrderDetails && selectedOrder != null) {
        OrderDetailsDialog(
            order = selectedOrder!!,
            onDismiss = { showOrderDetails = false },
            onStatusUpdate = { newStatus ->
                viewModel.updateOrderStatus(selectedOrder!!.id, newStatus)
                showOrderDetails = false
            }
        )
    }
}

@Composable
private fun KitchenStatCard(
    label: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = CardDark.copy(0.7f),
        border = BorderStroke(1.dp, color.copy(0.2f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(Modifier.height(6.dp))
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = OnSurfaceDark)
            Text(label, style = MaterialTheme.typography.labelSmall, color = OnSurfaceDark.copy(0.6f))
        }
    }
}

@Composable
private fun PerfectOrderCard(
    order: Order,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .width(200.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = CardDark,
        border = BorderStroke(2.dp, getStatusColor(order.status).copy(0.5f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Order Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Room ${order.roomNumber}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = OnSurfaceDark
                    )
                    Text(
                        order.guestName,
                        style = MaterialTheme.typography.labelSmall,
                        color = OnSurfaceDark.copy(0.7f)
                    )
                }
                Surface(
                    shape = CircleShape,
                    color = getStatusColor(order.status),
                    modifier = Modifier.size(8.dp),
                    content = {}
                )
            }

            // Status Badge
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = getStatusColor(order.status).copy(0.2f)
            ) {
                Text(
                    order.status.userFriendlyName,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = getStatusColor(order.status),
                    fontWeight = FontWeight.Bold
                )
            }

            // Items Count
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    Icons.Default.RestaurantMenu,
                    null,
                    modifier = Modifier.size(14.dp),
                    tint = GoldPrimary
                )
                Text(
                    "${order.items.size} item${if (order.items.size != 1) "s" else ""}",
                    style = MaterialTheme.typography.labelSmall,
                    color = OnSurfaceDark.copy(0.8f)
                )
            }

            // Items List
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                order.items.take(3).forEach { item ->
                    Text(
                        "• ${item.quantity}x ${item.menuItemName}",
                        style = MaterialTheme.typography.labelSmall,
                        color = OnSurfaceDark.copy(0.7f),
                        maxLines = 1
                    )
                }
                if (order.items.size > 3) {
                    Text(
                        "+${order.items.size - 3} more",
                        style = MaterialTheme.typography.labelSmall,
                        color = GoldPrimary
                    )
                }
            }

            // Estimated Time
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    Icons.Default.Schedule,
                    null,
                    modifier = Modifier.size(12.dp),
                    tint = OnSurfaceDark.copy(0.6f)
                )
                Text(
                    "~${order.estimatedDeliveryMinutes} min",
                    style = MaterialTheme.typography.labelSmall,
                    color = OnSurfaceDark.copy(0.6f)
                )
            }
        }
    }
}

@Composable
private fun DetailedOrderCard(
    order: Order,
    onStatusUpdate: (OrderStatus) -> Unit,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
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
                    Text(
                        "Room ${order.roomNumber}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = OnSurfaceDark
                    )
                    Text(
                        order.guestName,
                        style = MaterialTheme.typography.labelMedium,
                        color = GoldPrimary
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = getStatusColor(order.status).copy(0.2f)
                ) {
                    Text(
                        order.status.userFriendlyName,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = getStatusColor(order.status),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Items
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                order.items.forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "${item.quantity}x ${item.menuItemName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceDark.copy(0.8f)
                        )
                        if (item.customization.isNotEmpty()) {
                            Text(
                                item.customization,
                                style = MaterialTheme.typography.labelSmall,
                                color = OnSurfaceDark.copy(0.6f)
                            )
                        }
                    }
                }
            }

            if (order.specialInstructions.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFF9800).copy(0.1f)
                ) {
                    Text(
                        "Note: ${order.specialInstructions}",
                        modifier = Modifier.padding(8.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFFF9800)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Status Update Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (order.status == OrderStatus.RECEIVED) {
                    Button(
                        onClick = { onStatusUpdate(OrderStatus.PREPARING) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = StatusPreparing),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Start Prep", fontSize = 12.sp)
                    }
                } else if (order.status == OrderStatus.PREPARING) {
                    Button(
                        onClick = { onStatusUpdate(OrderStatus.READY) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Mark Ready", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderDetailsDialog(
    order: Order,
    onDismiss: () -> Unit,
    onStatusUpdate: (OrderStatus) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0A1424),
        shape = RoundedCornerShape(24.dp),
        title = {
            Text("Order #${order.id.take(8)}", fontWeight = FontWeight.Bold, fontSize = 20.sp)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OrderDetailRow("Guest", order.guestName)
                OrderDetailRow("Room", order.roomNumber)
                OrderDetailRow("Status", order.status.userFriendlyName)
                OrderDetailRow("Items", order.items.size.toString())
                
                Spacer(Modifier.height(8.dp))
                
                Text("Items:", fontWeight = FontWeight.Bold, color = GoldPrimary)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    order.items.forEach { item ->
                        Text(
                            "• ${item.quantity}x ${item.menuItemName}",
                            style = MaterialTheme.typography.labelSmall,
                            color = OnSurfaceDark.copy(0.8f)
                        )
                    }
                }

                if (order.specialInstructions.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFFF9800).copy(0.1f)
                    ) {
                        Text(
                            "Special: ${order.specialInstructions}",
                            modifier = Modifier.padding(8.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFFF9800)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
            ) {
                Text("Close", color = SurfaceDark)
            }
        }
    )
}

@Composable
private fun OrderDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = OnSurfaceDark.copy(0.7f))
        Text(value, fontWeight = FontWeight.Bold, color = GoldPrimary)
    }
}

private fun getStatusColor(status: OrderStatus): Color {
    return when (status) {
        OrderStatus.RECEIVED -> StatusReceived
        OrderStatus.PREPARING -> StatusPreparing
        OrderStatus.READY -> SuccessGreen
        OrderStatus.ON_THE_WAY -> InfoBlue
        OrderStatus.DELIVERED -> SuccessGreen
        OrderStatus.PENDING -> WarningAmber
        OrderStatus.CANCELLED -> ErrorRed
    }
}
