package com.gohahotel.connect.ui.staff

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gohahotel.connect.domain.model.Order
import com.gohahotel.connect.domain.model.OrderStatus
import com.gohahotel.connect.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KitchenOrdersScreen(
    viewModel: StaffViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedFilter by remember { mutableStateOf<OrderStatus?>(null) }

    val filteredOrders = if (selectedFilter != null) {
        uiState.orders.filter { it.status == selectedFilter }
    } else {
        uiState.orders.filter { it.status == OrderStatus.PENDING || it.status == OrderStatus.PREPARING }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kitchen Orders", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Filter chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedFilter == null,
                        onClick = { selectedFilter = null },
                        label = { Text("All") },
                        leadingIcon = if (selectedFilter == null) {
                            { Icon(Icons.Default.Check, null, modifier = Modifier.size(18.dp)) }
                        } else null
                    )
                }
                item {
                    FilterChip(
                        selected = selectedFilter == OrderStatus.PENDING,
                        onClick = { selectedFilter = OrderStatus.PENDING },
                        label = { Text("Pending") },
                        leadingIcon = if (selectedFilter == OrderStatus.PENDING) {
                            { Icon(Icons.Default.Check, null, modifier = Modifier.size(18.dp)) }
                        } else null
                    )
                }
                item {
                    FilterChip(
                        selected = selectedFilter == OrderStatus.PREPARING,
                        onClick = { selectedFilter = OrderStatus.PREPARING },
                        label = { Text("Preparing") },
                        leadingIcon = if (selectedFilter == OrderStatus.PREPARING) {
                            { Icon(Icons.Default.Check, null, modifier = Modifier.size(18.dp)) }
                        } else null
                    )
                }
                item {
                    FilterChip(
                        selected = selectedFilter == OrderStatus.READY,
                        onClick = { selectedFilter = OrderStatus.READY },
                        label = { Text("Ready") },
                        leadingIcon = if (selectedFilter == OrderStatus.READY) {
                            { Icon(Icons.Default.Check, null, modifier = Modifier.size(18.dp)) }
                        } else null
                    )
                }
            }

            // Orders list
            if (filteredOrders.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Default.RestaurantMenu,
                            null,
                            modifier = Modifier.size(64.dp),
                            tint = GoldPrimary.copy(0.3f)
                        )
                        Text(
                            "No orders",
                            style = MaterialTheme.typography.titleMedium,
                            color = OnSurfaceDark.copy(0.5f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    items(filteredOrders) { order ->
                        KitchenOrderCard(
                            order = order,
                            onMarkPreparing = {
                                viewModel.updateOrderStatus(order.id, OrderStatus.PENDING)
                            },
                            onMarkReady = {
                                viewModel.updateOrderStatus(order.id, OrderStatus.PREPARING)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun KitchenOrderCard(
    order: Order,
    onMarkPreparing: () -> Unit,
    onMarkReady: () -> Unit
) {
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val statusColor = when (order.status) {
        OrderStatus.PENDING -> Color(0xFFFF9800)
        OrderStatus.PREPARING -> Color(0xFF2196F3)
        OrderStatus.READY -> SuccessGreen
        else -> OnSurfaceDark.copy(0.5f)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF0E1B2A)
        ),
        border = BorderStroke(1.dp, statusColor.copy(0.3f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header with order ID and status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Order #${order.id.takeLast(6)}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = GoldPrimary
                    )
                    Text(
                        "Room ${order.roomNumber} • ${order.guestName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceDark.copy(0.7f)
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = statusColor.copy(0.15f),
                    border = BorderStroke(1.dp, statusColor.copy(0.5f))
                ) {
                    Text(
                        order.status.displayName,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Divider(color = OnSurfaceDark.copy(0.1f))

            // Items list
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Items",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = OnSurfaceDark
                )
                order.items.forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                item.menuItemName,
                                style = MaterialTheme.typography.bodySmall,
                                color = OnSurfaceDark
                            )
                            if (item.customization.isNotBlank()) {
                                Text(
                                    "Note: ${item.customization}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = GoldPrimary.copy(0.7f)
                                )
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = GoldPrimary.copy(0.15f)
                        ) {
                            Text(
                                "×${item.quantity}",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = GoldPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Special requests
            if (order.specialInstructions.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF1A2E3A),
                    border = BorderStroke(1.dp, GoldPrimary.copy(0.2f))
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            Icons.Default.Notes,
                            null,
                            modifier = Modifier.size(16.dp),
                            tint = GoldPrimary.copy(0.7f)
                        )
                        Text(
                            order.specialInstructions,
                            style = MaterialTheme.typography.labelSmall,
                            color = OnSurfaceDark.copy(0.8f)
                        )
                    }
                }
            }

            // Time info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Schedule,
                    null,
                    modifier = Modifier.size(16.dp),
                    tint = OnSurfaceDark.copy(0.5f)
                )
                Text(
                    "Ordered at ${timeFormat.format(order.createdAt)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = OnSurfaceDark.copy(0.5f)
                )
            }

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (order.status == OrderStatus.PENDING) {
                    Button(
                        onClick = onMarkPreparing,
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2196F3)
                        )
                    ) {
                        Icon(
                            Icons.Default.LocalFireDepartment,
                            null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("Start Preparing", fontWeight = FontWeight.Bold)
                    }
                } else if (order.status == OrderStatus.PREPARING) {
                    Button(
                        onClick = onMarkReady,
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SuccessGreen
                        )
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("Mark Ready", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
