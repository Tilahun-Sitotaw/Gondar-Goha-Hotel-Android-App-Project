package com.gohahotel.connect.ui.admin

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gohahotel.connect.domain.model.Order
import com.gohahotel.connect.domain.model.OrderStatus
import com.gohahotel.connect.ui.theme.*

// ─── Status filter tabs ───────────────────────────────────────────────────────
private val STATUS_FILTERS = listOf(
    null to "All",
    OrderStatus.RECEIVED   to "New",
    OrderStatus.PREPARING  to "Preparing",
    OrderStatus.ON_THE_WAY to "On the Way",
    OrderStatus.DELIVERED  to "Delivered"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminOrdersScreen(
    onBack: () -> Unit,
    viewModel: AdminViewModel = hiltViewModel()
) {
    val orders by viewModel.orders.collectAsState()
    var selectedFilter by remember { mutableStateOf<OrderStatus?>(null) }

    // Live pulse animation for the "LIVE" badge
    val infiniteTransition = rememberInfiniteTransition(label = "live")
    val livePulse by infiniteTransition.animateFloat(
        initialValue = 0.4f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
        label = "livePulse"
    )

    val filteredOrders = remember(orders, selectedFilter) {
        if (selectedFilter == null) orders
        else orders.filter { it.status == selectedFilter }
    }

    val pendingCount = orders.count {
        it.status == OrderStatus.RECEIVED || it.status == OrderStatus.PREPARING
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Column {
                            Text(
                                "Kitchen Dashboard",
                                fontWeight = FontWeight.ExtraBold,
                                color = GoldPrimary
                            )
                            Text(
                                "${orders.size} total orders",
                                style = MaterialTheme.typography.labelSmall,
                                color = OnSurfaceDark.copy(0.5f)
                            )
                        }
                        // LIVE badge
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF50C878).copy(livePulse * 0.25f),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp, Color(0xFF50C878).copy(livePulse)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(
                                            Color(0xFF50C878).copy(livePulse),
                                            CircleShape
                                        )
                                )
                                Text(
                                    "LIVE",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF50C878),
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = GoldPrimary)
                    }
                },
                actions = {
                    if (pendingCount > 0) {
                        BadgedBox(
                            badge = {
                                Badge(containerColor = ErrorRed) {
                                    Text("$pendingCount", color = Color.White, fontSize = 10.sp)
                                }
                            },
                            modifier = Modifier.padding(end = 12.dp)
                        ) {
                            Icon(Icons.Default.Notifications, null, tint = GoldPrimary)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceDark.copy(alpha = 0.97f),
                    titleContentColor = GoldPrimary
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF071520), Color(0xFF050D18), Color.Black)
                    )
                )
                .padding(padding)
        ) {
            Column {
                // ── Stats row ─────────────────────────────────────────────────
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        KitchenStatChip(
                            label = "New",
                            count = orders.count { it.status == OrderStatus.RECEIVED },
                            color = ErrorRed
                        )
                    }
                    item {
                        KitchenStatChip(
                            label = "Preparing",
                            count = orders.count { it.status == OrderStatus.PREPARING },
                            color = WarningAmber
                        )
                    }
                    item {
                        KitchenStatChip(
                            label = "On the Way",
                            count = orders.count { it.status == OrderStatus.ON_THE_WAY },
                            color = InfoBlue
                        )
                    }
                    item {
                        KitchenStatChip(
                            label = "Delivered",
                            count = orders.count { it.status == OrderStatus.DELIVERED },
                            color = SuccessGreen
                        )
                    }
                }

                // ── Filter tabs ───────────────────────────────────────────────
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(STATUS_FILTERS) { (status, label) ->
                        val isSelected = selectedFilter == status
                        val chipColor = when (status) {
                            OrderStatus.RECEIVED   -> ErrorRed
                            OrderStatus.PREPARING  -> WarningAmber
                            OrderStatus.ON_THE_WAY -> InfoBlue
                            OrderStatus.DELIVERED  -> SuccessGreen
                            OrderStatus.CANCELLED  -> Color.Gray
                            null                   -> GoldPrimary
                        }
                        FilterChip(
                            selected = isSelected,
                            onClick  = { selectedFilter = status },
                            label    = { Text(label, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            colors   = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = chipColor.copy(0.2f),
                                selectedLabelColor     = chipColor,
                                containerColor         = Color.White.copy(0.04f),
                                labelColor             = OnSurfaceDark.copy(0.6f)
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                selectedBorderColor = chipColor.copy(0.5f),
                                borderColor = Color.White.copy(0.1f)
                            )
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                // ── Orders list ───────────────────────────────────────────────
                if (filteredOrders.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.RoomService, null,
                                modifier = Modifier.size(72.dp),
                                tint = GoldPrimary.copy(0.08f)
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                if (selectedFilter == null) "No orders yet"
                                else "No ${selectedFilter?.displayName} orders",
                                color = OnSurfaceDark.copy(0.3f),
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                "Orders appear here in real time",
                                color = OnSurfaceDark.copy(0.2f),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(filteredOrders, key = { it.id }) { order ->
                            AnimatedVisibility(
                                visible = true,
                                enter = fadeIn() + slideInVertically()
                            ) {
                                KitchenOrderCard(
                                    order = order,
                                    onStatusUpdate = { newStatus ->
                                        viewModel.updateOrderStatus(order.id, newStatus)
                                    }
                                )
                            }
                        }
                        item { Spacer(Modifier.height(16.dp)) }
                    }
                }
            }
        }
    }
}

// ─── Stat chip ────────────────────────────────────────────────────────────────
@Composable
private fun KitchenStatChip(label: String, count: Int, color: Color) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(0.1f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "$count",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 20.sp,
                color = color
            )
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                color = OnSurfaceDark.copy(0.7f)
            )
        }
    }
}

// ─── Kitchen order card ───────────────────────────────────────────────────────
@Composable
fun KitchenOrderCard(order: Order, onStatusUpdate: (OrderStatus) -> Unit) {
    val statusColor = when (order.status) {
        OrderStatus.RECEIVED   -> ErrorRed
        OrderStatus.PREPARING  -> WarningAmber
        OrderStatus.ON_THE_WAY -> InfoBlue
        OrderStatus.DELIVERED  -> SuccessGreen
        OrderStatus.CANCELLED  -> Color.Gray
        else                   -> GoldPrimary
    }
    val statusEmoji = when (order.status) {
        OrderStatus.RECEIVED   -> "🔔"
        OrderStatus.PREPARING  -> "👨‍🍳"
        OrderStatus.ON_THE_WAY -> "🛎️"
        OrderStatus.DELIVERED  -> "✅"
        OrderStatus.CANCELLED  -> "❌"
        else                   -> "📋"
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFF0C1A28),
        border = androidx.compose.foundation.BorderStroke(
            1.5.dp, statusColor.copy(0.35f)
        )
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // ── Header ────────────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Status emoji circle
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(statusColor.copy(0.12f), CircleShape)
                            .border(1.dp, statusColor.copy(0.3f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(statusEmoji, fontSize = 20.sp)
                    }
                    Column {
                        Text(
                            "ROOM ${order.roomNumber.ifBlank { "—" }}",
                            fontWeight = FontWeight.ExtraBold,
                            color = GoldPrimary,
                            fontSize = 18.sp
                        )
                        Text(
                            order.guestName.ifBlank { "Guest" },
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceDark.copy(0.5f)
                        )
                    }
                }

                // Status badge
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = statusColor.copy(0.15f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp, statusColor.copy(0.4f)
                    )
                ) {
                    Text(
                        order.status.displayName.uppercase(),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 14.dp),
                color = Color.White.copy(0.06f)
            )

            // ── Items ─────────────────────────────────────────────────────────
            order.items.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(GoldPrimary.copy(0.12f), RoundedCornerShape(6.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "×${item.quantity}",
                                fontWeight = FontWeight.ExtraBold,
                                color = GoldPrimary,
                                fontSize = 11.sp
                            )
                        }
                        Text(
                            item.menuItemName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = OnSurfaceDark
                        )
                    }
                    Text(
                        "ETB ${item.subtotal.toInt()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = GoldPrimary.copy(0.7f),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Special instructions
            if (order.specialInstructions.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = WarningAmber.copy(0.07f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp, WarningAmber.copy(0.2f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            Icons.Default.Info, null,
                            tint = WarningAmber,
                            modifier = Modifier.size(14.dp).padding(top = 2.dp)
                        )
                        Text(
                            order.specialInstructions,
                            style = MaterialTheme.typography.bodySmall,
                            color = WarningAmber.copy(0.85f)
                        )
                    }
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = Color.White.copy(0.06f)
            )

            // ── Total + Action ────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Total",
                        style = MaterialTheme.typography.labelSmall,
                        color = OnSurfaceDark.copy(0.4f)
                    )
                    Text(
                        "ETB ${order.totalAmount.toInt()}",
                        fontWeight = FontWeight.ExtraBold,
                        color = GoldPrimary,
                        fontSize = 18.sp
                    )
                }

                // Action button based on current status
                when (order.status) {
                    OrderStatus.RECEIVED -> {
                        Button(
                            onClick = { onStatusUpdate(OrderStatus.PREPARING) },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = WarningAmber,
                                contentColor = Color.Black
                            )
                        ) {
                            Icon(Icons.Default.Restaurant, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Start Preparing", fontWeight = FontWeight.Bold)
                        }
                    }
                    OrderStatus.PREPARING -> {
                        Button(
                            onClick = { onStatusUpdate(OrderStatus.ON_THE_WAY) },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = InfoBlue,
                                contentColor = Color.White
                            )
                        ) {
                            Icon(Icons.Default.DeliveryDining, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Send to Room", fontWeight = FontWeight.Bold)
                        }
                    }
                    OrderStatus.ON_THE_WAY -> {
                        Button(
                            onClick = { onStatusUpdate(OrderStatus.DELIVERED) },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SuccessGreen,
                                contentColor = Color.White
                            )
                        ) {
                            Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Mark Delivered", fontWeight = FontWeight.Bold)
                        }
                    }
                    OrderStatus.DELIVERED -> {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = SuccessGreen.copy(0.1f),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp, SuccessGreen.copy(0.3f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle, null,
                                    tint = SuccessGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    "Delivered",
                                    color = SuccessGreen,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    else -> {}
                }
            }
        }
    }
}

// Keep old name for backward compat
@Composable
fun OrderAdminCard(order: Order, onStatusUpdate: (OrderStatus) -> Unit) =
    KitchenOrderCard(order = order, onStatusUpdate = onStatusUpdate)
