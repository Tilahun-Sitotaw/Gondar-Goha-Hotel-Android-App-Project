package com.gohahotel.connect.ui.rooms

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.gohahotel.connect.domain.model.*
import com.gohahotel.connect.ui.payment.PaymentSelectionSheet
import com.gohahotel.connect.ui.payment.PaymentViewModel
import coil.compose.AsyncImage
import com.gohahotel.connect.ui.theme.*
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomDetailScreen(
    roomId: String,
    viewModel: RoomViewModel = hiltViewModel(),
    paymentViewModel: PaymentViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onInRoomRequest: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val paymentUiState by paymentViewModel.uiState.collectAsState()
    val room = uiState.selectedRoom

    LaunchedEffect(roomId) { viewModel.loadRoomDetail(roomId) }

    var showBookingDialog by remember { mutableStateOf(false) }
    var checkIn  by remember { mutableStateOf("") }
    var checkOut by remember { mutableStateOf("") }
    var guests   by remember { mutableStateOf("1") }
    var special  by remember { mutableStateOf("") }
    var currentImageIndex by remember { mutableIntStateOf(0) }

    var showPaymentSheet by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isBookingSuccess) {
        if (uiState.isBookingSuccess) {
            showBookingDialog = false
            viewModel.resetBookingState()
        }
    }

    LaunchedEffect(paymentUiState.paymentSuccess) {
        if (paymentUiState.paymentSuccess) {
            showPaymentSheet = false
            room?.let {
                viewModel.bookRoom(
                    guestId = "guest", guestName = "Guest",
                    guestEmail = "", roomId = it.id,
                    roomName = it.name, roomType = it.type.name,
                    checkIn = checkIn, checkOut = checkOut,
                    nights = 1, guests = guests.toIntOrNull() ?: 1,
                    totalPrice = it.pricePerNight,
                    specialRequests = special
                )
            }
            paymentViewModel.resetState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(room?.name ?: "Room Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            if (room != null) {
                Surface(shadowElevation = 8.dp) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onInRoomRequest,
                            modifier = Modifier.weight(1f).height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, GoldPrimary)
                        ) {
                            Icon(Icons.Default.RoomService, null,
                                tint = GoldPrimary, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Request", color = GoldPrimary)
                        }
                        Button(
                            onClick = { showBookingDialog = true },
                            modifier = Modifier.weight(2f).height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                            enabled = room.isAvailable
                        ) {
                            Icon(Icons.Default.BookOnline, null,
                                tint = SurfaceDark, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Book Now · ${room.currency} ${room.pricePerNight.toInt()}/night",
                                color = SurfaceDark, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    ) { padding ->
        if (room == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = GoldPrimary)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
            ) {
                // Image gallery
                Box(
                    modifier = Modifier.fillMaxWidth().height(280.dp)
                        .background(Brush.verticalGradient(listOf(TealDark, SurfaceVariantDark)))
                ) {
                    if (room.imageUrls.isNotEmpty()) {
                        AsyncImage(
                            model = room.imageUrls.getOrNull(currentImageIndex) ?: room.imageUrls.first(),
                            contentDescription = room.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        // Image counter
                        Surface(
                            modifier = Modifier.align(Alignment.BottomEnd).padding(12.dp),
                            shape    = RoundedCornerShape(50),
                            color    = Color.Black.copy(0.5f)
                        ) {
                            Text(
                                "${currentImageIndex + 1}/${room.imageUrls.size}",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                color    = Color.White,
                                style    = MaterialTheme.typography.labelSmall
                            )
                        }
                    } else {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Hotel, null,
                                Modifier.size(80.dp), tint = GoldPrimary.copy(0.4f))
                        }
                    }
                }

                Column(Modifier.padding(20.dp)) {
                    // Header
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.Top
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(room.name, style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold)
                            Text("${room.type.displayName} · Floor ${room.floorNumber}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(0.6f))
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("${room.currency} ${room.pricePerNight.toInt()}",
                                style = MaterialTheme.typography.headlineSmall,
                                color = GoldPrimary, fontWeight = FontWeight.Bold)
                            Text("per night", style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.Star, null, Modifier.size(16.dp), tint = GoldPrimary)
                            Text("${room.rating} (${room.reviewCount} reviews)",
                                style = MaterialTheme.typography.bodySmall, color = GoldPrimary)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.People, null, Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurface.copy(0.5f))
                            Text("Up to ${room.capacity} guests",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(0.3f))
                    Spacer(Modifier.height(16.dp))

                    Text("About this Room", style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    Text(room.description.ifBlank { "Enjoy a luxurious stay in one of Goha Hotel's premium rooms, perched on the hilltop of Gondar with panoramic city views." },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(0.75f))

                    Spacer(Modifier.height(16.dp))
                    Text("Amenities", style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(10.dp))

                    room.amenities.chunked(2).forEach { row ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            row.forEach { amenity ->
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.CheckCircle, null,
                                        Modifier.size(14.dp), tint = SuccessGreen)
                                    Text(amenity, style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(0.8f))
                                }
                            }
                            if (row.size == 1) Spacer(Modifier.weight(1f))
                        }
                        Spacer(Modifier.height(6.dp))
                    }

                    if (room.hasView || room.hasMountainView) {
                        Spacer(Modifier.height(12.dp))
                        Card(
                            shape  = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = GoldDark.copy(0.15f))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("🌄", style = MaterialTheme.typography.titleMedium)
                                Text(
                                    if (room.hasMountainView) "Panoramic mountain & city views from your room"
                                    else "Beautiful scenic views from your room",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = GoldLight
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(20.dp))
                }
            }
        }
    }

    // Booking dialog
    if (showBookingDialog && room != null) {
        AlertDialog(
            onDismissRequest = { showBookingDialog = false },
            title = { Text("Book ${room.name}", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(checkIn, { checkIn = it },
                        label = { Text("Check-In Date (YYYY-MM-DD)") },
                        modifier = Modifier.fillMaxWidth(), singleLine = true,
                        shape = RoundedCornerShape(12.dp))
                    OutlinedTextField(checkOut, { checkOut = it },
                        label = { Text("Check-Out Date (YYYY-MM-DD)") },
                        modifier = Modifier.fillMaxWidth(), singleLine = true,
                        shape = RoundedCornerShape(12.dp))
                    OutlinedTextField(guests, { guests = it },
                        label = { Text("Number of Guests") },
                        modifier = Modifier.fillMaxWidth(), singleLine = true,
                        shape = RoundedCornerShape(12.dp))
                    OutlinedTextField(special, { special = it },
                        label = { Text("Special Requests (optional)") },
                        modifier = Modifier.fillMaxWidth(), maxLines = 3,
                        shape = RoundedCornerShape(12.dp))
                    if (uiState.isLoading) LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = GoldPrimary
                    )
                    if (uiState.isBookingSuccess) Text("✅ Booking Confirmed!",
                        color = SuccessGreen, fontWeight = FontWeight.Bold)
                }
            },
            confirmButton = {
                Button(
                    onClick = { showPaymentSheet = true },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                    shape  = RoundedCornerShape(12.dp)
                ) { Text("Proceed to Payment", color = SurfaceDark, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showBookingDialog = false }) { Text("Cancel") }
            },
            shape = RoundedCornerShape(20.dp)
        )
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
                        amount = room?.pricePerNight ?: 0.0,
                        referenceId = "ROOM-BOOKING"
                    )
                },
                isProcessing = paymentUiState.isProcessing
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RoomDetailPreview() {
    GohaHotelTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Text("Switch to Split/Design view to see the UI", 
                 modifier = Modifier.padding(32.dp))
        }
    }
}
