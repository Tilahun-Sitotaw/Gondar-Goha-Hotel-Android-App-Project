package com.gohahotel.connect.ui.admin

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.gohahotel.connect.domain.model.MenuCategory
import com.gohahotel.connect.domain.model.MenuItem
import com.gohahotel.connect.ui.theme.*

private fun categoryColor(cat: MenuCategory): Color = when (cat) {
    MenuCategory.ETHIOPIAN    -> Color(0xFFD4A843)
    MenuCategory.INTERNATIONAL -> Color(0xFF4A90E2)
    MenuCategory.BEVERAGES    -> Color(0xFF50C878)
    MenuCategory.DESSERTS     -> Color(0xFFE24A8A)
    MenuCategory.BREAKFAST    -> Color(0xFFE29B4A)
    MenuCategory.ROOM_SERVICE -> Color(0xFFB04AE2)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminMenuManagement(
    viewModel: AdminViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val menuItems by viewModel.menuItems.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingItem   by remember { mutableStateOf<MenuItem?>(null) }
    var itemToDelete  by remember { mutableStateOf<MenuItem?>(null) }

    Scaffold(
        containerColor = SurfaceDark,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Dining Menu", color = OnSurfaceDark,
                            fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                        Text("${menuItems.size} dishes · ${menuItems.count { it.isAvailable }} available",
                            style = MaterialTheme.typography.labelSmall,
                            color = TealLight.copy(alpha = 0.8f))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = GoldPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Box(
                            modifier = Modifier.size(36.dp).background(TealPrimary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) { Icon(Icons.Default.Add, null, tint = Color.White, modifier = Modifier.size(20.dp)) }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceDark)
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(SurfaceDark, Color(0xFF071420), Color(0xFF050D18))))
                .padding(padding)
        ) {
            if (menuItems.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.Restaurant, null, tint = TealPrimary.copy(0.3f), modifier = Modifier.size(64.dp))
                    Spacer(Modifier.height(16.dp))
                    Text("No dishes yet", color = OnSurfaceDark.copy(0.5f), fontWeight = FontWeight.Bold)
                    Text("Tap + to add your first dish", color = OnSurfaceDark.copy(0.3f),
                        style = MaterialTheme.typography.bodySmall)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(menuItems, key = { it.id }) { item ->
                        AdminDishCard(
                            item     = item,
                            onEdit   = { editingItem = item },
                            onDelete = { itemToDelete = item }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddMenuItemDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { newItem -> viewModel.saveMenuItem(newItem); showAddDialog = false }
        )
    }

    editingItem?.let { item ->
        AddMenuItemDialog(
            itemToEdit = item,
            onDismiss  = { editingItem = null },
            onConfirm  = { updated -> viewModel.saveMenuItem(updated); editingItem = null }
        )
    }

    itemToDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            containerColor   = Color(0xFF111E2E),
            title = { Text("Delete Dish?", color = Color(0xFFE24A4A), fontWeight = FontWeight.Bold) },
            text  = { Text("Remove \"${item.name}\" from the menu?", color = OnSurfaceDark.copy(0.8f)) },
            confirmButton = {
                Button(
                    onClick = { viewModel.deleteMenuItem(item.id); itemToDelete = null },
                    colors  = ButtonDefaults.buttonColors(containerColor = Color(0xFFE24A4A))
                ) { Text("Delete", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null }) { Text("Cancel", color = GoldPrimary) }
            }
        )
    }
}

@Composable
private fun AdminDishCard(item: MenuItem, onEdit: () -> Unit, onDelete: () -> Unit) {
    val accent = categoryColor(item.category)
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onEdit() },
        shape    = RoundedCornerShape(20.dp),
        colors   = CardDefaults.cardColors(containerColor = Color(0xFF0E1B2A)),
        border   = BorderStroke(1.dp, accent.copy(0.25f))
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(72.dp).clip(RoundedCornerShape(14.dp)).background(accent.copy(0.1f))
            ) {
                if (item.imageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current).data(item.imageUrl).crossfade(true).build(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Restaurant, null, tint = accent.copy(0.5f), modifier = Modifier.size(30.dp))
                    }
                }
                Box(
                    modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).size(10.dp)
                        .background(if (item.isAvailable) Color(0xFF50C878) else Color(0xFFE24A4A), CircleShape)
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.name, fontWeight = FontWeight.ExtraBold, color = OnSurfaceDark, fontSize = 15.sp)
                Spacer(Modifier.height(3.dp))
                Surface(shape = RoundedCornerShape(6.dp), color = accent.copy(0.18f)) {
                    Text(item.category.displayName,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = accent, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(4.dp))
                Text("${item.price.toInt()} ${item.currency}",
                    color = GoldPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
            Column {
                IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, null, tint = GoldLight) }
                IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, null, tint = Color(0xFFE24A4A).copy(0.8f)) }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Add / Edit Dish Dialog  — fixed upload + validation
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun AddMenuItemDialog(
    viewModel: AdminViewModel = hiltViewModel(),
    itemToEdit: MenuItem? = null,
    onDismiss: () -> Unit,
    onConfirm: (MenuItem) -> Unit
) {
    var name         by remember { mutableStateOf(itemToEdit?.name ?: "") }
    var price        by remember { mutableStateOf(itemToEdit?.price?.toString() ?: "") }
    var description  by remember { mutableStateOf(itemToEdit?.description ?: "") }
    var category     by remember { mutableStateOf(itemToEdit?.category ?: MenuCategory.ETHIOPIAN) }
    var imageUrl     by remember { mutableStateOf(itemToEdit?.imageUrl ?: "") }
    var isVegetarian by remember { mutableStateOf(itemToEdit?.isVegetarian ?: false) }
    var isVegan      by remember { mutableStateOf(itemToEdit?.isVegan ?: false) }
    var isSpicy      by remember { mutableStateOf(itemToEdit?.isSpicy ?: false) }
    var isAvailable  by remember { mutableStateOf(itemToEdit?.isAvailable ?: true) }
    var validationError by remember { mutableStateOf<String?>(null) }

    // Dedicated upload state
    val isUploading by viewModel.isUploading.collectAsState()
    val uploadError by viewModel.uploadError.collectAsState()

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            validationError = null
            viewModel.uploadImage(it, "menu") { url -> imageUrl = url }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = Color(0xFF0E1B2A),
        shape            = RoundedCornerShape(24.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(36.dp).background(TealPrimary.copy(0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Default.Restaurant, null, tint = TealLight, modifier = Modifier.size(20.dp)) }
                Spacer(Modifier.width(10.dp))
                Text(if (itemToEdit == null) "Add New Dish" else "Edit Dish",
                    color = TealLight, fontWeight = FontWeight.ExtraBold)
            }
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Category selector
                Text("Category", style = MaterialTheme.typography.labelMedium, color = TealLight.copy(0.8f))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    MenuCategory.entries.forEach { cat ->
                        val accent   = categoryColor(cat)
                        val selected = category == cat
                        Surface(
                            onClick = { category = cat },
                            shape   = RoundedCornerShape(10.dp),
                            color   = if (selected) accent.copy(0.25f) else Color.White.copy(0.05f),
                            border  = BorderStroke(1.dp, if (selected) accent else Color.White.copy(0.1f))
                        ) {
                            Text(cat.displayName,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                style    = MaterialTheme.typography.labelSmall,
                                color    = if (selected) accent else OnSurfaceDark.copy(0.5f),
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                }

                ColoredTextField(value = name, onValueChange = { name = it; validationError = null },
                    label = "Dish Name", icon = Icons.Default.Restaurant)
                ColoredTextField(value = price, onValueChange = { price = it; validationError = null },
                    label = "Price (ETB)", icon = Icons.Default.AttachMoney)
                ColoredTextField(value = description, onValueChange = { description = it },
                    label = "Description", icon = Icons.Default.Description, minLines = 2)

                // Diet toggles
                Text("Dietary Tags", style = MaterialTheme.typography.labelMedium, color = TealLight.copy(0.8f))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    ToggleChip("🌿 Veg",   isVegetarian, Color(0xFF50C878), Modifier.weight(1f)) { isVegetarian = it }
                    ToggleChip("🌱 Vegan", isVegan,      Color(0xFF2E7D32), Modifier.weight(1f)) { isVegan = it }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    ToggleChip("🌶 Spicy",    isSpicy,     Color(0xFFE24A4A), Modifier.weight(1f)) { isSpicy = it }
                    ToggleChip("✅ Available", isAvailable, TealPrimary,       Modifier.weight(1f)) { isAvailable = it }
                }

                // ── Photo section ─────────────────────────────────────────────
                Text("Dish Photo", style = MaterialTheme.typography.labelMedium, color = TealLight.copy(0.8f))

                // Preview if already uploaded
                if (imageUrl.isNotEmpty()) {
                    Box {
                        AsyncImage(
                            model = imageUrl, contentDescription = null,
                            modifier = Modifier.fillMaxWidth().height(150.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                        // Remove button
                        IconButton(
                            onClick  = { imageUrl = "" },
                            modifier = Modifier.align(Alignment.TopEnd).padding(6.dp)
                                .size(28.dp).background(Color(0xFFE24A4A), CircleShape)
                        ) {
                            Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(14.dp))
                        }
                    }
                }

                // Upload button
                Button(
                    onClick  = { launcher.launch("image/*") },
                    enabled  = !isUploading,
                    modifier = Modifier.fillMaxWidth(),
                    colors   = ButtonDefaults.buttonColors(
                        containerColor = TealPrimary.copy(0.15f),
                        contentColor   = TealLight,
                        disabledContainerColor = TealPrimary.copy(0.05f),
                        disabledContentColor   = TealLight.copy(0.4f)
                    ),
                    shape  = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, TealPrimary.copy(if (isUploading) 0.15f else 0.4f))
                ) {
                    if (isUploading) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp),
                            color = TealLight, strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                        Text("Uploading...")
                    } else {
                        Icon(Icons.Default.AddAPhoto, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(if (imageUrl.isEmpty()) "Upload Photo" else "Change Photo")
                    }
                }

                // Status messages
                when {
                    isUploading -> {
                        Text("⏳ Uploading, please wait...",
                            color = TealLight, style = MaterialTheme.typography.labelSmall)
                    }
                    imageUrl.isNotEmpty() -> {
                        Text("✅ Photo ready",
                            color = Color(0xFF50C878), style = MaterialTheme.typography.labelSmall)
                    }
                }

                uploadError?.let {
                    Text("⚠ $it", color = Color(0xFFE24A4A), style = MaterialTheme.typography.labelSmall)
                }
                validationError?.let {
                    Text(it, color = Color(0xFFE24A4A), style = MaterialTheme.typography.labelSmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    when {
                        name.isBlank() ->
                            validationError = "Dish name is required"
                        price.toDoubleOrNull()?.let { it <= 0 } != false ->
                            validationError = "Enter a valid price"
                        isUploading ->
                            validationError = "Please wait — photo is still uploading"
                        imageUrl.isBlank() ->
                            validationError = "A photo is required"
                        else -> onConfirm(
                            (itemToEdit ?: MenuItem()).copy(
                                name         = name,
                                price        = price.toDouble(),
                                description  = description,
                                category     = category,
                                imageUrl     = imageUrl,
                                isVegetarian = isVegetarian,
                                isVegan      = isVegan,
                                isSpicy      = isSpicy,
                                isAvailable  = isAvailable
                            )
                        )
                    }
                },
                enabled = !isUploading,
                colors  = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                shape   = RoundedCornerShape(12.dp)
            ) {
                Icon(if (itemToEdit == null) Icons.Default.Add else Icons.Default.Check,
                    null, tint = Color.White, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(if (itemToEdit == null) "Add Dish" else "Update Dish",
                    color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = OnSurfaceDark.copy(0.6f)) }
        }
    )
}
