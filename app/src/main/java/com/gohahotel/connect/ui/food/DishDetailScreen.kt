package com.gohahotel.connect.ui.food

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.gohahotel.connect.domain.model.MenuItem
import com.gohahotel.connect.ui.theme.*

// ─── Background / card / border constants ─────────────────────────────────────
private val BgDark     = Color(0xFF050D18)
private val CardBg     = Color(0xFF0E1B2A)
private val BorderColor = Color.White.copy(alpha = 0.08f)

@Composable
fun DishDetailScreen(
    menuItemId: String,
    viewModel: FoodViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onViewCart: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    // Guard: if menuItemId is blank, go back immediately
    if (menuItemId.isBlank()) {
        LaunchedEffect(Unit) { onBack() }
        return
    }

    // Resolve item from either list
    val item: MenuItem? = remember(menuItemId, uiState.menuItems, uiState.featuredItems) {
        uiState.menuItems.firstOrNull { it.id == menuItemId }
            ?: uiState.featuredItems.firstOrNull { it.id == menuItemId }
    }

    // If menu is done loading and item still not found, go back
    LaunchedEffect(uiState.isLoading, item) {
        if (!uiState.isLoading && item == null &&
            (uiState.menuItems.isNotEmpty() || uiState.featuredItems.isNotEmpty())) {
            onBack()
        }
    }

    // Cart quantity already in cart for this item
    val cartQty = uiState.cartItems.firstOrNull { it.menuItem.id == item?.id }?.quantity ?: 0

    // Local quantity selector state — initialise to max(1, cartQty) once item loads
    var qty by remember(item?.id) { mutableIntStateOf(if (cartQty > 0) cartQty else 1) }

    // Current image page for dot indicator
    var currentImageIndex by remember { mutableIntStateOf(0) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
    ) {
        if (item == null) {
            // ── Loading state ──────────────────────────────────────────────────
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = GoldPrimary)
            }
            // Back button still accessible while loading
            BackButton(onBack = onBack)
        } else {
            val allImages = item.allImages

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 88.dp) // space for sticky bottom bar
            ) {
                // ── Hero image ─────────────────────────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                ) {
                    if (allImages.isNotEmpty()) {
                        val imageUrl = allImages.getOrElse(currentImageIndex) { allImages.first() }
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(imageUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = item.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        // Fallback: emoji on dark background
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(CardBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "🍽️",
                                fontSize = 72.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // Gradient overlay at bottom of hero
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .align(Alignment.BottomCenter)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, BgDark)
                                )
                            )
                    )

                    // Image swipe dots — only if multiple images
                    if (allImages.size > 1) {
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            allImages.forEachIndexed { index, _ ->
                                Box(
                                    modifier = Modifier
                                        .size(if (index == currentImageIndex) 8.dp else 6.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (index == currentImageIndex) GoldPrimary
                                            else Color.White.copy(alpha = 0.4f)
                                        )
                                        .clickable { currentImageIndex = index }
                                )
                            }
                        }
                    }

                    // Back button overlay
                    BackButton(onBack = onBack)
                }

                // ── Content section ────────────────────────────────────────────
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Category chip + name row
                    SuggestionChip(
                        onClick = {},
                        label = {
                            Text(
                                text = item.category.displayName,
                                color = GoldPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        },
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.5f)),
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = GoldPrimary.copy(alpha = 0.12f)
                        )
                    )

                    // Dish name
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = OnSurfaceDark
                    )

                    // Price
                    Text(
                        text = "${item.currency} ${String.format("%.2f", item.price)}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = GoldPrimary
                    )

                    // ── Diet badges ───────────────────────────────────────────
                    val hasBadges = item.isVegan || item.isVegetarian || item.isSpicy || item.isGlutenFree
                    if (hasBadges) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (item.isVegan) {
                                DietBadge(
                                    label = "🌱 Vegan",
                                    bgColor = Color(0xFF1B5E20).copy(alpha = 0.3f),
                                    borderColor = Color(0xFF4CAF50).copy(alpha = 0.6f),
                                    textColor = Color(0xFF81C784)
                                )
                            }
                            if (item.isVegetarian && !item.isVegan) {
                                DietBadge(
                                    label = "🌿 Vegetarian",
                                    bgColor = Color(0xFF33691E).copy(alpha = 0.3f),
                                    borderColor = Color(0xFF8BC34A).copy(alpha = 0.6f),
                                    textColor = Color(0xFFAED581)
                                )
                            }
                            if (item.isSpicy) {
                                DietBadge(
                                    label = "🌶 Spicy",
                                    bgColor = Color(0xFFB71C1C).copy(alpha = 0.3f),
                                    borderColor = Color(0xFFEF5350).copy(alpha = 0.6f),
                                    textColor = Color(0xFFEF9A9A)
                                )
                            }
                            if (item.isGlutenFree) {
                                DietBadge(
                                    label = "GF",
                                    bgColor = Color(0xFFFF6F00).copy(alpha = 0.2f),
                                    borderColor = WarningAmber.copy(alpha = 0.6f),
                                    textColor = WarningAmber
                                )
                            }
                        }
                    }

                    // ── Prep time ─────────────────────────────────────────────
                    if (item.prepTimeMinutes > 0) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Schedule,
                                contentDescription = "Prep time",
                                tint = OnSurfaceDark.copy(alpha = 0.55f),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "~${item.prepTimeMinutes} min",
                                style = MaterialTheme.typography.bodySmall,
                                color = OnSurfaceDark.copy(alpha = 0.55f)
                            )
                        }
                    }

                    // ── Description ───────────────────────────────────────────
                    if (item.description.isNotBlank()) {
                        HorizontalDivider(color = BorderColor, thickness = 1.dp)
                        Text(
                            text = item.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = OnSurfaceDark.copy(alpha = 0.75f),
                            lineHeight = 22.sp
                        )
                    }

                    // ── Quantity selector ─────────────────────────────────────
                    HorizontalDivider(color = BorderColor, thickness = 1.dp)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Quantity",
                            style = MaterialTheme.typography.titleMedium,
                            color = OnSurfaceDark
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Minus button
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.08f))
                                    .clickable(enabled = qty > 1) { qty-- },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Remove,
                                    contentDescription = "Decrease quantity",
                                    tint = if (qty > 1) OnSurfaceDark else OnSurfaceDark.copy(alpha = 0.3f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // Count
                            Text(
                                text = qty.toString(),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = GoldPrimary,
                                modifier = Modifier.widthIn(min = 28.dp),
                                textAlign = TextAlign.Center
                            )

                            // Plus button
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(GoldPrimary)
                                    .clickable { qty++ },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Add,
                                    contentDescription = "Increase quantity",
                                    tint = Color(0xFF050D18),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            // ── Sticky bottom bar ──────────────────────────────────────────────
            val total = item.price * qty
            val alreadyInCart = cartQty > 0

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter),
                color = CardBg,
                shadowElevation = 8.dp,
                tonalElevation = 0.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Total price label
                    Column {
                        Text(
                            text = "Total",
                            style = MaterialTheme.typography.labelSmall,
                            color = OnSurfaceDark.copy(alpha = 0.55f)
                        )
                        Text(
                            text = "ETB ${String.format("%.2f", total)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = OnSurfaceDark
                        )
                    }

                    // Add / Update button
                    Button(
                        onClick = {
                            val currentItem = item ?: return@Button
                            // Safe cart update: remove existing then add desired qty
                            val existingQty = uiState.cartItems
                                .firstOrNull { it.menuItem.id == currentItem.id }?.quantity ?: 0
                            // Remove all existing
                            repeat(existingQty) { viewModel.removeFromCart(currentItem) }
                            // Add desired qty
                            repeat(qty) { viewModel.addToCart(currentItem) }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GoldPrimary,
                            contentColor = Color(0xFF050D18)
                        )
                    ) {
                        Icon(
                            imageVector = if (alreadyInCart) Icons.Filled.Edit else Icons.Filled.ShoppingCart,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (alreadyInCart) "Update Order" else "Add to Order",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    }
}

// ── Back button overlay ────────────────────────────────────────────────────────
@Composable
private fun BackButton(onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .statusBarsPadding()
            .padding(start = 12.dp, top = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.45f))
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

// ── Diet badge chip ────────────────────────────────────────────────────────────
@Composable
private fun DietBadge(
    label: String,
    bgColor: Color,
    borderColor: Color,
    textColor: Color
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = textColor
        )
    }
}
