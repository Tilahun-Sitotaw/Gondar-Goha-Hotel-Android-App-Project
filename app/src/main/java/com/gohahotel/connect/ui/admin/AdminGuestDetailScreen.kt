package com.gohahotel.connect.ui.admin

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.gohahotel.connect.ui.theme.*
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminGuestDetailScreen(
    userId: String,
    onBack: () -> Unit,
    viewModel: AdminViewModel = hiltViewModel()
) {
    var guestData by remember { mutableStateOf<Map<String, Any>?>(null) }
    var roomBookings by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var foodOrders by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedTab by remember { mutableStateOf(0) }

    val firestore = FirebaseFirestore.getInstance()

    LaunchedEffect(userId) {
        isLoading = true
        try {
            // Fetch guest data
            val userDoc = firestore.collection("users").document(userId).get().await()
            guestData = userDoc.data

            // Fetch room bookings
            val bookingsSnapshot = firestore.collection("bookings")
                .whereEqualTo("userId", userId)
                .get().await()
            roomBookings = bookingsSnapshot.documents.mapNotNull { it.data?.plus("id" to it.id) }

            // Fetch food orders
            val ordersSnapshot = firestore.collection("orders")
                .whereEqualTo("userId", userId)
                .get().await()
            foodOrders = ordersSnapshot.documents.mapNotNull { it.data?.plus("id" to it.id) }
        } catch (e: Exception) {
            // Handle error
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Guest Details", fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp, color = GoldPrimary)
                        Text("Complete profile & order history",
                            style = MaterialTheme.typography.labelSmall,
                            color = OnSurfaceDark.copy(0.5f))
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
                .background(Brush.verticalGradient(listOf(Color(0xFF071520), Color(0xFF050D18))))
                .padding(padding)
        ) {
            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GoldPrimary)
                }
            } else if (guestData == null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Guest not found", color = OnSurfaceDark.copy(0.5f))
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // ── Guest Profile Card ────────────────────────────────────
                    item {
                        GuestProfileCard(guestData!!)
                    }

                    // ── Tab Selector ──────────────────────────────────────────
                    item {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFF0E1B2A),
                            border = BorderStroke(1.dp, Color.White.copy(0.06f))
                        ) {
                            Row(modifier = Modifier.padding(4.dp)) {
                                listOf(
                                    "Room Bookings" to roomBookings.size,
                                    "Food Orders" to foodOrders.size
                                ).forEachIndexed { index, (label, count) ->
                                    val selected = selectedTab == index
                                    Surface(
                                        onClick = { selectedTab = index },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (selected) GoldPrimary else Color.Transparent
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(vertical = 12.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                label,
                                                style = MaterialTheme.typography.labelMedium,
                                                color = if (selected) Color(0xFF050D18) else OnSurfaceDark.copy(0.5f),
                                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                            )
                                            Text(
                                                "$count",
                                                style = MaterialTheme.typography.titleMedium,
                                                color = if (selected) Color(0xFF050D18) else GoldPrimary,
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // ── Content based on selected tab ─────────────────────────
                    if (selectedTab == 0) {
                        // Room Bookings
                        if (roomBookings.isEmpty()) {
                            item {
                                EmptyStateCard("No room bookings yet", Icons.Default.Hotel)
                            }
                        } else {
                            items(roomBookings) { booking ->
                                RoomBookingCard(booking)
                            }
                        }
                    } else {
                        // Food Orders
                        if (foodOrders.isEmpty()) {
                            item {
                                EmptyStateCard("No food orders yet", Icons.Default.Restaurant)
                            }
                        } else {
                            items(foodOrders) { order ->
                                FoodOrderCard(order)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GuestProfileCard(guestData: Map<String, Any>) {
    val name = (guestData["displayName"] as? String)?.takeIf { it.isNotBlank() }
        ?: (guestData["email"] as? String)?.substringBefore("@")?.replaceFirstChar { it.uppercase() }
        ?: "Guest"
    val email = guestData["email"] as? String ?: ""
    val phone = guestData["phoneNumber"] as? String ?: ""
    val address = guestData["address"] as? String ?: ""
    val role = guestData["role"] as? String ?: "GUEST"
    val profileImg = guestData["profilePhotoUrl"] as? String ?: ""
    val idDocumentUrl = guestData["idDocumentUrl"] as? String ?: ""
    val idDocumentType = guestData["idDocumentType"] as? String ?: ""

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFF0E1B2A),
        border = BorderStroke(1.dp, GoldPrimary.copy(0.2f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Profile photo
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(GoldPrimary.copy(0.15f))
                        .border(2.dp, GoldPrimary.copy(0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (profileImg.isNotBlank()) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(profileImg).crossfade(true).build(),
                            contentDescription = "Profile",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(
                            name.take(1).uppercase(),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldPrimary
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        name,
                        style = MaterialTheme.typography.titleLarge,
                        color = GoldPrimary,
                        fontWeight = FontWeight.ExtraBold
                    )
                    if (email.isNotBlank()) {
                        Text(
                            email,
                            style = MaterialTheme.typography.bodyMedium,
                            color = OnSurfaceDark.copy(0.7f)
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (role == "ADMIN") ErrorRed.copy(0.15f) else GoldPrimary.copy(0.15f)
                    ) {
                        Text(
                            role,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (role == "ADMIN") ErrorRed else GoldPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (phone.isNotBlank() || address.isNotBlank()) {
                Spacer(Modifier.height(16.dp))
                HorizontalDivider(color = Color.White.copy(0.06f))
                Spacer(Modifier.height(16.dp))

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (phone.isNotBlank()) {
                        InfoRow(Icons.Default.Phone, "Phone", phone)
                    }
                    if (address.isNotBlank()) {
                        InfoRow(Icons.Default.LocationOn, "Address", address)
                    }
                }
            }

            // ID Document
            if (idDocumentUrl.isNotBlank()) {
                Spacer(Modifier.height(16.dp))
                HorizontalDivider(color = Color.White.copy(0.06f))
                Spacer(Modifier.height(16.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = GoldPrimary.copy(0.08f),
                    border = BorderStroke(1.dp, GoldPrimary.copy(0.25f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Badge, null,
                                tint = GoldPrimary, modifier = Modifier.size(16.dp))
                            Text("Identity Document",
                                style = MaterialTheme.typography.labelMedium,
                                color = GoldPrimary,
                                fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(idDocumentType.ifBlank { "ID Document" },
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceDark.copy(0.6f))
                        Spacer(Modifier.height(10.dp))
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(idDocumentUrl).crossfade(true).build(),
                            contentDescription = "ID Document",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, Color.White.copy(0.1f), RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = GoldPrimary.copy(0.6f), modifier = Modifier.size(16.dp))
        Text(
            "$label:",
            style = MaterialTheme.typography.labelSmall,
            color = OnSurfaceDark.copy(0.5f),
            modifier = Modifier.width(60.dp)
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            color = OnSurfaceDark,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun RoomBookingCard(booking: Map<String, Any>) {
    val roomName = booking["roomName"] as? String ?: "Room"
    val checkIn = booking["checkInDate"] as? String ?: ""
    val checkOut = booking["checkOutDate"] as? String ?: ""
    val status = booking["status"] as? String ?: "PENDING"
    val totalPrice = (booking["totalPrice"] as? Number)?.toDouble() ?: 0.0

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF0E1B2A),
        border = BorderStroke(1.dp, Color.White.copy(0.06f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Hotel, null,
                        tint = GoldPrimary, modifier = Modifier.size(20.dp))
                    Text(
                        roomName,
                        style = MaterialTheme.typography.titleMedium,
                        color = GoldPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
                StatusBadge(status)
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Check-in", style = MaterialTheme.typography.labelSmall,
                        color = OnSurfaceDark.copy(0.5f))
                    Text(checkIn, style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceDark, fontWeight = FontWeight.SemiBold)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Check-out", style = MaterialTheme.typography.labelSmall,
                        color = OnSurfaceDark.copy(0.5f))
                    Text(checkOut, style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceDark, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(Modifier.height(10.dp))
            HorizontalDivider(color = Color.White.copy(0.06f))
            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Total Amount", style = MaterialTheme.typography.labelMedium,
                    color = OnSurfaceDark.copy(0.6f))
                Text("ETB ${String.format("%.2f", totalPrice)}",
                    style = MaterialTheme.typography.titleMedium,
                    color = GoldPrimary, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

@Composable
private fun FoodOrderCard(order: Map<String, Any>) {
    val orderId = (order["id"] as? String ?: "").take(8)
    val status = order["status"] as? String ?: "PENDING"
    val totalPrice = (order["totalPrice"] as? Number)?.toDouble() ?: 0.0
    val items = order["items"] as? List<Map<String, Any>> ?: emptyList()
    val timestamp = order["timestamp"] as? com.google.firebase.Timestamp

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF0E1B2A),
        border = BorderStroke(1.dp, Color.White.copy(0.06f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Restaurant, null,
                        tint = GoldPrimary, modifier = Modifier.size(20.dp))
                    Text(
                        "Order #$orderId",
                        style = MaterialTheme.typography.titleMedium,
                        color = GoldPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
                StatusBadge(status)
            }

            if (timestamp != null) {
                Spacer(Modifier.height(6.dp))
                Text(
                    java.text.SimpleDateFormat("MMM dd, yyyy • hh:mm a", java.util.Locale.getDefault())
                        .format(timestamp.toDate()),
                    style = MaterialTheme.typography.labelSmall,
                    color = OnSurfaceDark.copy(0.5f)
                )
            }

            Spacer(Modifier.height(12.dp))

            // Order items
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items.take(3).forEach { item ->
                    val name = item["name"] as? String ?: ""
                    val quantity = (item["quantity"] as? Number)?.toInt() ?: 1
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "$quantity× $name",
                            style = MaterialTheme.typography.bodyMedium,
                            color = OnSurfaceDark.copy(0.8f),
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                if (items.size > 3) {
                    Text(
                        "+${items.size - 3} more items",
                        style = MaterialTheme.typography.labelSmall,
                        color = GoldPrimary.copy(0.7f),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(Modifier.height(10.dp))
            HorizontalDivider(color = Color.White.copy(0.06f))
            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Total Amount", style = MaterialTheme.typography.labelMedium,
                    color = OnSurfaceDark.copy(0.6f))
                Text("ETB ${String.format("%.2f", totalPrice)}",
                    style = MaterialTheme.typography.titleMedium,
                    color = GoldPrimary, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

@Composable
private fun StatusBadge(status: String) {
    val (bgColor, textColor) = when (status.uppercase()) {
        "CONFIRMED", "COMPLETED", "DELIVERED" -> SuccessGreen.copy(0.15f) to SuccessGreen
        "PENDING", "PREPARING", "IN_PROGRESS" -> GoldPrimary.copy(0.15f) to GoldPrimary
        "CANCELLED", "REJECTED" -> ErrorRed.copy(0.15f) to ErrorRed
        else -> Color.White.copy(0.1f) to OnSurfaceDark.copy(0.6f)
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = bgColor
    ) {
        Text(
            status.replace("_", " "),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun EmptyStateCard(
    message: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF0E1B2A).copy(0.5f),
        border = BorderStroke(1.dp, Color.White.copy(0.06f))
    ) {
        Column(
            modifier = Modifier.padding(40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = OnSurfaceDark.copy(0.3f),
                modifier = Modifier.size(48.dp)
            )
            Text(
                message,
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurfaceDark.copy(0.4f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
