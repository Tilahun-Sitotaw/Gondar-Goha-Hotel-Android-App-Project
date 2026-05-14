package com.gohahotel.connect.ui.staff

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.gohahotel.connect.domain.model.Booking
import com.gohahotel.connect.domain.model.BookingStatus
import com.gohahotel.connect.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffBookingsScreen(
    viewModel: StaffViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedFilter by remember { mutableStateOf<BookingStatus?>(null) }

    val filteredBookings = if (selectedFilter != null) {
        uiState.bookings.filter { it.status == selectedFilter }
    } else {
        uiState.bookings
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Room Bookings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Filter chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedFilter == null,
                        onClick = { selectedFilter = null },
                        label = { Text("All") },
                        leadingIcon = if (selectedFilter == null) {
                            { Icon(Icons.Default.Check, null, modifier = Modifier.size(18.dp)) }
                        } else null
                    )
                }
                item {
                    FilterChip(
                        selected = selectedFilter == BookingStatus.PENDING,
                        onClick = { selectedFilter = BookingStatus.PENDING },
                        label = { Text("Pending") },
                        leadingIcon = if (selectedFilter == BookingStatus.PENDING) {
                            { Icon(Icons.Default.Check, null, modifier = Modifier.size(18.dp)) }
                        } else null
                    )
                }
                item {
                    FilterChip(
                        selected = selectedFilter == BookingStatus.CHECKED_IN,
                        onClick = { selectedFilter = BookingStatus.CHECKED_IN },
                        label = { Text("Checked In") },
                        leadingIcon = if (selectedFilter == BookingStatus.CHECKED_IN) {
                            { Icon(Icons.Default.Check, null, modifier = Modifier.size(18.dp)) }
                        } else null
                    )
                }
                item {
                    FilterChip(
                        selected = selectedFilter == BookingStatus.CHECKED_OUT,
                        onClick = { selectedFilter = BookingStatus.CHECKED_OUT },
                        label = { Text("Checked Out") },
                        leadingIcon = if (selectedFilter == BookingStatus.CHECKED_OUT) {
                            { Icon(Icons.Default.Check, null, modifier = Modifier.size(18.dp)) }
                        } else null
                    )
                }
            }

            // Bookings list
            if (filteredBookings.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Default.Hotel,
                            null,
                            modifier = Modifier.size(64.dp),
                            tint = GoldPrimary.copy(0.3f)
                        )
                        Text(
                            "No bookings",
                            style = MaterialTheme.typography.titleMedium,
                            color = OnSurfaceDark.copy(0.5f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    items(filteredBookings) { booking ->
                        StaffBookingCard(
                            booking = booking,
                            onCheckIn = {
                                viewModel.updateBookingStatus(booking.id, BookingStatus.CHECKED_IN.name)
                            },
                            onCheckOut = {
                                viewModel.updateBookingStatus(booking.id, BookingStatus.CHECKED_OUT.name)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StaffBookingCard(
    booking: Booking,
    onCheckIn: () -> Unit,
    onCheckOut: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()) }
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    
    val statusColor = when (booking.status) {
        BookingStatus.PENDING -> Color(0xFFFF9800)
        BookingStatus.CHECKED_IN -> SuccessGreen
        BookingStatus.CHECKED_OUT -> OnSurfaceDark.copy(0.5f)
        else -> GoldPrimary
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF0E1B2A)
        ),
        border = BorderStroke(1.dp, statusColor.copy(0.3f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header with guest name and room number
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        booking.guestName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = GoldPrimary
                    )
                    Text(
                        "Room ${booking.roomName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceDark.copy(0.7f)
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = statusColor.copy(0.15f),
                    border = BorderStroke(1.dp, statusColor.copy(0.5f))
                ) {
                    Text(
                        booking.status.displayName,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Divider(color = OnSurfaceDark.copy(0.1f))

            // Check-in and Check-out dates
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Check-in
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF162030),
                    border = BorderStroke(1.dp, TealLight.copy(0.2f))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                Icons.Default.FlightLand,
                                null,
                                modifier = Modifier.size(14.dp),
                                tint = TealLight
                            )
                            Text(
                                "Check-In",
                                style = MaterialTheme.typography.labelSmall,
                                color = OnSurfaceDark.copy(0.6f)
                            )
                        }
                        Text(
                            dateFormat.format(java.util.Date(booking.checkInDate.toLongOrNull() ?: 0)),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = OnSurfaceDark
                        )
                    }
                }

                // Check-out
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF162030),
                    border = BorderStroke(1.dp, Color(0xFFE24A4A).copy(0.2f))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                Icons.Default.FlightTakeoff,
                                null,
                                modifier = Modifier.size(14.dp),
                                tint = Color(0xFFE24A4A)
                            )
                            Text(
                                "Check-Out",
                                style = MaterialTheme.typography.labelSmall,
                                color = OnSurfaceDark.copy(0.6f)
                            )
                        }
                        Text(
                            dateFormat.format(java.util.Date(booking.checkOutDate.toLongOrNull() ?: 0)),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = OnSurfaceDark
                        )
                    }
                }
            }

            // Guest details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    color = GoldPrimary.copy(0.08f),
                    border = BorderStroke(1.dp, GoldPrimary.copy(0.2f))
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.People,
                            null,
                            modifier = Modifier.size(14.dp),
                            tint = GoldPrimary
                        )
                        Text(
                            "${booking.numberOfGuests} guest${if (booking.numberOfGuests > 1) "s" else ""}",
                            style = MaterialTheme.typography.labelSmall,
                            color = GoldPrimary
                        )
                    }
                }

                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    color = GoldPrimary.copy(0.08f),
                    border = BorderStroke(1.dp, GoldPrimary.copy(0.2f))
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.NightsStay,
                            null,
                            modifier = Modifier.size(14.dp),
                            tint = GoldPrimary
                        )
                        Text(
                            "${booking.numberOfNights} night${if (booking.numberOfNights > 1) "s" else ""}",
                            style = MaterialTheme.typography.labelSmall,
                            color = GoldPrimary
                        )
                    }
                }
            }

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (booking.status == BookingStatus.PENDING) {
                    Button(
                        onClick = onCheckIn,
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SuccessGreen
                        )
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("Check In", fontWeight = FontWeight.Bold)
                    }
                } else if (booking.status == BookingStatus.CHECKED_IN) {
                    Button(
                        onClick = onCheckOut,
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE24A4A)
                        )
                    ) {
                        Icon(
                            Icons.Default.ExitToApp,
                            null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("Check Out", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
