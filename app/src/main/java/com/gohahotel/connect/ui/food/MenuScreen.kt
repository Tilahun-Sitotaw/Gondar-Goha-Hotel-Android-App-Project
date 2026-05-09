package com.gohahotel.connect.ui.food

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.gohahotel.connect.domain.model.MenuCategory
import com.gohahotel.connect.domain.model.MenuItem
import com.gohahotel.connect.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(
    viewModel: FoodViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onViewCart: () -> Unit,
    onDishClick: (String) -> Unit = {}
) {
    val uiState   by viewModel.uiState.collectAsState()
    val cartCount  = viewModel.cartCount
    val cartTotal  = viewModel.cartTotal
    val categories = listOf(null) + MenuCategory.entries.toList()

    Scaffold(
        containerColor = Color(0xFF050D18),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Restaurant",
                            fontWeight = FontWeight.ExtraBold,
                            color = GoldPrimary, fontSize = 18.sp)
                        Text("Goha Hotel · Gondar",
                            style = MaterialTheme.typography.labelSmall,
                            color = OnSurfaceDark.copy(0.5f))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = GoldPrimary)
                    }
                },
                actions = {
                    if (cartCount > 0) {
                        BadgedBox(badge = {
                            Badge(containerColor = GoldPrimary) {
                                Text("$cartCount", color = Color(0xFF050D18),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold)
                            }
                        }) {
                            IconButton(onClick = onViewCart) {
                                Icon(Icons.Default.ShoppingCart, null, tint = GoldPrimary)
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF050D18))
            )
        },
        // Floating cart bar
        bottomBar = {
            AnimatedVisibility(
                visible = cartCount > 0,
                enter = slideInVertically { it } + fadeIn(),
                exit  = slideOutVertically { it } + fadeOut()
            ) {
                Surface(
                    color = Color(0xFF0A1424),
                    border = BorderStroke(1.dp, GoldPrimary.copy(0.25f)),
                    shape  = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                    shadowElevation = 12.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("$cartCount item${if (cartCount > 1) "s" else ""} in cart",
                                style = MaterialTheme.typography.labelSmall,
                                color = OnSurfaceDark.copy(0.6f))
                            Text("ETB ${cartTotal.toInt()}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold, color = GoldPrimary)
                        }
                        Button(
                            onClick = onViewCart,
                            shape  = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
                        ) {
                            Icon(Icons.Default.ShoppingCart, null,
                                tint = Color(0xFF050D18), modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("View Order",
                                color = Color(0xFF050D18), fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Brush.verticalGradient(listOf(Color(0xFF050D18), Color(0xFF040C14))))
        ) {
            // ── Search bar ────────────────────────────────────────────────────
            Surface(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF0E1B2A),
                border = BorderStroke(1.dp, Color.White.copy(0.08f))
            ) {
                TextField(
                    value = uiState.searchQuery,
                    onValueChange = viewModel::search,
                    placeholder = {
                        Text("Search dishes, cuisine...",
                            color = OnSurfaceDark.copy(0.35f),
                            style = MaterialTheme.typography.bodyMedium)
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Search, null,
                            tint = GoldPrimary.copy(0.6f), modifier = Modifier.size(18.dp))
                    },
                    trailingIcon = if (uiState.searchQuery.isNotBlank()) {
                        {
                            IconButton(onClick = { viewModel.search("") }) {
                                Icon(Icons.Default.Clear, null,
                                    tint = OnSurfaceDark.copy(0.4f), modifier = Modifier.size(16.dp))
                            }
                        }
                    } else null,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor   = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor   = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor             = GoldPrimary,
                        focusedTextColor        = OnSurfaceDark,
                        unfocusedTextColor      = OnSurfaceDark
                    )
                )
            }

            // ── Category chips ────────────────────────────────────────────────
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { cat ->
                    val isSelected = uiState.selectedCategory == cat
                    Surface(
                        onClick = { viewModel.selectCategory(cat) },
                        shape = RoundedCornerShape(50),
                        color = if (isSelected) GoldPrimary else Color(0xFF0E1B2A),
                        border = BorderStroke(1.dp,
                            if (isSelected) GoldPrimary else Color.White.copy(0.1f))
                    ) {
                        Text(
                            text = cat?.displayName ?: "All",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.labelLarge,
                            color = if (isSelected) Color(0xFF050D18) else OnSurfaceDark.copy(0.7f),
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(Modifier.height(6.dp))

            // ── Menu content ──────────────────────────────────────────────────
            if (uiState.isLoading && uiState.menuItems.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GoldPrimary)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Chef's recommendations (featured)
                    if (uiState.featuredItems.isNotEmpty() &&
                        uiState.searchQuery.isBlank() &&
                        uiState.selectedCategory == null
                    ) {
                        item {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.Star, null,
                                    tint = GoldPrimary, modifier = Modifier.size(16.dp))
                                Text("Chef's Recommendations",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold, color = GoldPrimary)
                            }
                            Spacer(Modifier.height(10.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                items(uiState.featuredItems, key = { "feat_${it.id}" }) { item ->
                                    FeaturedDishCard(
                                        item = item,
                                        cartQty = uiState.cartItems
                                            .firstOrNull { it.menuItem.id == item.id }?.quantity ?: 0,
                                        onAdd    = { viewModel.addToCart(item) },
                                        onRemove = { viewModel.removeFromCart(item) }
                                    )
                                }
                            }
                            Spacer(Modifier.height(16.dp))
                            HorizontalDivider(color = Color.White.copy(0.06f))
                            Spacer(Modifier.height(8.dp))
                            Text("Full Menu",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold, color = OnSurfaceDark)
                        }
                    }

                    // Main menu items
                    items(uiState.menuItems, key = { it.id }) { item ->
                        DishCard(
                            item    = item,
                            cartQty = uiState.cartItems
                                .firstOrNull { it.menuItem.id == item.id }?.quantity ?: 0,
                            onAdd    = { viewModel.addToCart(item) },
                            onRemove = { viewModel.removeFromCart(item) },
                            onClick  = { onDishClick(item.id) }
                        )
                    }

                    if (uiState.menuItems.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier.fillParentMaxWidth().height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("🍽️", fontSize = 40.sp)
                                    Spacer(Modifier.height(12.dp))
                                    Text(
                                        if (uiState.searchQuery.isNotBlank())
                                            "No dishes match \"${uiState.searchQuery}\""
                                        else "No dishes available yet",
                                        color = OnSurfaceDark.copy(0.4f),
                                        textAlign = TextAlign.Center,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }

                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }
}

// ── Featured dish card (horizontal scroll) ────────────────────────────────────
@Composable
private fun FeaturedDishCard(
    item: MenuItem,
    cartQty: Int,
    onAdd: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.width(150.dp),
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(containerColor = Color(0xFF0E1B2A)),
        border   = BorderStroke(1.dp, GoldPrimary.copy(0.15f))
    ) {
        Column {
            Box(
                modifier = Modifier.fillMaxWidth().height(110.dp)
                    .background(Color(0xFF0A1424))
            ) {
                val imageUrl = item.allImages.firstOrNull() ?: ""
                if (imageUrl.isNotBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(imageUrl).crossfade(true).build(),
                        contentDescription = item.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("🍛", fontSize = 30.sp)
                    }
                }
                // Diet tags
                if (item.isVegetarian || item.isVegan) {
                    Surface(
                        modifier = Modifier.align(Alignment.TopStart).padding(5.dp),
                        shape = RoundedCornerShape(5.dp),
                        color = Color(0xFF50C878).copy(0.9f)
                    ) {
                        Text(if (item.isVegan) "🌱" else "🌿",
                            modifier = Modifier.padding(3.dp), fontSize = 10.sp)
                    }
                }
                if (item.isSpicy) {
                    Surface(
                        modifier = Modifier.align(Alignment.TopEnd).padding(5.dp),
                        shape = RoundedCornerShape(5.dp),
                        color = Color(0xFFE24A4A).copy(0.9f)
                    ) {
                        Text("🌶️", modifier = Modifier.padding(3.dp), fontSize = 10.sp)
                    }
                }
            }
            Column(modifier = Modifier.padding(10.dp)) {
                Text(item.name,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = OnSurfaceDark,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis)
                Text("ETB ${item.price.toInt()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = GoldPrimary, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                // Quantity control or Add button
                if (cartQty > 0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(onClick = onRemove, modifier = Modifier.size(26.dp)
                            .background(Color.White.copy(0.08f), CircleShape)) {
                            Icon(Icons.Default.Remove, null,
                                tint = GoldPrimary, modifier = Modifier.size(14.dp))
                        }
                        Text("$cartQty",
                            fontWeight = FontWeight.ExtraBold,
                            color = GoldPrimary, fontSize = 14.sp)
                        IconButton(onClick = onAdd, modifier = Modifier.size(26.dp)
                            .background(GoldPrimary, CircleShape)) {
                            Icon(Icons.Default.Add, null,
                                tint = Color(0xFF050D18), modifier = Modifier.size(14.dp))
                        }
                    }
                } else {
                    Button(
                        onClick = onAdd,
                        modifier = Modifier.fillMaxWidth().height(30.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(Icons.Default.Add, null,
                            tint = Color(0xFF050D18), modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Add", color = Color(0xFF050D18),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ── Main dish card (vertical list) ───────────────────────────────────────────
@Composable
private fun DishCard(
    item: MenuItem,
    cartQty: Int,
    onAdd: () -> Unit,
    onRemove: () -> Unit,
    onClick: () -> Unit = {}
) {
    val isInCart = cartQty > 0
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(16.dp),
        color    = if (isInCart) Color(0xFF0E1B2A) else Color(0xFF0A1424),
        border   = BorderStroke(
            width = if (isInCart) 1.5.dp else 1.dp,
            color = if (isInCart) GoldPrimary.copy(0.4f) else Color.White.copy(0.06f)
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Dish image
            Box(
                modifier = Modifier
                    .size(82.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0E1B2A))
            ) {
                val imageUrl = item.allImages.firstOrNull() ?: ""
                if (imageUrl.isNotBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(imageUrl).crossfade(true).build(),
                        contentDescription = item.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("🍽️", fontSize = 26.sp)
                    }
                }
                // In-cart indicator
                if (isInCart) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .size(18.dp)
                            .background(GoldPrimary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("$cartQty",
                            color = Color(0xFF050D18),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold)
                    }
                }
            }

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Text(item.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = OnSurfaceDark,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis)

                // Diet tags row
                if (item.isVegetarian || item.isVegan || item.isSpicy || item.isGlutenFree) {
                    Spacer(Modifier.height(3.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        if (item.isVegan)      DietBadge("🌱 Vegan",  Color(0xFF2E7D32))
                        else if (item.isVegetarian) DietBadge("🌿 Veg", Color(0xFF50C878))
                        if (item.isSpicy)      DietBadge("🌶 Spicy",  Color(0xFFE24A4A))
                        if (item.isGlutenFree) DietBadge("GF",        Color(0xFFE29B4A))
                    }
                }

                Spacer(Modifier.height(3.dp))
                if (item.description.isNotBlank()) {
                    Text(
                        item.description.take(55) + if (item.description.length > 55) "…" else "",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceDark.copy(0.45f),
                        maxLines = 2
                    )
                }
                Spacer(Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text("ETB ${item.price.toInt()}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold, color = GoldPrimary)
                        if (item.prepTimeMinutes > 0) {
                            Text("~${item.prepTimeMinutes} min",
                                style = MaterialTheme.typography.labelSmall,
                                color = OnSurfaceDark.copy(0.4f))
                        }
                    }

                    // Quantity controls
                    if (isInCart) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IconButton(
                                onClick  = onRemove,
                                modifier = Modifier.size(30.dp)
                                    .background(Color.White.copy(0.08f), CircleShape)
                            ) {
                                Icon(Icons.Default.Remove, null,
                                    tint = GoldPrimary, modifier = Modifier.size(14.dp))
                            }
                            Text("$cartQty",
                                fontWeight = FontWeight.ExtraBold,
                                color = GoldPrimary, fontSize = 15.sp,
                                modifier = Modifier.widthIn(min = 20.dp),
                                textAlign = TextAlign.Center)
                            IconButton(
                                onClick  = onAdd,
                                modifier = Modifier.size(30.dp)
                                    .background(GoldPrimary, CircleShape)
                            ) {
                                Icon(Icons.Default.Add, null,
                                    tint = Color(0xFF050D18), modifier = Modifier.size(14.dp))
                            }
                        }
                    } else {
                        IconButton(
                            onClick  = onAdd,
                            modifier = Modifier.size(34.dp)
                                .background(GoldPrimary, CircleShape)
                        ) {
                            Icon(Icons.Default.Add, null,
                                tint = Color(0xFF050D18), modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DietBadge(label: String, color: Color) {
    Surface(shape = RoundedCornerShape(4.dp), color = color.copy(0.15f)) {
        Text(label,
            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color, fontSize = 9.sp)
    }
}
