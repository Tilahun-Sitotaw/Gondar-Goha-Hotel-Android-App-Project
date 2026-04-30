package com.gohahotel.connect.ui.guide

import androidx.compose.animation.*
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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
    val categories = listOf(null) + GuideCategory.entries.toList()

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ── Modern Parallax-style Header ──────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(Brush.verticalGradient(listOf(TealDark, SurfaceDark)))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                        .padding(top = 20.dp),
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onBack, modifier = Modifier.offset(x = (-12).dp)) {
                            Icon(Icons.Default.ArrowBack, "Back", tint = GoldPrimary)
                        }
                        Text(
                            "Cultural Guide",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = GoldPrimary
                        )
                    }
                    Text(
                        "Discover the wonders of Gondar",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceDark.copy(alpha = 0.6f)
                    )
                }
            }

            // ── Minimal Search & Filter ───────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-25).dp)
                    .padding(horizontal = 16.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    shadowElevation = 4.dp
                ) {
                    TextField(
                        value = uiState.searchQuery,
                        onValueChange = viewModel::search,
                        placeholder = { Text("Search historic sites, food...") },
                        leadingIcon = { Icon(Icons.Default.Search, null, tint = GoldPrimary) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        singleLine = true
                    )
                }

                Spacer(Modifier.height(16.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    items(categories) { cat ->
                        val isSelected = uiState.selectedCategory == cat
                        Surface(
                            onClick = { viewModel.selectCategory(cat) },
                            shape = RoundedCornerShape(50),
                            color = if (isSelected) GoldPrimary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = if (!isSelected) BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.2f)) else null
                        ) {
                            Text(
                                text = if (cat == null) "All" else "${cat.icon} ${cat.displayName}",
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                style = MaterialTheme.typography.labelLarge,
                                color = if (isSelected) SurfaceDark else MaterialTheme.colorScheme.onSurface,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // ── Content ──────────────────────────────────────────────────────────
            LazyColumn(
                modifier = Modifier.fillMaxSize().offset(y = (-10).dp),
                contentPadding = PaddingValues(bottom = 24.dp, start = 16.dp, end = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (uiState.nearbyEntries.isNotEmpty() && uiState.searchQuery.isBlank()) {
                    item {
                        Text(
                            "📍 Featured Nearby",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = GoldPrimary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            items(uiState.nearbyEntries) { entry ->
                                FeaturedGuideCard(entry = entry, onClick = { onEntryClick(entry.id) })
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "All Attractions",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }

                items(uiState.entries) { entry ->
                    ModernGuideCard(entry = entry, onClick = { onEntryClick(entry.id) })
                }
            }
        }
    }
}

@Composable
private fun FeaturedGuideCard(entry: GuideEntry, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.width(220.dp).height(280.dp),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (entry.imageUrls.isNotEmpty()) {
                AsyncImage(
                    model = entry.imageUrls.first(),
                    contentDescription = entry.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(Modifier.fillMaxSize().background(SurfaceVariantDark), contentAlignment = Alignment.Center) {
                    Text(entry.category.icon, fontSize = 64.sp)
                }
            }
            // Gradient Overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)),
                            startY = 400f
                        )
                    )
            )
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.Bottom
            ) {
                Text(
                    entry.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, null, tint = GoldPrimary, modifier = Modifier.size(12.dp))
                    Text(
                        "${entry.distanceFromHotelKm} km",
                        style = MaterialTheme.typography.labelSmall,
                        color = GoldPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun ModernGuideCard(entry: GuideEntry, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceVariantDark)
            ) {
                if (entry.imageUrls.isNotEmpty()) {
                    AsyncImage(
                        model = entry.imageUrls.first(),
                        contentDescription = entry.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(entry.category.icon, fontSize = 32.sp)
                    }
                }
            }
            
            Spacer(Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        entry.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Text(entry.category.icon, modifier = Modifier.padding(start = 4.dp))
                }
                
                Text(
                    entry.summary,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(Modifier.height(8.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, null, tint = GoldPrimary, modifier = Modifier.size(14.dp))
                    Text(
                        "${entry.distanceFromHotelKm} km · ",
                        style = MaterialTheme.typography.labelMedium,
                        color = GoldPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        entry.category.displayName,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}
