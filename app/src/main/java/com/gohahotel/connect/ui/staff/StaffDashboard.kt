package com.gohahotel.connect.ui.staff

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.gohahotel.connect.domain.model.Booking
import com.gohahotel.connect.domain.model.BookingStatus
import com.gohahotel.connect.domain.model.RoomStatus
import com.gohahotel.connect.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffDashboard(
    onBack: () -> Unit,
    onLogout: () -> Unit,
    viewModel: StaffViewModel = hiltViewModel()
) {
    val bookings by viewModel.allBookings.collectAsState(initial = emptyList())
    val roomStatuses by viewModel.roomStatuses.collectAsState(initial = emptyList())
    val isLoading by viewModel.isLoading.collectAsState(initial = false)
    
    var selectedFilter by remember { mutableStateOf(BookingStatus.CONFIRMED) }
    var showRoomDetails by remember { mutableStateOf(false) }
    var selectedRoom by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.loadAllBookings()
        viewModel.loadRoomStatuses()
    }

    Scaffold(
        containerColor = SurfaceDark,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Staff Dashboard", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                        Text("Room Bookings & Status", style = MaterialTheme.typography.labelSmall, color = GoldPrimary.copy(0.7f))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = GoldPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.Logout, "Logout", tint = GoldPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceDark.copy(0.95f)
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(SurfaceDark, Color(0xFF0A1424))))
        ) {
            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GoldPrimary)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    // ── Quick Stats ──────────────────────────────────────────────
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StaffStatCard(
                            label = "Total Bookings",
                            value = bookings.size.toString(),
                            icon = Icons.Default.BookOnline,
                            color = GoldPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        StaffStatCard(
                            label = "Checked In",
                            value = bookings.count { it.status == BookingStatus.CHECKED_IN }.toString(),
                            icon = Icons.Default.CheckCircle,
                            color = SuccessGreen,
                            modifier = Modifier.weight(1f)
                        )
                        StaffStatCard(
                            label = "Pending",
                            value = bookings.count { it.status == BookingStatus.PENDING }.toString(),
                            icon = Icons.Default.Schedule,
                            color = Color(0xFFFF9800),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(Modifier.height(24.dp))

                    // ── Room Status Overview ─────────────────────────────────────
                    Text(
                        "ROOM STATUS OVERVIEW",
                        style = MaterialTheme.typography.labelMedium,
                        color = GoldPrimary.copy(0.6f),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                    Spacer(Modifier.height(12.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(roomStatuses.size) { index ->
                            val room = roomStatuses[index]
                            RoomStatusCard(
                                roomName = room.roomName,
                                status = room.status,
                                guestName = room.currentGuest,
                                checkOut = room.checkOutDate,
                                onClick = {
                                    selectedRoom = room.roomId
                                    showRoomDetails = true
                                }
                            )
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    // ── Booking Filter Tabs ──────────────────────────────────────
                    Text(
                        "ACTIVE BOOKINGS",
                        style = MaterialTheme.typography.labelMedium,
                        color = GoldPrimary.copy(0.6f),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                    Spacer(Modifier.height(12.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            BookingStatus.PENDING,
                            BookingStatus.CONFIRMED,
                            BookingStatus.CHECKED_IN,
                            BookingStatus.CHECKED_OUT
                        ).forEach { status ->
                            FilterChip(
                                selected = selectedFilter == status,
                                onClick = { selectedFilter = status },
                                label = { Text(status.displayName) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = GoldPrimary,
                                    selectedLabelColor = SurfaceDark,
                                    containerColor = CardDark,
                                    labelColor = OnSurfaceDark
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    selectedBorderColor = GoldPrimary,
                                    borderColor = GoldPrimary.copy(0.3f)
                                )
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // ── Bookings List ────────────────────────────────────────────
                    val filteredBookings = bookings.filter { it.status == selectedFilter }
                    
                    if (filteredBookings.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.EventBusy,
                                    null,
                                    modifier = Modifier.size(48.dp),
                                    tint = GoldPrimary.copy(0.3f)
                                )
                                Spacer(Modifier.height(12.dp))
                                Text(
                                    "No ${selectedFilter.displayName.lowercase()} bookings",
                                    color = OnSurfaceDark.copy(0.5f)
                                )
                            }
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            filteredBookings.forEach { booking ->
                                BookingCard(booking)
                            }
                        }
                    }

                    Spacer(Modifier.height(40.dp))
                }
            }
        }
    }

    // ── Room Details Dialog ──────────────────────────────────────────────────
    if (showRoomDetails) {
        val room = roomStatuses.find { it.roomId == selectedRoom }
        if (room != null) {
            AlertDialog(
                onDismissRequest = { showRoomDetails = false },
                containerColor = Color(0xFF0A1424),
                shape = RoundedCornerShape(24.dp),
                title = {
                    Text(room.roomName, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        RoomDetailRow("Status", room.status.displayName, room.status.color)
                        RoomDetailRow("Current Guest", room.currentGuest.ifEmpty { "Vacant" })
                        RoomDetailRow("Check-In", room.checkInDate.ifEmpty { "N/A" })
                        RoomDetailRow("Check-Out", room.checkOutDate.ifEmpty { "N/A" })
                        RoomDetailRow("Occupancy", "${room.occupancyPercentage}%")
                        RoomDetailRow("Next Available", room.nextAvailableDate.ifEmpty { "Available Now" })
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { showRoomDetails = false },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                    ) {
                        Text("Close", color = SurfaceDark)
                    }
                }
            )
        }
    }
}

@Composable
private fun StaffStatCard(
    label: String,
    value: String,
    icon: androidx.compose.material.icons.Icons.Filled,
    color: Color,
    modifier: Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = CardDark.copy(0.7f),
        border = BorderStroke(1.dp, color.copy(0.2f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(Modifier.height(6.dp))
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = OnSurfaceDark)
            Text(label, style = MaterialTheme.typography.labelSmall, color = OnSurfaceDark.copy(0.6f))
        }
    }
}

@Composable
private fun RoomStatusCard(
    roomName: String,
    status: com.gohahotel.connect.domain.model.RoomStatus,
    guestName: String,
    checkOut: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .width(160.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = CardDark,
        border = BorderStroke(1.dp, status.color.copy(0.3f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(roomName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Surface(
                    shape = CircleShape,
                    color = Color(status.color),
                    modifier = Modifier.size(8.dp)
                )
            }
            Text(
                status.displayName,
                style = MaterialTheme.typography.labelSmall,
                color = Color(status.color),
                fontWeight = FontWeight.Bold
            )
            if (guestName.isNotEmpty()) {
                Text(
                    "Guest: $guestName",
                    style = MaterialTheme.typography.labelSmall,
                    color = OnSurfaceDark.copy(0.7f),
                    maxLines = 1
                )
            }
            if (checkOut.isNotEmpty()) {
                Text(
                    "Check-out: $checkOut",
                    style = MaterialTheme.typography.labelSmall,
                    color = OnSurfaceDark.copy(0.6f)
                )
            }
        }
    }
}

@Composable
private fun BookingCard(booking: Booking) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = CardDark,
        border = BorderStroke(1.dp, Color.White.copy(0.05f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        booking.guestName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = OnSurfaceDark
                    )
                    Text(
                        booking.roomName,
                        style = MaterialTheme.typography.labelMedium,
                        color = GoldPrimary
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(booking.status.color).copy(0.2f)
                ) {
                    Text(
                        booking.status.displayName,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(booking.status.color),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                BookingDetailItem("Check-In", booking.checkInDate)
                BookingDetailItem("Check-Out", booking.checkOutDate)
                BookingDetailItem("Guests", booking.numberOfGuests.toString())
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Contact: ${booking.guestPhone}",
                    style = MaterialTheme.typography.labelSmall,
                    color = OnSurfaceDark.copy(0.7f)
                )
                Text(
                    "${booking.currency} ${booking.totalPrice.toInt()}",
                    fontWeight = FontWeight.Bold,
                    color = GoldPrimary
                )
            }
        }
    }
}

@Composable
private fun BookingDetailItem(label: String, value: String) {
    Column {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = OnSurfaceDark.copy(0.6f)
        )
        Text(
            value,
            style = MaterialTheme.typography.labelMedium,
            color = OnSurfaceDark,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun RoomDetailRow(label: String, value: String, color: String? = null) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = OnSurfaceDark.copy(0.7f))
        Text(
            value,
            fontWeight = FontWeight.Bold,
            color = if (color != null) Color(android.graphics.Color.parseColor(color)) else GoldPrimary
        )
    }
}
