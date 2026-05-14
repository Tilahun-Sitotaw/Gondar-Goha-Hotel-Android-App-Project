package com.gohahotel.connect.ui.food

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gohahotel.connect.domain.model.Order
import com.gohahotel.connect.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuestOrdersScreen(
    viewModel: FoodViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onNavigateToOrder: (String) -> Unit
) {
    // Collect active orders from the repository (handled directly or via a new state flow)
    // Since FoodViewModel doesn't directly expose getActiveOrders yet, we will just observe
    // a quick flow from the repository.
    
    // For now, let's use the local state if available. We will need to update FoodViewModel to expose `userOrders`.
    val uiState by viewModel.uiState.collectAsState()
    
    // We assume FoodViewModel is updated to provide userOrders
    val orders = uiState.userOrders

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Orders", fontWeight = FontWeight.Bold, color = OnSurfaceDark) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back", tint = GoldPrimary) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceDark)
            )
        },
        containerColor = SurfaceDark
    ) { padding ->
        when {
            uiState.isLoading && orders.isEmpty() -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GoldPrimary)
                }
            }
            orders.isEmpty() -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Icon(Icons.Default.Receipt, null,
                            Modifier.size(72.dp), tint = GoldPrimary.copy(alpha = 0.2f))
                        Text("No orders yet",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White.copy(0.6f),
                            fontWeight = FontWeight.Bold)
                        Text("Your food orders will appear here once you place one.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(0.35f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        uiState.error?.let {
                            Text("⚠️ $it",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFE24A4A).copy(0.8f),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        }
                    }
                }
            }
            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(padding)
                ) {
                    items(orders.sortedByDescending { it.createdAt }, key = { it.id }) { order ->
                        GuestOrderCard(order = order, onClick = { onNavigateToOrder(order.id) })
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GuestOrderCard(order: Order, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.1f))
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Order #${order.id.takeLast(6).uppercase()}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = GoldPrimary
                )
                Surface(
                    shape = RoundedCornerShape(50),
                    color = when (order.status.name) {
                        "RECEIVED" -> StatusReceived.copy(alpha = 0.2f)
                        "PREPARING" -> StatusPreparing.copy(alpha = 0.2f)
                        "ON_THE_WAY" -> StatusOnWay.copy(alpha = 0.2f)
                        "DELIVERED" -> StatusDelivered.copy(alpha = 0.2f)
                        else -> Color.Gray.copy(alpha = 0.2f)
                    }
                ) {
                    Text(
                        order.status.userFriendlyName,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = when (order.status.name) {
                            "RECEIVED" -> StatusReceived
                            "PREPARING" -> StatusPreparing
                            "ON_THE_WAY" -> StatusOnWay
                            "DELIVERED" -> StatusDelivered
                            else -> Color.Gray
                        },
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            
            Text("${order.items.size} items", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(0.7f))
            Text(order.items.joinToString { "${it.quantity}x ${it.menuItemName}" },
                style = MaterialTheme.typography.labelSmall, color = Color.White.copy(0.5f), maxLines = 1)
                
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault()).format(order.createdAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(0.4f)
                )
                Text(
                    "ETB ${order.totalAmount.toInt()}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}
