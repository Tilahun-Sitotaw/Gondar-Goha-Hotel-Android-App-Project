package com.gohahotel.connect.ui.food

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.verticalScroll
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
import com.gohahotel.connect.domain.model.OrderStatus
import com.gohahotel.connect.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderTrackingScreen(
    orderId: String,
    viewModel: FoodViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(orderId) { viewModel.observeOrder(orderId) }

    val order = uiState.activeOrder
    val steps = listOf(
        OrderStatus.RECEIVED,
        OrderStatus.PREPARING,
        OrderStatus.ON_THE_WAY,
        OrderStatus.DELIVERED
    )
    val currentStep = order?.status?.step ?: 0

    // Pulsing animation for active step
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "pulseAlpha"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Order Tracking", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Big status emoji
            val emoji = when (order?.status) {
                OrderStatus.RECEIVED  -> "📋"
                OrderStatus.PREPARING -> "👨‍🍳"
                OrderStatus.ON_THE_WAY -> "🛎️"
                OrderStatus.DELIVERED -> "✅"
                else -> "📋"
            }
            Text(emoji, fontSize = 72.sp)

            Text(
                order?.status?.userFriendlyName ?: "Loading...",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = when (order?.status) {
                    OrderStatus.RECEIVED  -> StatusReceived
                    OrderStatus.PREPARING -> StatusPreparing
                    OrderStatus.ON_THE_WAY -> StatusOnWay
                    OrderStatus.DELIVERED -> StatusDelivered
                    else -> Color.White
                }
            )

            if (order?.status != OrderStatus.DELIVERED) {
                Text(
                    "Estimated: ~${order?.estimatedDeliveryMinutes ?: 30} minutes",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(0.6f)
                )
            }

            // Progress stepper
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(20.dp),
                colors   = CardDefaults.cardColors(containerColor = CardDark)
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(0.dp)) {
                    steps.forEachIndexed { index, step ->
                        val isDone   = currentStep > step.step
                        val isActive = currentStep == step.step
                        Row(
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Step indicator column
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier.size(36.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = when {
                                            isDone   -> SuccessGreen
                                            isActive -> GoldPrimary.copy(alpha = pulseAlpha)
                                            else     -> MaterialTheme.colorScheme.outline.copy(0.2f)
                                        },
                                        modifier = Modifier.size(36.dp)
                                    ) {}
                                    Icon(
                                        if (isDone) Icons.Default.Check else
                                        when (step) {
                                            OrderStatus.RECEIVED   -> Icons.Default.Receipt
                                            OrderStatus.PREPARING  -> Icons.Default.Restaurant
                                            OrderStatus.ON_THE_WAY -> Icons.Default.DeliveryDining
                                            OrderStatus.DELIVERED  -> Icons.Default.CheckCircle
                                            else -> Icons.Default.Circle
                                        },
                                        contentDescription = step.displayName,
                                        tint = if (isDone || isActive) Color.White
                                               else Color.White.copy(0.3f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                if (index < steps.lastIndex) {
                                    Box(
                                        modifier = Modifier
                                            .width(2.dp)
                                            .height(32.dp)
                                            .padding(vertical = 2.dp),
                                    ) {
                                        Surface(
                                            modifier = Modifier.fillMaxSize(),
                                            color    = if (isDone) SuccessGreen else MaterialTheme.colorScheme.outline.copy(0.2f)
                                        ) {}
                                    }
                                }
                            }

                            Column(modifier = Modifier.padding(top = 6.dp)) {
                                Text(
                                    step.displayName,
                                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                                    color = when {
                                        isDone   -> SuccessGreen
                                        isActive -> GoldPrimary
                                        else     -> Color.White.copy(0.4f)
                                    }
                                )
                                Text(
                                    step.displayNameAmharic,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(0.4f)
                                )
                                if (index < steps.lastIndex) Spacer(Modifier.height(20.dp))
                            }
                        }
                    }
                }
            }

            // Order summary
            if (order != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(16.dp),
                    colors   = CardDefaults.cardColors(containerColor = CardDark)
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Order Summary", style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold)
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(0.2f))
                        order.items.forEach { item ->
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("${item.quantity}× ${item.menuItemName}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(0.8f))
                                Text("ETB ${item.subtotal.toInt()}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium)
                            }
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(0.2f))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total", fontWeight = FontWeight.Bold, color = Color.White)
                            Text("ETB ${order.totalAmount.toInt()}",
                                fontWeight = FontWeight.Bold, color = GoldPrimary)
                        }
                    }
                }
            }

            if (order?.status == OrderStatus.DELIVERED) {
                Button(
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape  = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                ) {
                    Text("Back to Menu", color = SurfaceDark, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
