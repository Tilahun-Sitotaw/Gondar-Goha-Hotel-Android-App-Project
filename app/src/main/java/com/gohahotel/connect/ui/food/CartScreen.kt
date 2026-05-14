package com.gohahotel.connect.ui.food

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.gohahotel.connect.ui.payment.PaymentSelectionSheet
import com.gohahotel.connect.ui.payment.PaymentViewModel
import com.gohahotel.connect.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    viewModel: FoodViewModel = hiltViewModel(),
    paymentViewModel: PaymentViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onOrderPlaced: (String) -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val uiState       by viewModel.uiState.collectAsState()
    val paymentUiState by paymentViewModel.uiState.collectAsState()

    var instructions     by remember { mutableStateOf("") }
    var roomNumber       by remember { mutableStateOf("") }
    var deliveryType     by remember { mutableStateOf(DeliveryType.ROOM) }
    var showPaymentSheet by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.activeOrderId) {
        uiState.activeOrderId?.let { onOrderPlaced(it) }
    }

    LaunchedEffect(paymentUiState.paymentSuccess) {
        if (paymentUiState.paymentSuccess) {
            showPaymentSheet = false
            viewModel.placeOrder(
                roomNumber   = when (deliveryType) {
                    DeliveryType.ROOM    -> roomNumber.ifBlank { "Unknown" }
                    DeliveryType.LOBBY   -> "Lobby"
                    DeliveryType.POOLSIDE -> "Pool Area"
                    DeliveryType.TERRACE  -> "Terrace"
                },
                instructions = instructions,
                deliveryLocation = deliveryType.label,
                paymentMethod = paymentUiState.selectedMethod?.displayName ?: "Unknown",
                paymentTransactionId = paymentUiState.transactionId ?: ""
            )
            paymentViewModel.resetState()
        }
    }

    Scaffold(
        containerColor = Color(0xFF050D18),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Your Order",
                            fontWeight = FontWeight.ExtraBold,
                            color = OnSurfaceDark, fontSize = 18.sp)
                        if (uiState.cartItems.isNotEmpty()) {
                            Text("${viewModel.cartCount} items · ETB ${viewModel.cartTotal.toInt()}",
                                style = MaterialTheme.typography.labelSmall,
                                color = GoldPrimary.copy(0.8f))
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = GoldPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF050D18))
            )
        },
        bottomBar = {
            if (uiState.cartItems.isNotEmpty()) {
                Surface(
                    color = Color(0xFF0A1424),
                    border = BorderStroke(1.dp, GoldPrimary.copy(0.2f)),
                    shape  = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        // Order summary row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Order Total",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = OnSurfaceDark.copy(0.5f))
                                Text("ETB ${viewModel.cartTotal.toInt()}",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold, color = GoldPrimary)
                            }
                            // Delivery type badge
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = TealPrimary.copy(0.15f),
                                border = BorderStroke(1.dp, TealPrimary.copy(0.3f))
                            ) {
                                Text("${deliveryType.emoji} ${deliveryType.label}",
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TealLight, fontWeight = FontWeight.Bold)
                            }
                        }

                        if (uiState.isLoading) {
                            LinearProgressIndicator(
                                modifier = Modifier.fillMaxWidth(),
                                color = GoldPrimary, trackColor = GoldPrimary.copy(0.15f))
                        }

                        Button(
                            onClick = {
                                if (viewModel.isGuest) onNavigateToLogin()
                                else showPaymentSheet = true
                            },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape  = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                            enabled = !uiState.isLoading
                        ) {
                            Icon(Icons.Default.Payment, null,
                                tint = Color(0xFF050D18), modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Place Order · ETB ${viewModel.cartTotal.toInt()}",
                                color = Color(0xFF050D18), fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            }
        }
    ) { padding ->
        if (uiState.cartItems.isEmpty()) {
            // Empty cart
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("🛒", fontSize = 56.sp)
                    Text("Your cart is empty",
                        style = MaterialTheme.typography.titleMedium,
                        color = OnSurfaceDark.copy(0.5f),
                        fontWeight = FontWeight.Bold)
                    Text("Add dishes from the menu to get started",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceDark.copy(0.3f),
                        textAlign = TextAlign.Center)
                    Spacer(Modifier.height(4.dp))
                    OutlinedButton(
                        onClick = onBack,
                        border = BorderStroke(1.dp, GoldPrimary.copy(0.5f)),
                        shape  = RoundedCornerShape(50)
                    ) {
                        Icon(Icons.Default.Restaurant, null,
                            tint = GoldPrimary, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Browse Menu", color = GoldPrimary)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(Brush.verticalGradient(
                        listOf(Color(0xFF050D18), Color(0xFF040C14))
                    )),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // ── Cart items ────────────────────────────────────────────────
                item {
                    Text("Order Items",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = OnSurfaceDark)
                }

                items(uiState.cartItems, key = { it.menuItem.id }) { cart ->
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFF0E1B2A),
                        border = BorderStroke(1.dp, Color.White.copy(0.06f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Dish image
                            Box(
                                modifier = Modifier.size(56.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF0A1424))
                            ) {
                                val imageUrl = cart.menuItem.allImages.firstOrNull() ?: ""
                                if (imageUrl.isNotBlank()) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(imageUrl).crossfade(true).build(),
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Text("🍽️", fontSize = 20.sp)
                                    }
                                }
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(cart.menuItem.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = OnSurfaceDark)
                                Text("ETB ${cart.menuItem.price.toInt()} each",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = OnSurfaceDark.copy(0.5f))
                                if (cart.customization.isNotBlank()) {
                                    Text("📝 ${cart.customization}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = GoldPrimary.copy(0.7f))
                                }
                            }

                            // Qty controls
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                IconButton(
                                    onClick = { viewModel.removeFromCart(cart.menuItem) },
                                    modifier = Modifier.size(28.dp)
                                        .background(Color.White.copy(0.08f), CircleShape)
                                ) {
                                    Icon(Icons.Default.Remove, null,
                                        tint = GoldPrimary, modifier = Modifier.size(13.dp))
                                }
                                Text("${cart.quantity}",
                                    fontWeight = FontWeight.ExtraBold,
                                    color = GoldPrimary, fontSize = 14.sp)
                                IconButton(
                                    onClick = { viewModel.addToCart(cart.menuItem) },
                                    modifier = Modifier.size(28.dp)
                                        .background(GoldPrimary, CircleShape)
                                ) {
                                    Icon(Icons.Default.Add, null,
                                        tint = Color(0xFF050D18), modifier = Modifier.size(13.dp))
                                }
                            }

                            // Subtotal
                            Text("ETB ${(cart.menuItem.price * cart.quantity).toInt()}",
                                fontWeight = FontWeight.ExtraBold,
                                color = GoldPrimary,
                                style = MaterialTheme.typography.titleSmall)
                        }
                    }
                }

                // ── Delivery type selector ────────────────────────────────────
                item {
                    Spacer(Modifier.height(4.dp))
                    Text("Delivery Location",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = OnSurfaceDark)
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DeliveryType.entries.forEach { type ->
                            val selected = deliveryType == type
                            Surface(
                                onClick = { deliveryType = type },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                color = if (selected) TealPrimary.copy(0.2f) else Color(0xFF0E1B2A),
                                border = BorderStroke(
                                    1.dp,
                                    if (selected) TealPrimary else Color.White.copy(0.08f)
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(type.emoji, fontSize = 18.sp)
                                    Spacer(Modifier.height(3.dp))
                                    Text(type.label,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (selected) TealLight else OnSurfaceDark.copy(0.5f),
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                        textAlign = TextAlign.Center)
                                }
                            }
                        }
                    }
                }

                // Room number field (only for room delivery)
                if (deliveryType == DeliveryType.ROOM) {
                    item {
                        OutlinedTextField(
                            value = roomNumber,
                            onValueChange = { roomNumber = it },
                            label = { Text("Room Number") },
                            leadingIcon = {
                                Icon(Icons.Default.Hotel, null,
                                    tint = GoldPrimary.copy(0.6f), modifier = Modifier.size(18.dp))
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor   = GoldPrimary,
                                unfocusedBorderColor = Color.White.copy(0.1f),
                                focusedTextColor     = OnSurfaceDark,
                                unfocusedTextColor   = OnSurfaceDark,
                                focusedLabelColor    = GoldPrimary,
                                cursorColor          = GoldPrimary,
                                focusedContainerColor   = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            )
                        )
                    }
                }

                // ── Special instructions ──────────────────────────────────────
                item {
                    OutlinedTextField(
                        value = instructions,
                        onValueChange = { instructions = it },
                        label = { Text("Special Instructions (optional)") },
                        leadingIcon = {
                            Icon(Icons.Default.Notes, null,
                                tint = GoldPrimary.copy(0.6f), modifier = Modifier.size(18.dp))
                        },
                        placeholder = { Text("Allergies, preferences, extra sauce...",
                            color = OnSurfaceDark.copy(0.3f)) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2, maxLines = 3,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor   = GoldPrimary,
                            unfocusedBorderColor = Color.White.copy(0.1f),
                            focusedTextColor     = OnSurfaceDark,
                            unfocusedTextColor   = OnSurfaceDark,
                            focusedLabelColor    = GoldPrimary,
                            cursorColor          = GoldPrimary,
                            focusedContainerColor   = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        )
                    )
                }

                // ── Order summary card ────────────────────────────────────────
                item {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFF0E1B2A),
                        border = BorderStroke(1.dp, GoldPrimary.copy(0.15f))
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("Order Summary",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold, color = OnSurfaceDark)
                            HorizontalDivider(color = Color.White.copy(0.06f))
                            uiState.cartItems.forEach { cart ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("${cart.quantity}× ${cart.menuItem.name}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = OnSurfaceDark.copy(0.7f),
                                        modifier = Modifier.weight(1f))
                                    Text("ETB ${(cart.menuItem.price * cart.quantity).toInt()}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = OnSurfaceDark.copy(0.7f))
                                }
                            }
                            HorizontalDivider(color = Color.White.copy(0.06f))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Total",
                                    fontWeight = FontWeight.ExtraBold, color = OnSurfaceDark)
                                Text("ETB ${viewModel.cartTotal.toInt()}",
                                    fontWeight = FontWeight.ExtraBold, color = GoldPrimary)
                            }
                        }
                    }
                }

                // Clear cart
                item {
                    TextButton(
                        onClick = viewModel::clearCart,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.DeleteOutline, null,
                            tint = ErrorRed.copy(0.7f), modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Clear Cart", color = ErrorRed.copy(0.7f))
                    }
                }

                item { Spacer(Modifier.height(100.dp)) }
            }
        }
    }

    // Payment sheet
    if (showPaymentSheet) {
        ModalBottomSheet(
            onDismissRequest = { showPaymentSheet = false },
            containerColor   = Color(0xFF0A1424),
            shape            = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            PaymentSelectionSheet(
                selectedMethod   = paymentUiState.selectedMethod,
                onMethodSelected = { paymentViewModel.selectMethod(it) },
                amount           = viewModel.cartTotal,
                currency         = "ETB",
                onConfirm = {
                    paymentViewModel.processPayment(
                        amount      = viewModel.cartTotal,
                        referenceId = "FOOD-ORDER"
                    )
                },
                isProcessing = paymentUiState.isProcessing
            )
        }
    }
}

// ── Delivery type enum ────────────────────────────────────────────────────────
enum class DeliveryType(val label: String, val emoji: String) {
    ROOM("Room",     "🛏️"),
    LOBBY("Lobby",   "🏨"),
    POOLSIDE("Pool", "🏊"),
    TERRACE("Roof",  "🌄")
}
