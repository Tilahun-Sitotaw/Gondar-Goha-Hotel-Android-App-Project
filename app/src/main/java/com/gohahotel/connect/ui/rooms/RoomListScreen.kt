package com.gohahotel.connect.ui.rooms

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import com.gohahotel.connect.domain.model.HotelRoom
import com.gohahotel.connect.domain.model.RoomType
import com.gohahotel.connect.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomListScreen(
    viewModel: RoomViewModel = hiltViewModel(),
    onRoomClick: (String) -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val filters = listOf(null) + RoomType.values().toList()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Browse Rooms", fontWeight = FontWeight.Bold)
                        Text("Goha Hotel · Gondar",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.6f))
                    }
                },
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
                .background(Brush.verticalGradient(listOf(SurfaceDark, Color(0xFF0A1424), Color.Black)))
                .padding(padding)
        ) {
            // ── Filter chips ─────────────────────────────────────────────────
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filters) { filter ->
                    FilterChip(
                        selected = uiState.selectedFilter == filter,
                        onClick  = { viewModel.selectFilter(filter) },
                        label    = { Text(filter?.displayName ?: "All") },
                        leadingIcon = if (uiState.selectedFilter == filter) {
                            { Icon(Icons.Default.Check, null, Modifier.size(16.dp)) }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GoldPrimary,
                            selectedLabelColor     = SurfaceDark,
                            containerColor         = CardDark,
                            labelColor             = OnSurfaceDark.copy(alpha = 0.7f)
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = GoldPrimary.copy(alpha = 0.3f),
                            enabled     = true,
                            selected    = uiState.selectedFilter == filter
                        )
                    )
                }
            }

            if (uiState.isLoading && uiState.rooms.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GoldPrimary)
                }
            } else if (uiState.rooms.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Hotel, null,
                            Modifier.size(64.dp), tint = GoldPrimary.copy(alpha = 0.3f))
                        Spacer(Modifier.height(12.dp))
                        Text("No rooms available", color = Color.White.copy(0.5f))
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(uiState.rooms, key = { it.id }) { room ->
                        RoomCard(room = room, onClick = { onRoomClick(room.id) })
                    }
                }
            }
        }
    }
}

@Composable
fun RoomCard(room: HotelRoom, onClick: () -> Unit) {
    Card(
        onClick  = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(20.dp),
        colors   = CardDefaults.cardColors(containerColor = CardDark),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column {
            // Room image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(
                        Brush.verticalGradient(listOf(TealDark, SurfaceVariantDark))
                    )
            ) {
                    var loadError by remember { mutableStateOf(false) }
                    val imageUrl = room.allImages.firstOrNull() ?: ""
                    
                    if (imageUrl.isNotEmpty()) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(imageUrl)
                                .crossfade(true)
                                .listener(
                                    onError = { _, _ -> loadError = true },
                                    onSuccess = { _, _ -> loadError = false }
                                )
                                .build(),
                            contentDescription = room.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        
                        if (loadError) {
                            Box(Modifier.fillMaxSize().background(Color.Black.copy(0.7f)), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Load Error", color = Color.Red, style = MaterialTheme.typography.labelSmall)
                                    Text(imageUrl.take(20) + "...", color = Color.White, fontSize = 8.sp)
                                }
                            }
                        }
                    } else {
                        Box(
                            Modifier.fillMaxSize().background(
                                Brush.verticalGradient(listOf(TealDark, SurfaceDark))
                            ), 
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Surface(
                                    modifier = Modifier.size(64.dp),
                                    shape = CircleShape,
                                    color = GoldPrimary.copy(alpha = 0.1f),
                                    border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.2f))
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text("G", color = GoldPrimary, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Text("No Photo", color = GoldPrimary.copy(0.5f), style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                // Availability badge
                Surface(
                    modifier = Modifier.align(Alignment.TopEnd).padding(12.dp),
                    shape    = RoundedCornerShape(50),
                    color    = if (room.isAvailable) SuccessGreen else ErrorRed
                ) {
                    Text(
                        if (room.isAvailable) "Available" else "Occupied",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style    = MaterialTheme.typography.labelSmall,
                        color    = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                // Type badge
                Surface(
                    modifier = Modifier.align(Alignment.TopStart).padding(12.dp),
                    shape    = RoundedCornerShape(50),
                    color    = GoldPrimary
                ) {
                    Text(room.type.displayName,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style    = MaterialTheme.typography.labelSmall,
                        color    = SurfaceDark, fontWeight = FontWeight.Bold)
                }
            }

            // Info
            Column(Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(room.name, style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold, color = OnSurfaceDark)
                        Text("Floor ${room.floorNumber} · ${room.bedType}",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceDark.copy(alpha = 0.7f))
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("${room.currency} ${room.pricePerNight.toInt()}",
                            style = MaterialTheme.typography.titleMedium,
                            color = GoldPrimary, fontWeight = FontWeight.Bold)
                        Text("/night", style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(0.5f))
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Amenities chips
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(room.amenities.take(4)) { amenity ->
                        Surface(shape = RoundedCornerShape(50),
                            color = TealDark.copy(alpha = 0.4f)) {
                            Text(amenity,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                style    = MaterialTheme.typography.labelSmall,
                                color    = TealLight)
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.Star, null,
                            Modifier.size(14.dp), tint = GoldPrimary)
                        Text("${room.rating}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold, color = GoldPrimary)
                        Text("(${room.reviewCount})",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(0.5f))
                    }
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.People, null,
                            Modifier.size(14.dp), tint = Color.White.copy(0.5f))
                        Text("${room.capacity} guests",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(0.5f))
                    }
                }
            }
        }
    }
}
