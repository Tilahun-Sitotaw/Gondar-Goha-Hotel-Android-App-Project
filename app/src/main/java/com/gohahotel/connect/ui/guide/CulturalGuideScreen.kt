package com.gohahotel.connect.ui.guide

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
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
    val uiState      by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current
    val categories   = listOf(null) + GuideCategory.entries.toList()

    Scaffold(
        containerColor = SurfaceDark,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Cultural Guide", color = OnSurfaceDark,
                            fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                        Text("Discover the wonders of Gondar",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF9B59B6).copy(0.7f))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = GoldPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceDark)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(SurfaceDark, Color(0xFF0D0A1A), Color(0xFF050D18))))
                .padding(padding)
        ) {
            // ── Search bar ────────────────────────────────────────────────────
            Surface(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF0E1B2A),
                border = BorderStroke(1.dp, Color.White.copy(0.08f))
            ) {
                TextField(
                    value = uiState.searchQuery,
                    onValueChange = viewModel::search,
                    placeholder = {
                        Text("Search historic sites, culture...",
                            color = OnSurfaceDark.copy(0.35f))
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Search, null,
                            tint = Color(0xFF9B59B6).copy(0.7f), modifier = Modifier.size(20.dp))
                    },
                    trailingIcon = if (uiState.searchQuery.isNotEmpty()) {
                        {
                            IconButton(onClick = { viewModel.search(""); focusManager.clearFocus() }) {
                                Icon(Icons.Default.Close, null,
                                    tint = OnSurfaceDark.copy(0.5f), modifier = Modifier.size(18.dp))
                            }
                        }
                    } else null,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor   = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor   = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor             = Color(0xFF9B59B6),
                        focusedTextColor        = OnSurfaceDark,
                        unfocusedTextColor      = OnSurfaceDark
                    )
                )
            }

            // ── Category filter chips ─────────────────────────────────────────
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { cat ->
                    val isSelected = uiState.selectedCategory == cat
                    val accent = if (cat == null) Color(0xFF9B59B6) else guideCategoryColor(cat)
                    Surface(
                        onClick = { viewModel.selectCategory(cat) },
                        shape = RoundedCornerShape(50),
                        color = if (isSelected) accent.copy(0.2f) else Color.White.copy(0.05f),
                        border = BorderStroke(1.dp, if (isSelected) accent else Color.White.copy(0.1f))
                    ) {
                        Text(
                            text = if (cat == null) "🗺️ All" else "${cat.icon} ${cat.displayName}",
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.labelLarge,
                            color = if (isSelected) accent else OnSurfaceDark.copy(0.6f),
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            // ── Content ───────────────────────────────────────────────────────
            when {
                uiState.isLoading && uiState.entries.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF9B59B6))
                    }
                }
                uiState.entries.isEmpty() && uiState.nearbyEntries.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🏛️", fontSize = 48.sp)
                            Spacer(Modifier.height(12.dp))
                            Text(
                                if (uiState.searchQuery.isNotBlank())
                                    "No results for \"${uiState.searchQuery}\""
                                else "No cultural entries yet.\nAdmin can add them from the dashboard.",
                                color = OnSurfaceDark.copy(0.4f),
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 32.dp)
                            )
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Featured nearby section (only when no search/filter active)
                        if (uiState.nearbyEntries.isNotEmpty() &&
                            uiState.searchQuery.isBlank() &&
                            uiState.selectedCategory == null
                        ) {
                            item {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.LocationOn, null,
                                        tint = GoldPrimary, modifier = Modifier.size(16.dp))
                                    Text("📍 Featured Nearby",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = GoldPrimary)
                                }
                                Spacer(Modifier.height(10.dp))
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                                    items(uiState.nearbyEntries, key = { "nearby_${it.id}" }) { entry ->
                                        FeaturedGuideCard(
                                            entry   = entry,
                                            onClick = { onEntryClick(entry.id) }
                                        )
                                    }
                                }
                                Spacer(Modifier.height(8.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.Map, null,
                                        tint = OnSurfaceDark, modifier = Modifier.size(16.dp))
                                    Text("All Attractions",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = OnSurfaceDark)
                                }
                            }
                        }

                        // Main list — all from Firestore, no hardcoded data
                        items(uiState.entries, key = { it.id }) { entry ->
                            GuideEntryCard(
                                entry   = entry,
                                onClick = { onEntryClick(entry.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Featured card (horizontal scroll) ────────────────────────────────────────
@Composable
private fun FeaturedGuideCard(entry: GuideEntry, onClick: () -> Unit) {
    val accent = guideCategoryColor(entry.category)
    Card(
        onClick   = onClick,
        modifier  = Modifier.width(200.dp).height(260.dp),
        shape     = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            val imageUrl = entry.allImages.firstOrNull() ?: ""
            if (imageUrl.isNotEmpty()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageUrl).crossfade(true).build(),
                    contentDescription = entry.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    Modifier.fillMaxSize().background(
                        Brush.verticalGradient(listOf(accent.copy(0.2f), SurfaceDark))
                    ),
                    contentAlignment = Alignment.Center
                ) { Text(entry.category.icon, fontSize = 56.sp) }
            }
            // Gradient overlay
            Box(
                modifier = Modifier.fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black.copy(0.8f)),
                            startY = 300f
                        )
                    )
            )
            Column(
                modifier = Modifier.fillMaxSize().padding(14.dp),
                verticalArrangement = Arrangement.Bottom
            ) {
                Surface(shape = RoundedCornerShape(6.dp), color = accent.copy(0.85f)) {
                    Text("${entry.category.icon} ${entry.category.displayName}",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Black, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(6.dp))
                Text(entry.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis)
                if (entry.distanceFromHotelKm > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        Icon(Icons.Default.LocationOn, null,
                            tint = GoldPrimary, modifier = Modifier.size(11.dp))
                        Text("${entry.distanceFromHotelKm} km",
                            style = MaterialTheme.typography.labelSmall,
                            color = GoldPrimary)
                    }
                }
            }
        }
    }
}

// ── List card ─────────────────────────────────────────────────────────────────
@Composable
private fun GuideEntryCard(entry: GuideEntry, onClick: () -> Unit) {
    val accent = guideCategoryColor(entry.category)
    Surface(
        onClick  = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(18.dp),
        color    = Color(0xFF0E1B2A),
        border   = BorderStroke(1.dp, accent.copy(0.2f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Thumbnail
            Box(
                modifier = Modifier.size(86.dp).clip(RoundedCornerShape(14.dp))
                    .background(accent.copy(0.1f))
            ) {
                val imageUrl = entry.allImages.firstOrNull() ?: ""
                if (imageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(imageUrl).crossfade(true).build(),
                        contentDescription = entry.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(entry.category.icon, fontSize = 30.sp)
                    }
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                // Category badge
                Surface(shape = RoundedCornerShape(6.dp), color = accent.copy(0.15f)) {
                    Text("${entry.category.icon} ${entry.category.displayName}",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = accent, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(5.dp))
                Text(entry.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = OnSurfaceDark,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis)
                if (entry.summary.isNotBlank()) {
                    Spacer(Modifier.height(3.dp))
                    Text(entry.summary,
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceDark.copy(0.5f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis)
                }
                Spacer(Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (entry.distanceFromHotelKm > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                            Icon(Icons.Default.LocationOn, null,
                                tint = GoldPrimary, modifier = Modifier.size(12.dp))
                            Text("${entry.distanceFromHotelKm} km",
                                style = MaterialTheme.typography.labelSmall,
                                color = GoldPrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                    if (entry.openingHours.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                            Icon(Icons.Default.Schedule, null,
                                tint = TealLight, modifier = Modifier.size(12.dp))
                            Text(entry.openingHours,
                                style = MaterialTheme.typography.labelSmall,
                                color = TealLight)
                        }
                    }
                }
            }

            Icon(Icons.Default.ChevronRight, null,
                tint = accent.copy(0.5f), modifier = Modifier.size(20.dp))
        }
    }
}

// ── Helper: category accent color ─────────────────────────────────────────────
fun guideCategoryColor(cat: GuideCategory): Color = when (cat) {
    GuideCategory.HERITAGE  -> Color(0xFFD4A843)
    GuideCategory.CHURCHES  -> Color(0xFF9B59B6)
    GuideCategory.MARKETS   -> Color(0xFFE29B4A)
    GuideCategory.NATURE    -> Color(0xFF50C878)
    GuideCategory.HISTORY   -> Color(0xFF4A90E2)
    GuideCategory.DINING    -> Color(0xFFE24A8A)
}
