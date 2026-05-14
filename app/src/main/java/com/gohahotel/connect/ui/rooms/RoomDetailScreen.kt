package com.gohahotel.connect.ui.rooms

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.gohahotel.connect.domain.model.*
import com.gohahotel.connect.ui.payment.PaymentSelectionSheet
import com.gohahotel.connect.ui.payment.PaymentViewModel
import com.gohahotel.connect.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.shape.CircleShape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomDetailScreen(
    roomId: String,
    viewModel: RoomViewModel = hiltViewModel(),
    paymentViewModel: PaymentViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onInRoomRequest: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val paymentUiState by paymentViewModel.uiState.collectAsState()
    val room = uiState.selectedRoom

    LaunchedEffect(roomId) { viewModel.loadRoomDetail(roomId) }

    var showBookingDialog  by remember { mutableStateOf(false) }
    var currentImageIndex  by remember { mutableIntStateOf(0) }
    var showPaymentSheet   by remember { mutableStateOf(false) }
    var showSuccessDialog  by remember { mutableStateOf(false) }
    var bookingConfirmRef  by remember { mutableStateOf("") }
    var guestValidationError by remember { mutableStateOf("") }

    // Booking state — stored as millis for easy calculation
    var checkInMillis  by remember { mutableLongStateOf(
        Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }.timeInMillis
    )}
    var checkOutMillis by remember { mutableLongStateOf(
        Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 2) }.timeInMillis
    )}
    var checkInHour    by remember { mutableIntStateOf(14) }  // 2 PM default
    var checkInMinute  by remember { mutableIntStateOf(0) }
    var guests         by remember { mutableStateOf("1") }
    var special        by remember { mutableStateOf("") }

    // Derived display strings
    val dateFmt = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val displayFmt = remember { SimpleDateFormat("EEE, MMM d yyyy", Locale.getDefault()) }
    val checkInStr  = dateFmt.format(Date(checkInMillis))
    val checkOutStr = dateFmt.format(Date(checkOutMillis))
    val nights = maxOf(1, ((checkOutMillis - checkInMillis) / (1000 * 60 * 60 * 24)).toInt())

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
                val ref = "GH-${System.currentTimeMillis().toString().takeLast(6)}"
                bookingConfirmRef = ref
                viewModel.bookRoom(
                    roomId          = it.id,
                    roomName        = it.name,
                    roomType        = it.type.name,
                    checkIn         = checkInStr,
                    checkOut        = checkOutStr,
                    nights          = nights,
                    guests          = guests.toIntOrNull() ?: 1,
                    totalPrice      = it.pricePerNight * nights,
                    specialRequests = special,
                    referenceId     = ref,
                    currency        = it.currency
                )
                showSuccessDialog = true
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
                Surface(
                    color = MaterialTheme.colorScheme.background,
                    shadowElevation = 12.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        // Availability banner
                        if (!room.isAvailable) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = ErrorRed.copy(0.1f),
                                border = BorderStroke(1.dp, ErrorRed.copy(0.3f)),
                                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                            ) {
                                Text(
                                    "⚠️ This room is currently unavailable",
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ErrorRed
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = onInRoomRequest,
                                modifier = Modifier.weight(1f).height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, GoldPrimary),
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) {
                                Icon(Icons.Default.RoomService, null,
                                    tint = GoldPrimary, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    "Service",
                                    color = GoldPrimary,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                            }
                            Button(
                                onClick = {
                                    if (viewModel.isGuest) onNavigateToLogin()
                                    else showBookingDialog = true
                                },
                                modifier = Modifier.weight(2f).height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = GoldPrimary,
                                    disabledContainerColor = GoldPrimary.copy(0.3f)
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp),
                                enabled = room.isAvailable
                            ) {
                                Icon(Icons.Default.BookOnline, null,
                                    tint = SurfaceDark, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    "Book · ${room.currency} ${room.pricePerNight.toInt()}/night",
                                    color = SurfaceDark,
                                    fontWeight = FontWeight.ExtraBold,
                                    style = MaterialTheme.typography.labelLarge,
                                    maxLines = 1
                                )
                            }
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
                // Image gallery with swipe support
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .background(Brush.verticalGradient(listOf(TealDark, SurfaceVariantDark)))
                        .pointerInput(Unit) {
                            detectHorizontalDragGestures { change, dragAmount ->
                                change.consume()
                                when {
                                    dragAmount > 50 && currentImageIndex > 0 -> currentImageIndex--
                                    dragAmount < -50 && currentImageIndex < room.imageUrls.size - 1 -> currentImageIndex++
                                }
                            }
                        }
                ) {
                    if (room.imageUrls.isNotEmpty()) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(room.imageUrls.getOrNull(currentImageIndex) ?: room.imageUrls.first())
                                .crossfade(true)
                                .build(),
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
                        
                        // Navigation arrows
                        if (room.imageUrls.size > 1) {
                            // Left arrow
                            if (currentImageIndex > 0) {
                                IconButton(
                                    onClick = { currentImageIndex-- },
                                    modifier = Modifier
                                        .align(Alignment.CenterStart)
                                        .padding(12.dp)
                                        .background(Color.Black.copy(0.3f), CircleShape)
                                ) {
                                    Icon(Icons.Default.ChevronLeft, null, tint = Color.White)
                                }
                            }
                            // Right arrow
                            if (currentImageIndex < room.imageUrls.size - 1) {
                                IconButton(
                                    onClick = { currentImageIndex++ },
                                    modifier = Modifier
                                        .align(Alignment.CenterEnd)
                                        .padding(12.dp)
                                        .background(Color.Black.copy(0.3f), CircleShape)
                                ) {
                                    Icon(Icons.Default.ChevronRight, null, tint = Color.White)
                                }
                            }
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

                    Spacer(Modifier.height(12.dp))
                    // Room Status Badge with user-friendly label and date range
                    if (room != null) {
                        val statusColor = if (room.isAvailable) Color(0xFF4CAF50) else Color(0xFFFF9800)
                        val statusName = if (room.isAvailable) "Available Now" else "Reserved"
                        
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = statusColor.copy(0.15f),
                            border = BorderStroke(1.5.dp, statusColor.copy(0.4f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(statusColor, CircleShape)
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        statusName,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = statusColor
                                    )
                                    if (!room.isAvailable) {
                                        Text(
                                            "Reserved for this day",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = statusColor.copy(0.7f)
                                        )
                                    }
                                }
                            }
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
                            shape  = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A2E1A))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(Color(0xFF1A3020), Color(0xFF0D2010))
                                        )
                                    )
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(SuccessGreen.copy(0.2f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("🌄", fontSize = 18.sp)
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        if (room.hasMountainView) "Panoramic Views" else "Scenic Views",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Color(0xFF90EE90),
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        if (room.hasMountainView) "Panoramic mountain & city views from your room"
                                        else "Beautiful scenic views from your room",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFFB8F0B8)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(20.dp))
                }
            }
        }
    }

    // ── Booking Dialog with Date & Time Pickers ──────────────────────────────
    if (showBookingDialog && room != null) {
        BookingDialog(
            room           = room,
            checkInMillis  = checkInMillis,
            checkOutMillis = checkOutMillis,
            checkInHour    = checkInHour,
            checkInMinute  = checkInMinute,
            nights         = nights,
            guests         = guests,
            special        = special,
            displayFmt     = displayFmt,
            isLoading      = uiState.isLoading,
            onCheckInChange  = { checkInMillis = it },
            onCheckOutChange = { checkOutMillis = it },
            onTimeChange     = { h, m -> checkInHour = h; checkInMinute = m },
            onGuestsChange   = { guests = it },
            onSpecialChange  = { special = it },
            onProceed        = { showBookingDialog = false; showPaymentSheet = true },
            onDismiss        = { showBookingDialog = false }
        )
    }

    // ── Payment Sheet ─────────────────────────────────────────────────────────
    if (showPaymentSheet) {
        ModalBottomSheet(
            onDismissRequest = { showPaymentSheet = false },
            containerColor   = Color(0xFF0A1424),
            shape            = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            PaymentSelectionSheet(
                selectedMethod   = paymentUiState.selectedMethod,
                onMethodSelected = { paymentViewModel.selectMethod(it) },
                amount           = (room?.pricePerNight ?: 0.0) * nights,
                currency         = room?.currency ?: "ETB",
                onConfirm = {
                    paymentViewModel.processPayment(
                        amount      = (room?.pricePerNight ?: 0.0) * nights,
                        referenceId = "ROOM-BOOKING"
                    )
                },
                isProcessing = paymentUiState.isProcessing
            )
        }
    }

    // ── Booking Success Dialog ────────────────────────────────────────────────
    if (showSuccessDialog && room != null) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false; onBack() },
            containerColor   = Color(0xFF0A1424),
            shape            = RoundedCornerShape(28.dp),
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()) {
                    Text("🎉", fontSize = 48.sp)
                    Spacer(Modifier.height(8.dp))
                    Text("Booking Confirmed!",
                        color = SuccessGreen,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = SuccessGreen.copy(0.08f),
                        border = BorderStroke(1.dp, SuccessGreen.copy(0.25f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            ConfirmRow("Room", room.name)
                            ConfirmRow("Type", room.type.displayName)
                            ConfirmRow("Check-In", checkInStr)
                            ConfirmRow("Check-Out", checkOutStr)
                            ConfirmRow("Nights", "$nights night${if (nights > 1) "s" else ""}")
                            ConfirmRow("Guests", guests)
                            ConfirmRow("Total", "${room.currency} ${(room.pricePerNight * nights).toInt()}")
                            ConfirmRow("Ref #", bookingConfirmRef)
                        }
                    }
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = GoldPrimary.copy(0.07f),
                        border = BorderStroke(1.dp, GoldPrimary.copy(0.2f))
                    ) {
                        Row(modifier = Modifier.padding(10.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Email, null,
                                tint = GoldPrimary, modifier = Modifier.size(14.dp))
                            Text("A confirmation email has been sent to your registered address.",
                                style = MaterialTheme.typography.labelSmall,
                                color = GoldPrimary.copy(0.8f))
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showSuccessDialog = false; onBack() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                ) {
                    Text("Done", color = SurfaceDark, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Booking Dialog — Date picker + Time picker + Summary
// ─────────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BookingDialog(
    room: HotelRoom,
    checkInMillis: Long,
    checkOutMillis: Long,
    checkInHour: Int,
    checkInMinute: Int,
    nights: Int,
    guests: String,
    special: String,
    displayFmt: SimpleDateFormat,
    isLoading: Boolean,
    onCheckInChange: (Long) -> Unit,
    onCheckOutChange: (Long) -> Unit,
    onTimeChange: (Int, Int) -> Unit,
    onGuestsChange: (String) -> Unit,
    onSpecialChange: (String) -> Unit,
    onProceed: () -> Unit,
    onDismiss: () -> Unit
) {
    // Which picker is open
    var showCheckInDatePicker  by remember { mutableStateOf(false) }
    var showCheckOutDatePicker by remember { mutableStateOf(false) }
    var showTimePicker         by remember { mutableStateOf(false) }

    val totalPrice = room.pricePerNight * nights
    val timeStr = "%02d:%02d %s".format(
        if (checkInHour % 12 == 0) 12 else checkInHour % 12,
        checkInMinute,
        if (checkInHour < 12) "AM" else "PM"
    )

    // Validation
    val guestCount = guests.toIntOrNull() ?: 0
    val isValid = checkOutMillis > checkInMillis && guestCount >= 1 && guestCount <= room.capacity

    // ── Check-In Date Picker ──────────────────────────────────────────────────
    if (showCheckInDatePicker) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = checkInMillis,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    val today = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                    }.timeInMillis
                    return utcTimeMillis >= today
                }
            }
        )
        DatePickerDialog(
            onDismissRequest = { showCheckInDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let {
                        onCheckInChange(it)
                        // Auto-push check-out if it's now before check-in
                        if (checkOutMillis <= it) {
                            onCheckOutChange(it + 86_400_000L)
                        }
                    }
                    showCheckInDatePicker = false
                }) { Text("OK", color = GoldPrimary, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showCheckInDatePicker = false }) {
                    Text("Cancel", color = OnSurfaceDark.copy(0.6f))
                }
            },
            colors = DatePickerDefaults.colors(
                containerColor                  = Color(0xFF0E1B2A),
                titleContentColor               = GoldPrimary,
                headlineContentColor            = OnSurfaceDark,
                weekdayContentColor             = OnSurfaceDark.copy(0.6f),
                subheadContentColor             = OnSurfaceDark.copy(0.6f),
                navigationContentColor          = OnSurfaceDark,
                yearContentColor                = OnSurfaceDark,
                currentYearContentColor         = GoldPrimary,
                selectedYearContentColor        = Color(0xFF050D18),
                selectedYearContainerColor      = GoldPrimary,
                dayContentColor                 = OnSurfaceDark,
                disabledDayContentColor         = OnSurfaceDark.copy(0.25f),
                selectedDayContentColor         = Color(0xFF050D18),
                disabledSelectedDayContentColor = Color(0xFF050D18).copy(0.4f),
                selectedDayContainerColor       = GoldPrimary,
                disabledSelectedDayContainerColor = GoldPrimary.copy(0.3f),
                todayContentColor               = GoldPrimary,
                todayDateBorderColor            = GoldPrimary,
                dayInSelectionRangeContentColor = Color(0xFF050D18),
                dayInSelectionRangeContainerColor = GoldPrimary.copy(0.3f)
            )
        ) { DatePicker(state = state) }
    }

    // ── Check-Out Date Picker ─────────────────────────────────────────────────
    if (showCheckOutDatePicker) {
        val minOut = checkInMillis + 86_400_000L
        val state = rememberDatePickerState(
            initialSelectedDateMillis = maxOf(checkOutMillis, minOut),
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                    utcTimeMillis >= minOut
            }
        )
        DatePickerDialog(
            onDismissRequest = { showCheckOutDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { onCheckOutChange(it) }
                    showCheckOutDatePicker = false
                }) { Text("OK", color = GoldPrimary, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showCheckOutDatePicker = false }) {
                    Text("Cancel", color = OnSurfaceDark.copy(0.6f))
                }
            },
            colors = DatePickerDefaults.colors(
                containerColor                  = Color(0xFF0E1B2A),
                titleContentColor               = TealLight,
                headlineContentColor            = OnSurfaceDark,
                weekdayContentColor             = OnSurfaceDark.copy(0.6f),
                subheadContentColor             = OnSurfaceDark.copy(0.6f),
                navigationContentColor          = OnSurfaceDark,
                yearContentColor                = OnSurfaceDark,
                currentYearContentColor         = TealLight,
                selectedYearContentColor        = Color(0xFF050D18),
                selectedYearContainerColor      = TealLight,
                dayContentColor                 = OnSurfaceDark,
                disabledDayContentColor         = OnSurfaceDark.copy(0.25f),
                selectedDayContentColor         = Color(0xFF050D18),
                disabledSelectedDayContentColor = Color(0xFF050D18).copy(0.4f),
                selectedDayContainerColor       = TealLight,
                disabledSelectedDayContainerColor = TealLight.copy(0.3f),
                todayContentColor               = TealLight,
                todayDateBorderColor            = TealLight,
                dayInSelectionRangeContentColor = Color(0xFF050D18),
                dayInSelectionRangeContainerColor = TealLight.copy(0.3f)
            )
        ) { DatePicker(state = state) }
    }

    // ── Time Picker ───────────────────────────────────────────────────────────
    if (showTimePicker) {
        val timeState = rememberTimePickerState(
            initialHour   = checkInHour,
            initialMinute = checkInMinute,
            is24Hour      = false
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            containerColor   = Color(0xFF0E1B2A),
            shape            = RoundedCornerShape(24.dp),
            title = {
                Text("Check-In Time", color = GoldPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    TimePicker(
                        state  = timeState,
                        colors = TimePickerDefaults.colors(
                            clockDialColor          = Color(0xFF162030),
                            clockDialSelectedContentColor = Color(0xFF050D18),
                            clockDialUnselectedContentColor = OnSurfaceDark,
                            selectorColor           = GoldPrimary,
                            containerColor          = Color.Transparent,
                            periodSelectorBorderColor = GoldPrimary.copy(0.3f),
                            timeSelectorSelectedContainerColor = GoldPrimary,
                            timeSelectorUnselectedContainerColor = Color(0xFF162030),
                            timeSelectorSelectedContentColor = Color(0xFF050D18),
                            timeSelectorUnselectedContentColor = OnSurfaceDark
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onTimeChange(timeState.hour, timeState.minute)
                        showTimePicker = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                    shape  = RoundedCornerShape(12.dp)
                ) { Text("Set Time", color = Color(0xFF050D18), fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Cancel", color = OnSurfaceDark.copy(0.6f))
                }
            }
        )
    }

    // ── Main Booking Dialog ───────────────────────────────────────────────────
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = Color(0xFF0A1424),
        shape            = RoundedCornerShape(28.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier.size(38.dp)
                        .background(GoldPrimary.copy(0.15f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Default.Hotel, null, tint = GoldPrimary, modifier = Modifier.size(20.dp)) }
                Column {
                    Text("Book ${room.name}", color = GoldPrimary,
                        fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                    Text(room.type.displayName,
                        style = MaterialTheme.typography.labelSmall,
                        color = OnSurfaceDark.copy(0.5f))
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                // ── Date row ─────────────────────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Check-In
                    DateTimeButton(
                        label    = "Check-In",
                        icon     = Icons.Default.FlightLand,
                        line1    = displayFmt.format(Date(checkInMillis)),
                        line2    = timeStr,
                        color    = GoldPrimary,
                        modifier = Modifier.weight(1f),
                        onClick  = { showCheckInDatePicker = true }
                    )
                    // Arrow
                    Box(
                        modifier = Modifier.align(Alignment.CenterVertically)
                    ) {
                        Icon(Icons.Default.ArrowForward, null,
                            tint = OnSurfaceDark.copy(0.3f), modifier = Modifier.size(18.dp))
                    }
                    // Check-Out
                    DateTimeButton(
                        label    = "Check-Out",
                        icon     = Icons.Default.FlightTakeoff,
                        line1    = displayFmt.format(Date(checkOutMillis)),
                        line2    = "12:00 PM",
                        color    = TealLight,
                        modifier = Modifier.weight(1f),
                        onClick  = { showCheckOutDatePicker = true }
                    )
                }

                // ── Check-in time button ──────────────────────────────────────
                Surface(
                    onClick  = { showTimePicker = true },
                    shape    = RoundedCornerShape(14.dp),
                    color    = Color(0xFF0E1B2A),
                    border   = BorderStroke(1.dp, GoldPrimary.copy(0.25f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Default.Schedule, null,
                            tint = GoldPrimary, modifier = Modifier.size(18.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Check-In Time",
                                style = MaterialTheme.typography.labelSmall,
                                color = OnSurfaceDark.copy(0.5f))
                            Text(timeStr,
                                style = MaterialTheme.typography.bodyMedium,
                                color = OnSurfaceDark, fontWeight = FontWeight.Bold)
                        }
                        Icon(Icons.Default.Edit, null,
                            tint = GoldPrimary.copy(0.5f), modifier = Modifier.size(16.dp))
                    }
                }

                // ── Nights summary pill ───────────────────────────────────────
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = GoldPrimary.copy(0.1f),
                    border = BorderStroke(1.dp, GoldPrimary.copy(0.25f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.NightsStay, null,
                                tint = GoldPrimary, modifier = Modifier.size(16.dp))
                            Text("$nights night${if (nights > 1) "s" else ""}",
                                color = GoldPrimary, fontWeight = FontWeight.Bold)
                        }
                        Text("${room.currency} ${room.pricePerNight.toInt()} × $nights",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceDark.copy(0.6f))
                        Text("${room.currency} ${totalPrice.toInt()}",
                            color = GoldPrimary, fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp)
                    }
                }

                // ── Guests ────────────────────────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFF0E1B2A),
                        border = BorderStroke(1.dp, Color.White.copy(0.08f)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.People, null,
                                tint = TealLight, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            OutlinedTextField(
                                value = guests,
                                onValueChange = { if (it.length <= 2 && it.all(Char::isDigit)) onGuestsChange(it) },
                                label = { Text("Guests", style = MaterialTheme.typography.labelSmall) },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor   = Color.Transparent,
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedTextColor     = OnSurfaceDark,
                                    unfocusedTextColor   = OnSurfaceDark,
                                    focusedLabelColor    = TealLight,
                                    cursorColor          = GoldPrimary
                                )
                            )
                        }
                    }
                    // Capacity hint
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = TealPrimary.copy(0.1f),
                        border = BorderStroke(1.dp, TealPrimary.copy(0.2f))
                    ) {
                        Text("Max ${room.capacity}",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = TealLight)
                    }
                }

                // ── Special requests ──────────────────────────────────────────
                OutlinedTextField(
                    value = special,
                    onValueChange = onSpecialChange,
                    label = { Text("Special Requests (optional)") },
                    leadingIcon = { Icon(Icons.Default.Notes, null,
                        tint = GoldPrimary.copy(0.5f), modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 3,
                    shape = RoundedCornerShape(14.dp),
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

                // Validation warning
                val guestCount = guests.toIntOrNull() ?: 0
                if (guestCount < 1) {
                    Text("Please enter at least 1 guest",
                        color = Color(0xFFE24A4A),
                        style = MaterialTheme.typography.labelSmall)
                }
                if (guestCount > room.capacity) {
                    Text("Cannot exceed maximum guests for this room",
                        color = Color(0xFFE24A4A),
                        style = MaterialTheme.typography.labelSmall)
                }
                if (checkOutMillis <= checkInMillis) {
                    Text("Check-out must be after check-in",
                        color = Color(0xFFE24A4A),
                        style = MaterialTheme.typography.labelSmall)
                }

                if (isLoading) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = GoldPrimary,
                        trackColor = GoldPrimary.copy(0.15f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick  = onProceed,
                enabled  = isValid && !isLoading,
                shape    = RoundedCornerShape(14.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Payment, null,
                    tint = Color(0xFF050D18), modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Proceed to Payment · ${room.currency} ${totalPrice.toInt()}",
                    color = Color(0xFF050D18), fontWeight = FontWeight.ExtraBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = OnSurfaceDark.copy(0.5f))
            }
        }
    )
}

// ── Small helper composable for date/time tap buttons ────────────────────────
@Composable
private fun DateTimeButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    line1: String,
    line2: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick  = onClick,
        modifier = modifier,
        shape    = RoundedCornerShape(14.dp),
        color    = Color(0xFF0E1B2A),
        border   = BorderStroke(1.dp, color.copy(0.3f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                Icon(icon, null, tint = color, modifier = Modifier.size(14.dp))
                Text(label, style = MaterialTheme.typography.labelSmall, color = color)
            }
            Spacer(Modifier.height(4.dp))
            Text(line1, style = MaterialTheme.typography.bodySmall,
                color = OnSurfaceDark, fontWeight = FontWeight.Bold, maxLines = 1)
            Text(line2, style = MaterialTheme.typography.labelSmall,
                color = OnSurfaceDark.copy(0.5f))
        }
    }
}

// ── Booking confirmation row ──────────────────────────────────────────────────
@Composable
private fun ConfirmRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label,
            style = MaterialTheme.typography.bodySmall,
            color = OnSurfaceDark.copy(0.5f))
        Text(value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = OnSurfaceDark)
    }
}
