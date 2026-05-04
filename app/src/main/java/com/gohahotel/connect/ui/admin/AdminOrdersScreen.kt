package com.gohahotel.connect.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.RoomService
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gohahotel.connect.domain.model.Order
import com.gohahotel.connect.domain.model.OrderStatus
import com.gohahotel.connect.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminOrdersScreen(
    onBack: () -> Unit,
    viewModel: AdminViewModel = hiltViewModel()
) {
    val orders by viewModel.orders.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Service Requests", fontWeight = FontWeight.Bold)
                        Text("Live Order Tracking", style = MaterialTheme.typography.labelSmall, color = GoldPrimary.copy(alpha = 0.6f))
                    }
                },
                navigationIcon = { 
                    IconButton(onClick = onBack) { 
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = GoldPrimary) 
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
                .padding(padding)
        ) {
            val activeOrders = orders.filter { it.status != OrderStatus.DELIVERED && it.status != OrderStatus.CANCELLED }
            
            if (activeOrders.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.RoomService, null, modifier = Modifier.size(64.dp), tint = GoldPrimary.copy(alpha = 0.1f))
                        Text("All clear! No pending orders.", color = OnSurfaceDark.copy(alpha = 0.3f), style = MaterialTheme.typography.bodyLarge)
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(activeOrders) { order ->
                        OrderAdminCard(
                            order = order,
                            onStatusUpdate = { newStatus ->
                                viewModel.updateOrderStatus(order.id, newStatus)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OrderAdminCard(order: Order, onStatusUpdate: (OrderStatus) -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = CardDark.copy(alpha = 0.7f),
        border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween, 
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("ROOM ${order.roomNumber}", fontWeight = FontWeight.ExtraBold, color = GoldPrimary, fontSize = 20.sp)
                    Text("Guest: ${order.guestName}", style = MaterialTheme.typography.bodySmall, color = OnSurfaceDark.copy(alpha = 0.5f))
                }
                
                Surface(
                    color = when(order.status) {
                        OrderStatus.RECEIVED -> ErrorRed.copy(0.1f)
                        OrderStatus.PREPARING -> WarningAmber.copy(0.1f)
                        else -> InfoBlue.copy(0.1f)
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        order.status.displayName.uppercase(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = when(order.status) {
                            OrderStatus.RECEIVED -> ErrorRed
                            OrderStatus.PREPARING -> WarningAmber
                            else -> InfoBlue
                        },
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            HorizontalDivider(Modifier.padding(vertical = 16.dp), color = Color.White.copy(0.05f))
            
            order.items.forEach { item ->
                Row(modifier = Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(GoldPrimary.copy(alpha = 0.1f), RoundedCornerShape(4.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("${item.quantity}", fontWeight = FontWeight.Bold, color = GoldPrimary, fontSize = 12.sp)
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(item.menuItemName, style = MaterialTheme.typography.bodyMedium, color = OnSurfaceDark)
                }
            }
            
            Spacer(Modifier.height(20.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                when (order.status) {
                    OrderStatus.RECEIVED -> {
                        Button(
                            onClick = { onStatusUpdate(OrderStatus.PREPARING) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = SurfaceDark)
                        ) {
                            Text("Start Preparing", fontWeight = FontWeight.Bold)
                        }
                    }
                    OrderStatus.PREPARING -> {
                        Button(
                            onClick = { onStatusUpdate(OrderStatus.ON_THE_WAY) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = InfoBlue)
                        ) {
                            Text("Ready for Delivery", fontWeight = FontWeight.Bold)
                        }
                    }
                    OrderStatus.ON_THE_WAY -> {
                        Button(
                            onClick = { onStatusUpdate(OrderStatus.DELIVERED) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                        ) {
                            Text("Mark as Delivered", fontWeight = FontWeight.Bold)
                        }
                    }
                    else -> {}
                }
            }
        }
    }
}
