package com.gohahotel.connect.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
                title = { Text("Active Orders") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            Text("Pending Food & Service Requests", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(16.dp))
            
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(orders.filter { it.status != OrderStatus.DELIVERED && it.status != OrderStatus.CANCELLED }) { order ->
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

@Composable
fun OrderAdminCard(order: Order, onStatusUpdate: (OrderStatus) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardDark)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text("Room ${order.roomNumber}", fontWeight = FontWeight.Bold, color = GoldPrimary)
                Text(order.status.displayName, color = GoldLight)
            }
            Text("Guest: ${order.guestName}", style = MaterialTheme.typography.bodySmall, color = OnSurfaceDark.copy(alpha = 0.6f))
            HorizontalDivider(Modifier.padding(vertical = 8.dp), color = Color.White.copy(0.1f))
            
            order.items.forEach { item ->
                Text("• ${item.quantity}x ${item.menuItemName}", style = MaterialTheme.typography.bodyMedium, color = OnSurfaceDark)
            }
            
            Row(modifier = Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (order.status == OrderStatus.RECEIVED) {
                    Button(onClick = { onStatusUpdate(OrderStatus.PREPARING) }, modifier = Modifier.weight(1f)) {
                        Text("Prepare")
                    }
                }
                if (order.status == OrderStatus.PREPARING || order.status == OrderStatus.RECEIVED) {
                    Button(onClick = { onStatusUpdate(OrderStatus.ON_THE_WAY) }, modifier = Modifier.weight(1f)) {
                        Text("Send")
                    }
                }
                if (order.status == OrderStatus.ON_THE_WAY) {
                    Button(
                        onClick = { onStatusUpdate(OrderStatus.DELIVERED) }, 
                        modifier = Modifier.weight(1f), 
                        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                    ) {
                        Text("Done")
                    }
                }
            }
        }
    }
}
