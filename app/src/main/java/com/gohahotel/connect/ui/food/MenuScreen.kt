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

    Scaffold(
        containerColor = SurfaceDark,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Restaurant", fontWeight = FontWeight.Bold, color = OnSurfaceDark)
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
                Surface(shadowElevation = 8.dp) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("$cartCount item${if (cartCount > 1) "s" else ""} in cart",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(0.6f))
                            Text("ETB ${cartTotal.toInt()}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold, color = GoldPrimary)
                        }
                        Button(
                            onClick = onViewCart,
                            shape  = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                        ) {
                            Text("View Cart", color = SurfaceDark, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.width(6.dp))
                            Icon(Icons.Default.ArrowForward, null, tint = SurfaceDark,
                                modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {

            // Search bar
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = viewModel::search,
                placeholder  = { Text("Search dishes...") },
                leadingIcon  = { Icon(Icons.Default.Search, null) },
                trailingIcon = if (uiState.searchQuery.isNotBlank()) {
                    { IconButton(onClick = { viewModel.search("") }) {
                        Icon(Icons.Default.Clear, null) } }
                } else null,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(14.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = GoldPrimary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(0.4f)
                )
            )

            // Category tabs
            ScrollableTabRow(
                selectedTabIndex = MenuCategory.values().indexOf(uiState.selectedCategory),
                edgePadding      = 16.dp,
                containerColor   = MaterialTheme.colorScheme.background,
                contentColor     = GoldPrimary,
                divider          = {}
            ) {
                MenuCategory.values().forEach { cat ->
                    Tab(
                        selected = uiState.selectedCategory == cat,
                        onClick  = { viewModel.selectCategory(cat) },
                        text     = { Text(cat.displayName,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (uiState.selectedCategory == cat) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
            }

            Spacer(Modifier.height(4.dp))

            if (uiState.isLoading && uiState.menuItems.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GoldPrimary)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Featured section
                    if (uiState.featuredItems.isNotEmpty() && uiState.searchQuery.isBlank()) {
                        item {
                            Text("⭐ Chef's Recommendations",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(8.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                items(uiState.featuredItems.take(5)) { item ->
                                    FeaturedItemCard(
                                        item      = item,
                                        onAddToCart = { viewModel.addToCart(item) }
                                    )
                                }
                            }
                            Spacer(Modifier.height(12.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(0.2f))
                            Spacer(Modifier.height(4.dp))
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
                                    Text("🍽️", fontSize = 48.sp)
                                    Spacer(Modifier.height(8.dp))
                                    Text("No dishes found",
                                        color = Color.White.copy(0.5f))
                                }
                            }
                        }
                    }
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
