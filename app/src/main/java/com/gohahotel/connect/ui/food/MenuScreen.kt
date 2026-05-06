package com.gohahotel.connect.ui.food

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
import androidx.compose.ui.text.style.TextAlign
import com.gohahotel.connect.domain.model.MenuCategory
import com.gohahotel.connect.domain.model.MenuItem
import com.gohahotel.connect.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(
    viewModel: FoodViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onViewCart: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val cartCount = viewModel.cartCount
    val cartTotal = viewModel.cartTotal
    val categories = listOf(null) + MenuCategory.values().toList()

    Scaffold(
        containerColor = SurfaceDark,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Restaurant", fontWeight = FontWeight.ExtraBold, color = GoldPrimary)
                        Text("Goha Hotel · Gondar",
                            style = MaterialTheme.typography.labelSmall,
                            color = OnSurfaceDark.copy(alpha = 0.6f))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back", tint = GoldPrimary) }
                },
                actions = {
                    if (cartCount > 0) {
                        BadgedBox(badge = {
                            Badge(containerColor = GoldPrimary) {
                                Text("$cartCount", color = SurfaceDark,
                                    style = MaterialTheme.typography.labelSmall)
                            }
                        }) {
                            IconButton(onClick = onViewCart) {
                                Icon(Icons.Default.ShoppingCart, "Cart", tint = GoldPrimary)
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceDark)
            )
        },
        bottomBar = {
            AnimatedVisibility(cartCount > 0, enter = slideInVertically { it }, exit = slideOutVertically { it }) {
                Surface(
                    color = CardDark,
                    border = BorderStroke(1.dp, GoldPrimary.copy(0.2f)),
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("$cartCount item${if (cartCount > 1) "s" else ""} selected",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(0.6f))
                            Text("ETB ${cartTotal.toInt()}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold, color = GoldPrimary)
                        }
                        Button(
                            onClick = onViewCart,
                            shape  = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                        ) {
                            Text("Review Order", color = SurfaceDark, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.width(6.dp))
                            Icon(Icons.Default.ArrowForward, null, tint = SurfaceDark,
                                modifier = Modifier.size(16.dp))
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
                .background(
                    Brush.verticalGradient(
                        listOf(SurfaceDark, Color(0xFF0A1114))
                    )
                )
        ) {

            // Search bar - Modernized
            Surface(
                modifier = Modifier.padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                color = CardDark.copy(alpha = 0.6f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
            ) {
                TextField(
                    value = uiState.searchQuery,
                    onValueChange = viewModel::search,
                    placeholder  = { Text("Search delicious dishes...", color = Color.White.copy(0.4f)) },
                    leadingIcon  = { Icon(Icons.Default.Search, null, tint = GoldPrimary) },
                    trailingIcon = if (uiState.searchQuery.isNotBlank()) {
                        { IconButton(onClick = { viewModel.search("") }) {
                            Icon(Icons.Default.Clear, null, tint = GoldPrimary) } }
                    } else null,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = GoldPrimary,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
            }

            // Category tabs - Responsive Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                items(categories) { cat ->
                    val isSelected = uiState.selectedCategory == cat
                    Surface(
                        onClick = { viewModel.selectCategory(cat) },
                        shape = RoundedCornerShape(50),
                        color = if (isSelected) GoldPrimary else CardDark.copy(alpha = 0.6f),
                        border = BorderStroke(1.dp, if (isSelected) GoldPrimary else Color.White.copy(alpha = 0.1f))
                    ) {
                        Text(
                            text = cat?.displayName ?: "All Dishes",
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                            style = MaterialTheme.typography.labelLarge,
                            color = if (isSelected) SurfaceDark else Color.White,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            if (uiState.isLoading && uiState.menuItems.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GoldPrimary)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Featured section
                    if (uiState.featuredItems.isNotEmpty() && uiState.searchQuery.isBlank() && uiState.selectedCategory == null) {
                        item {
                            Text("⭐ Chef's Recommendations",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = GoldPrimary)
                            Spacer(Modifier.height(12.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                items(uiState.featuredItems) { item ->
                                    FeaturedItemCard(
                                        item      = item,
                                        onAddToCart = { viewModel.addToCart(item) }
                                    )
                                }
                            }
                            Spacer(Modifier.height(24.dp))
                            Text("Full Menu",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White)
                        }
                    }

                    items(uiState.menuItems, key = { it.id }) { item ->
                        MenuItemCard(
                            item        = item,
                            cartQty     = uiState.cartItems.firstOrNull { it.menuItem.id == item.id }?.quantity ?: 0,
                            onAddToCart = { viewModel.addToCart(item) },
                            onRemove    = { viewModel.removeFromCart(item) }
                        )
                    }

                    if (uiState.menuItems.isEmpty()) {
                        item {
                            Box(Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.RestaurantMenu, null, modifier = Modifier.size(64.dp), tint = GoldPrimary.copy(0.2f))
                                    Spacer(Modifier.height(16.dp))
                                    Text("No dishes found matching your search",
                                        color = Color.White.copy(0.5f),
                                        textAlign = TextAlign.Center)
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

@Composable
private fun FeaturedItemCard(item: MenuItem, onAddToCart: () -> Unit) {
    Card(
        modifier = Modifier.width(160.dp),
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(containerColor = CardDark)
    ) {
        Column {
            Box(Modifier.fillMaxWidth().height(100.dp)
                .background(Brush.verticalGradient(listOf(TealDark, SurfaceVariantDark)))) {
                val imageUrl = item.allImages.firstOrNull() ?: ""
                if (imageUrl.isNotBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = item.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("🍛", fontSize = 32.sp)
                    }
                }
                if (item.isVegetarian) {
                    Surface(Modifier.align(Alignment.TopStart).padding(4.dp),
                        shape = RoundedCornerShape(50), color = SuccessGreen) {
                        Text("VEG", Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall, color = Color.White)
                    }
                }
            }
            Column(Modifier.padding(10.dp)) {
                Text(item.name, style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold, maxLines = 1)
                Text("ETB ${item.price.toInt()}",
                    style = MaterialTheme.typography.bodySmall, color = GoldPrimary)
                Spacer(Modifier.height(6.dp))
                Button(onClick = onAddToCart, modifier = Modifier.fillMaxWidth().height(32.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("+ Add", color = SurfaceDark, style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun MenuItemCard(
    item: MenuItem, cartQty: Int,
    onAddToCart: () -> Unit, onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(containerColor = CardDark)
    ) {
        Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            // Image
            Box(Modifier.size(80.dp).clip(RoundedCornerShape(12.dp))
                .background(SurfaceVariantDark)) {
                val imageUrl = item.allImages.firstOrNull() ?: ""
                if (imageUrl.isNotBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = item.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("🍽️", fontSize = 28.sp)
                    }
                }
            }

            Column(Modifier.weight(1f)) {
                Text(item.name, style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold)
                if (item.nameAmharic.isNotBlank()) {
                    Text(item.nameAmharic, style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(0.5f))
                }
                Spacer(Modifier.height(2.dp))
                Text(item.description.take(60) + if (item.description.length > 60) "…" else "",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(0.6f), maxLines = 2)
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Text("ETB ${item.price.toInt()}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold, color = GoldPrimary)
                        if (item.prepTimeMinutes > 0) {
                            Text("~${item.prepTimeMinutes} min",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(0.45f))
                        }
                    }
                    // Quantity controls
                    if (cartQty > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            IconButton(onClick = onRemove, modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Default.Remove, null, Modifier.size(14.dp), tint = GoldPrimary)
                            }
                            Text("$cartQty", fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall, color = GoldPrimary)
                            IconButton(onClick = onAddToCart, modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Default.Add, null, Modifier.size(14.dp), tint = GoldPrimary)
                            }
                        }
                    } else {
                        IconButton(
                            onClick  = onAddToCart,
                            modifier = Modifier.size(32.dp)
                                .background(GoldPrimary, CircleShape)
                        ) {
                            Icon(Icons.Default.Add, "Add to cart",
                                Modifier.size(18.dp), tint = SurfaceDark)
                        }
                    }
                }
            }
        }
    }
}
