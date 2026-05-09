package com.gohahotel.connect.ui.rooms

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gohahotel.connect.domain.model.Booking
import com.gohahotel.connect.domain.model.BookingStatus
import com.gohahotel.connect.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyReservationsScreen(
    viewModel: RoomViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onRoomClick: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadMyReservations() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "My Reservations",
                            fontWeight = FontWeight.ExtraBold,
                            color = GoldPrimary
                        )
                        Text(
                            "${uiState.myBookings.size} booking${if (uiState.myBookings.size != 1) "s" else ""}",
                            style = MaterialTheme.typography.labelSmall,
                            color = OnSurfaceDark.copy(0.5f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = GoldPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceDark.copy(0.97f)
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF071520), Color(0xFF050D18))
                    )
                )
                .padding(padding)
        ) {
            when {
                uiState.isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = GoldPrimary)
                    }
                }
                uiState.myBookings.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("🏨", fontSize = 64.sp)
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "No Reservations Yet",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = OnSurfaceDark
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Your room bookings will appear here after you complete a reservation.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = OnSurfaceDark.copy(0.5f),
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(24.dp))
                        Button(
                            onClick = onBack,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                        ) {
                            Icon(Icons.Default.Bed, null,
                                tint = SurfaceDark, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Browse Rooms", color = SurfaceDark, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(
                            uiState.myBookings.sortedByDescending { it.createdAt },
                            key = { it.id }
                        ) { booking ->
                            ReservationCard(
                                booking = booking,
                                onClick = { if (booking.roomId.isNotBlank()) onRoomClick(booking.roomId) }
                            )
                        }
                        item { Spacer(Modifier.height(16.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReservationCard(booking: Booking, onClick: () -> Unit) {
    val statusColor = when (booking.status) {
        BookingStatus.CONFIRMED   -> SuccessGreen
        BookingStatus.CHECKED_IN  -> InfoBlue
        BookingStatus.CHECKED_OUT -> OnSurfaceDark.copy(0.4f)
        BookingStatus.CANCELLED   -> ErrorRed
        BookingStatus.PENDING     -> WarningAmber
    }
    val statusEmoji = when (booking.status) {
        BookingStatus.CONFIRMED   -> "✅"
        BookingStatus.CHECKED_IN  -> "🔑"
        BookingStatus.CHECKED_OUT -> "🏁"
        BookingStatus.CANCELLED   -> "❌"
        BookingStatus.PENDING     -> "⏳"
    }

    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFF0C1A28),
        border = BorderStroke(1.5.dp, statusColor.copy(0.3f))
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
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .background(GoldPrimary.copy(0.12f), CircleShape)
                            .border(1.dp, GoldPrimary.copy(0.25f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Hotel, null,
                            tint = GoldPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column {
                        Text(
                            booking.roomName.ifBlank { "Room" },
                            fontWeight = FontWeight.ExtraBold,
                            color = GoldPrimary,
                            fontSize = 18.sp
                        )
                        Text(
                            booking.roomType.ifBlank { "Standard" },
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceDark.copy(0.5f)
                        )
                    }
                }
                // Status badge
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = statusColor.copy(0.12f),
                    border = BorderStroke(1.dp, statusColor.copy(0.35f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(statusEmoji, fontSize = 12.sp)
                        Text(
                            booking.status.displayName,
                            style = MaterialTheme.typography.labelSmall,
                            color = statusColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 14.dp),
                color = Color.White.copy(0.06f)
            )

            // ── Dates ─────────────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Check-in
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    color = GoldPrimary.copy(0.07f),
                    border = BorderStroke(1.dp, GoldPrimary.copy(0.2f))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.FlightLand, null,
                                tint = GoldPrimary, modifier = Modifier.size(12.dp))
                            Text("Check-In",
                                style = MaterialTheme.typography.labelSmall,
                                color = GoldPrimary.copy(0.7f))
                        }
                        Spacer(Modifier.height(3.dp))
                        Text(
                            booking.checkInDate.ifBlank { "—" },
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = OnSurfaceDark
                        )
                    }
                }
                // Check-out
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    color = TealLight.copy(0.07f),
                    border = BorderStroke(1.dp, TealLight.copy(0.2f))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.FlightTakeoff, null,
                                tint = TealLight, modifier = Modifier.size(12.dp))
                            Text("Check-Out",
                                style = MaterialTheme.typography.labelSmall,
                                color = TealLight.copy(0.7f))
                        }
                        Spacer(Modifier.height(3.dp))
                        Text(
                            booking.checkOutDate.ifBlank { "—" },
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = OnSurfaceDark
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // ── Summary row ───────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Nights
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.NightsStay, null,
                            tint = OnSurfaceDark.copy(0.4f), modifier = Modifier.size(14.dp))
                        Text(
                            "${booking.numberOfNights} night${if (booking.numberOfNights != 1) "s" else ""}",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceDark.copy(0.6f)
                        )
                    }
                    // Guests
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.People, null,
                            tint = OnSurfaceDark.copy(0.4f), modifier = Modifier.size(14.dp))
                        Text(
                            "${booking.numberOfGuests} guest${if (booking.numberOfGuests != 1) "s" else ""}",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceDark.copy(0.6f)
                        )
                    }
                }
                // Total price
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "Total Paid",
                        style = MaterialTheme.typography.labelSmall,
                        color = OnSurfaceDark.copy(0.4f)
                    )
                    Text(
                        "${booking.currency} ${booking.totalPrice.toInt()}",
                        fontWeight = FontWeight.ExtraBold,
                        color = GoldPrimary,
                        fontSize = 16.sp
                    )
                }
            }

            // Special requests
            if (booking.specialRequests.isNotBlank()) {
                Spacer(Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White.copy(0.04f),
                    border = BorderStroke(1.dp, Color.White.copy(0.08f))
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(Icons.Default.Notes, null,
                            tint = OnSurfaceDark.copy(0.4f), modifier = Modifier.size(13.dp))
                        Text(
                            booking.specialRequests,
                            style = MaterialTheme.typography.labelSmall,
                            color = OnSurfaceDark.copy(0.5f)
                        )
                    }
                }
            }
        }
    }
}
