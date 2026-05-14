package com.gohahotel.connect.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.gohahotel.connect.domain.model.Promotion
import com.gohahotel.connect.ui.theme.GoldPrimary
import com.gohahotel.connect.ui.theme.OnSurfaceDark
import com.gohahotel.connect.ui.theme.SurfaceDark
import kotlinx.coroutines.delay

/**
 * Auto-scrolling carousel for events and highlights
 * Automatically scrolls horizontally with smooth animation
 */
@Composable
fun AutoScrollingCarousel(
    items: List<Promotion>,
    modifier: Modifier = Modifier,
    onItemClick: (String) -> Unit = {},
    autoScrollDurationMillis: Int = 4000,
    scrollDelayMillis: Int = 500
) {
    if (items.isEmpty()) return

    val listState = rememberLazyListState()
    var currentIndex by remember { mutableIntStateOf(0) }

    // Auto-scroll effect
    LaunchedEffect(items.size) {
        if (items.size <= 1) return@LaunchedEffect
        
        while (true) {
            delay(autoScrollDurationMillis.toLong())
            currentIndex = (currentIndex + 1) % items.size
            
            try {
                listState.animateScrollToItem(currentIndex)
            } catch (e: Exception) {
                // Handle edge cases
            }
        }
    }

    Box(modifier = modifier) {
        LazyRow(
            state = listState,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items, key = { it.id }) { promotion ->
                EventCard(
                    promotion = promotion,
                    onClick = { onItemClick(promotion.id) }
                )
            }
        }

        // Scroll indicator dots
        if (items.size > 1) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                repeat(items.size) { index ->
                    Box(
                        modifier = Modifier
                            .size(if (index == currentIndex) 8.dp else 6.dp)
                            .background(
                                color = if (index == currentIndex) GoldPrimary else GoldPrimary.copy(0.3f),
                                shape = RoundedCornerShape(50)
                            )
                    )
                }
            }
        }
    }
}

/**
 * Individual event card for carousel
 */
@Composable
private fun EventCard(
    promotion: Promotion,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .width(280.dp)
            .height(160.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0E1B2A)),
        onClick = onClick
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background image
            if (promotion.imageUrl.isNotBlank()) {
                AsyncImage(
                    model = promotion.imageUrl,
                    contentDescription = promotion.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                // Overlay gradient
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.Transparent,
                                    Color.Black.copy(0.7f)
                                )
                            )
                        )
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF1A3A52), Color(0xFF0E1B2A))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🎉", fontSize = 40.sp)
                }
            }

            // Content
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Title
                Text(
                    promotion.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Description
                Text(
                    promotion.description,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(0.85f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Event type badge
                Surface(
                    modifier = Modifier.width(IntrinsicSize.Max),
                    shape = RoundedCornerShape(6.dp),
                    color = GoldPrimary
                ) {
                    Text(
                        promotion.type.name,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = SurfaceDark,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}
