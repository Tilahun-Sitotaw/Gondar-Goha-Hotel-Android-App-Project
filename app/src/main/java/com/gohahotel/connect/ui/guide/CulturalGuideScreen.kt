package com.gohahotel.connect.ui.guide

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.gohahotel.connect.domain.model.GuideCategory
import com.gohahotel.connect.domain.model.GuideEntry
import com.gohahotel.connect.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CulturalGuideScreen(
    viewModel: GuideViewModel = hiltViewModel(),
    onEntryClick: (String) -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val categories = listOf(null) + GuideCategory.values().toList()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Cultural Guide", fontWeight = FontWeight.Bold)
                        Text("Gondar & Surroundings · Works Offline",
                            style = MaterialTheme.typography.labelSmall,
                            color = SuccessGreen)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {

            // Search
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = viewModel::search,
                placeholder  = { Text("Search attractions...") },
                leadingIcon  = { Icon(Icons.Default.Search, null) },
                modifier     = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                shape        = RoundedCornerShape(14.dp),
                singleLine   = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = GoldPrimary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(0.4f)
                )
            )

            // Category filter
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { cat ->
                    FilterChip(
                        selected = uiState.selectedCategory == cat,
                        onClick  = { viewModel.selectCategory(cat) },
                        label    = { Text(if (cat == null) "All" else "${cat.icon} ${cat.displayName}") },
                        colors   = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GoldPrimary,
                            selectedLabelColor     = SurfaceDark
                        )
                    )
                }
            }

            Spacer(Modifier.height(4.dp))

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Nearby section
                if (uiState.nearbyEntries.isNotEmpty() && uiState.searchQuery.isBlank()) {
                    item {
                        Text("📍 Nearby Attractions",
                            style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(uiState.nearbyEntries) { entry ->
                                NearbyCard(entry = entry, onClick = { onEntryClick(entry.id) })
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(0.2f))
                        Spacer(Modifier.height(4.dp))
                        Text("All Attractions",
                            style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                }

                items(uiState.entries, key = { it.id }) { entry ->
                    GuideEntryCard(entry = entry, onClick = { onEntryClick(entry.id) })
                }

                if (uiState.entries.isEmpty() && !uiState.isLoading) {
                    item {
                        Box(Modifier.fillParentMaxWidth().height(200.dp),
                            contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("🗺️", fontSize = 48.sp)
                                Spacer(Modifier.height(8.dp))
                                Text("No attractions found",
                                    color = MaterialTheme.colorScheme.onBackground.copy(0.5f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NearbyCard(entry: GuideEntry, onClick: () -> Unit) {
    Card(
        onClick  = onClick,
        modifier = Modifier.width(160.dp),
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(containerColor = CardDark)
    ) {
        Column {
            Box(Modifier.fillMaxWidth().height(90.dp)
                .background(Brush.verticalGradient(listOf(TealDark, SurfaceVariantDark)))) {
                if (entry.imageUrls.isNotEmpty()) {
                    AsyncImage(entry.imageUrls.first(), entry.title,
                        Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                } else {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(entry.category.icon, fontSize = 32.sp)
                    }
                }
            }
            Column(Modifier.padding(10.dp)) {
                Text(entry.title, style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold, maxLines = 2)
                Spacer(Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.LocationOn, null, Modifier.size(12.dp), tint = GoldPrimary)
                    Text("${entry.distanceFromHotelKm} km",
                        style = MaterialTheme.typography.labelSmall, color = GoldPrimary)
                }
            }
        }
    }
}

@Composable
private fun GuideEntryCard(entry: GuideEntry, onClick: () -> Unit) {
    Card(
        onClick  = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(18.dp),
        colors   = CardDefaults.cardColors(containerColor = CardDark)
    ) {
        Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Box(Modifier.size(80.dp).clip(RoundedCornerShape(12.dp))
                .background(SurfaceVariantDark)) {
                if (entry.imageUrls.isNotEmpty()) {
                    AsyncImage(entry.imageUrls.first(), entry.title,
                        Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                } else {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(entry.category.icon, fontSize = 32.sp)
                    }
                }
            }
            Column(Modifier.weight(1f)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(entry.title, style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    Surface(shape = RoundedCornerShape(50),
                        color = GoldDark.copy(alpha = 0.2f)) {
                        Text(entry.category.icon + " " + entry.category.displayName.split(" ").first(),
                            Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall, color = GoldPrimary)
                    }
                }
                Spacer(Modifier.height(3.dp))
                Text(entry.summary.take(80) + if (entry.summary.length > 80) "…" else "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(0.65f), maxLines = 2)
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (entry.distanceFromHotelKm > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                            Icon(Icons.Default.LocationOn, null, Modifier.size(12.dp), tint = GoldPrimary)
                            Text("${entry.distanceFromHotelKm} km",
                                style = MaterialTheme.typography.labelSmall, color = GoldPrimary)
                        }
                    }
                    if (entry.openingHours.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                            Icon(Icons.Default.Schedule, null, Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.onSurface.copy(0.4f))
                            Text(entry.openingHours.take(16),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
                        }
                    }
                }
            }
        }
    }
}
