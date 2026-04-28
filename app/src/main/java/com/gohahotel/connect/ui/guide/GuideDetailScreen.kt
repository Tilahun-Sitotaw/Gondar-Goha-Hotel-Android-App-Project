package com.gohahotel.connect.ui.guide

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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.gohahotel.connect.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuideDetailScreen(
    entryId: String,
    viewModel: GuideViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(entryId) { viewModel.loadEntryDetail(entryId) }

    val entry = uiState.selectedEntry

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(entry?.title ?: "Loading...", fontWeight = FontWeight.Bold, maxLines = 1) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        if (entry == null) {
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
                // Hero image
                Box(
                    modifier = Modifier.fillMaxWidth().height(260.dp)
                        .background(Brush.verticalGradient(listOf(TealDark, SurfaceVariantDark)))
                ) {
                    if (entry.imageUrls.isNotEmpty()) {
                        AsyncImage(entry.imageUrls.first(), entry.title,
                            Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    } else {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(entry.category.icon, fontSize = 72.sp)
                        }
                    }
                    // Category badge
                    Surface(
                        modifier = Modifier.align(Alignment.BottomStart).padding(16.dp),
                        shape    = RoundedCornerShape(50),
                        color    = GoldPrimary
                    ) {
                        Text("${entry.category.icon} ${entry.category.displayName}",
                            Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = SurfaceDark, fontWeight = FontWeight.Bold)
                    }
                }

                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {

                    // Title block
                    Text(entry.title, style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold)
                    if (entry.titleAmharic.isNotBlank()) {
                        Text(entry.titleAmharic,
                            style = MaterialTheme.typography.titleMedium,
                            color = GoldPrimary.copy(0.8f))
                    }

                    // Info chips row
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        if (entry.distanceFromHotelKm > 0) {
                            InfoChip(Icons.Default.LocationOn, "${entry.distanceFromHotelKm} km away")
                        }
                        if (entry.openingHours.isNotBlank()) {
                            InfoChip(Icons.Default.Schedule, entry.openingHours)
                        }
                        if (entry.entryFee.isNotBlank()) {
                            InfoChip(Icons.Default.ConfirmationNumber, entry.entryFee)
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(0.2f))

                    // Summary
                    Text(entry.summary, style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(0.85f))

                    // Full content
                    if (entry.content.isNotBlank()) {
                        Text(entry.content,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(0.7f))
                    }

                    // Amharic content
                    if (entry.contentAmharic.isNotBlank()) {
                        Card(shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = TealDark.copy(0.2f))) {
                            Column(Modifier.padding(14.dp)) {
                                Text("አማርኛ", style = MaterialTheme.typography.labelMedium,
                                    color = TealLight, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(4.dp))
                                Text(entry.summaryAmharic,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(0.75f))
                            }
                        }
                    }

                    // Interactive Map
                    Text("Location", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Card(
                        modifier = Modifier.fillMaxWidth().height(200.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = CardDark)
                    ) {
                        val location = LatLng(entry.latitude, entry.longitude)
                        val cameraPositionState = rememberCameraPositionState {
                            position = CameraPosition.fromLatLngZoom(location, 15f)
                        }
                        
                        GoogleMap(
                            modifier = Modifier.fillMaxSize(),
                            cameraPositionState = cameraPositionState,
                            uiSettings = MapUiSettings(zoomControlsEnabled = false)
                        ) {
                            Marker(
                                state = MarkerState(position = location),
                                title = entry.title,
                                snippet = "Gondar, Ethiopia"
                            )
                        }
                    }
                    
                    Text("Lat ${entry.latitude}, Lon ${entry.longitude}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(0.5f),
                        modifier = Modifier.padding(top = 4.dp))

                    // Tags
                    if (entry.tags.isNotEmpty()) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            entry.tags.forEach { tag ->
                                Surface(shape = RoundedCornerShape(50),
                                    color = GoldDark.copy(alpha = 0.15f)) {
                                    Text("#$tag",
                                        Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelSmall, color = GoldPrimary)
                                }
                            }
                        }
                    }

                    // Offline indicator
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.OfflinePin, null,
                            Modifier.size(14.dp), tint = SuccessGreen)
                        Text("Available offline", style = MaterialTheme.typography.labelSmall,
                            color = SuccessGreen)
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoChip(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Surface(shape = RoundedCornerShape(50), color = CardDark,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(0.2f))) {
        Row(
            Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(icon, null, Modifier.size(12.dp), tint = GoldPrimary)
            Text(text, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(0.75f))
        }
    }
}
