package com.gohahotel.connect.ui.food

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gohahotel.connect.ui.payment.PaymentSelectionSheet
import com.gohahotel.connect.ui.payment.PaymentViewModel
import com.gohahotel.connect.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    viewModel: FoodViewModel = hiltViewModel(),
    paymentViewModel: PaymentViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onOrderPlaced: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val paymentUiState by paymentViewModel.uiState.collectAsState()
    
    var instructions by remember { mutableStateOf("") }
    var roomNumber   by remember { mutableStateOf("") }
    var showPaymentSheet by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.activeOrderId) {
        uiState.activeOrderId?.let { onOrderPlaced(it) }
    }
    
    LaunchedEffect(paymentUiState.paymentSuccess) {
        if (paymentUiState.paymentSuccess) {
            showPaymentSheet = false
            viewModel.placeOrder(
                roomNumber = roomNumber.ifBlank { "Lobby" },
                guestId = "guest",
                guestName = "Valued Guest",
                instructions = instructions
            )
            paymentViewModel.resetState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Your Cart", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        bottomBar = {
            if (uiState.cartItems.isNotEmpty()) {
                Surface(shadowElevation = 8.dp) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("ETB ${viewModel.cartTotal.toInt()}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold, color = GoldPrimary)
                        }
                        if (uiState.isLoading) LinearProgressIndicator(Modifier.fillMaxWidth(), color = GoldPrimary)
                        Button(
                            onClick = { showPaymentSheet = true },
                            modifier = Modifier.fillMaxWidth().height(54.dp),
                            shape    = RoundedCornerShape(14.dp),
                            colors   = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                            enabled  = !uiState.isLoading
                        ) {
                            Icon(Icons.Default.RestaurantMenu, null, tint = SurfaceDark, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Checkout · ETB ${viewModel.cartTotal.toInt()}",
                                color = SurfaceDark, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    ) { padding ->
        if (uiState.cartItems.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Default.ShoppingCart, null,
                        Modifier.size(72.dp), tint = GoldPrimary.copy(0.3f))
                    Text("Your cart is empty", style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(0.5f))
                    OutlinedButton(onClick = onBack,
                        border = BorderStroke(1.dp, GoldPrimary), shape = RoundedCornerShape(50)) {
                        Text("Browse Menu", color = GoldPrimary)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.cartItems, key = { it.menuItem.id }) { cart ->
                    Card(shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CardDark)) {
                        Row(
                            Modifier.padding(12.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(cart.menuItem.name, style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold)
                                Text("ETB ${cart.menuItem.price.toInt()} each",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(0.6f))
                                if (cart.customization.isNotBlank()) {
                                    Text("Note: ${cart.customization}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = GoldPrimary.copy(0.8f))
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                IconButton(onClick = { viewModel.removeFromCart(cart.menuItem) },
                                    modifier = Modifier.size(28.dp)) {
                                    Icon(Icons.Default.Remove, null, Modifier.size(16.dp), tint = GoldPrimary)
                                }
                                Text("${cart.quantity}", fontWeight = FontWeight.Bold, color = GoldPrimary)
                                IconButton(onClick = { viewModel.addToCart(cart.menuItem) },
                                    modifier = Modifier.size(28.dp)) {
                                    Icon(Icons.Default.Add, null, Modifier.size(16.dp), tint = GoldPrimary)
                                }
                            }
                            Text("ETB ${(cart.menuItem.price * cart.quantity).toInt()}",
                                fontWeight = FontWeight.Bold, color = GoldPrimary,
                                style = MaterialTheme.typography.titleSmall)
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = roomNumber, onValueChange = { roomNumber = it },
                        label = { Text("Room Number") }, modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Hotel, null) },
                        singleLine = true, shape = RoundedCornerShape(14.dp))
                }
                item {
                    OutlinedTextField(
                        value = instructions, onValueChange = { instructions = it },
                        label = { Text("Special Instructions (optional)") },
                        modifier = Modifier.fillMaxWidth().height(90.dp),
                        maxLines = 3, shape = RoundedCornerShape(14.dp))
                }
                item {
                    TextButton(onClick = viewModel::clearCart, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.Delete, null, tint = ErrorRed, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Clear Cart", color = ErrorRed)
                    }
                }
            }
        }
    }

    if (showPaymentSheet) {
        ModalBottomSheet(
            onDismissRequest = { showPaymentSheet = false },
            containerColor = SurfaceDark
        ) {
            PaymentSelectionSheet(
                selectedMethod = paymentUiState.selectedMethod,
                onMethodSelected = { paymentViewModel.selectMethod(it) },
                onConfirm = {
                    paymentViewModel.processPayment(
                        amount = viewModel.cartTotal,
                        referenceId = "FOOD-ORDER"
                    )
                },
                isProcessing = paymentUiState.isProcessing
            )
        }
    }
}
