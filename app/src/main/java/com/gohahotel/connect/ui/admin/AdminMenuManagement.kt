package com.gohahotel.connect.ui.admin

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.gohahotel.connect.domain.model.MenuItem
import com.gohahotel.connect.domain.model.MenuCategory
import com.gohahotel.connect.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminMenuManagement(
    viewModel: AdminViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val menuItems by viewModel.menuItems.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    var editingItem by remember { mutableStateOf<MenuItem?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Menu Management", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = GoldPrimary) } },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, null, tint = GoldPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceDark,
                    titleContentColor = GoldPrimary
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(SurfaceDark, Color(0xFF0A1424), Color.Black)))
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
            items(menuItems) { item ->
                AdminMenuItem(
                    item = item,
                    onEdit = { editingItem = item },
                    onDelete = { viewModel.deleteMenuItem(item.id) }
                )
            }
        }
    }
}

    if (showAddDialog) {
        AddMenuItemDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { newItem ->
                viewModel.saveMenuItem(newItem)
                showAddDialog = false
            }
        )
    }

    if (editingItem != null) {
        AddMenuItemDialog(
            itemToEdit = editingItem,
            onDismiss = { editingItem = null },
            onConfirm = { updatedItem ->
                viewModel.saveMenuItem(updatedItem)
                editingItem = null
            }
        )
    }
}

@Composable
private fun AdminMenuItem(item: MenuItem, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onEdit() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (item.imageUrl.isNotEmpty()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(item.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier.size(60.dp).clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Surface(
                    modifier = Modifier.size(60.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(0.05f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.List, null, tint = GoldPrimary.copy(0.3f))
                    }
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.name, fontWeight = FontWeight.Bold, color = GoldPrimary)
                Text(item.category.displayName, style = MaterialTheme.typography.bodySmall, color = OnSurfaceDark.copy(alpha = 0.6f))
                Text("${item.price} ${item.currency}", style = MaterialTheme.typography.labelSmall, color = GoldPrimary.copy(alpha = 0.8f))
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, null, tint = GoldLight)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, null, tint = Color.Red.copy(alpha = 0.7f))
            }
        }
    }
}

@Composable
fun AddMenuItemDialog(
    viewModel: AdminViewModel = hiltViewModel(),
    itemToEdit: MenuItem? = null,
    onDismiss: () -> Unit,
    onConfirm: (MenuItem) -> Unit
) {
    var name by remember { mutableStateOf(itemToEdit?.name ?: "") }
    var price by remember { mutableStateOf(itemToEdit?.price?.toString() ?: "") }
    var description by remember { mutableStateOf(itemToEdit?.description ?: "") }
    var category by remember { mutableStateOf(itemToEdit?.category ?: MenuCategory.ETHIOPIAN) }
    var imageUrl by remember { mutableStateOf(itemToEdit?.imageUrl ?: "") }
    var expanded by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            viewModel.uploadImage(it, "menu") { url ->
                imageUrl = url
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (itemToEdit == null) "Add New Dish" else "Edit Dish") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Dish Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Price") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                
                Box {
                    OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
                        Text("Category: ${category.displayName}")
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        MenuCategory.entries.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.displayName) },
                                onClick = {
                                    category = cat
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Text("Dish Image", style = MaterialTheme.typography.labelMedium)
                if (imageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxWidth().height(150.dp).clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
                
                Button(
                    onClick = { launcher.launch("image/*") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary.copy(alpha = 0.1f), contentColor = GoldPrimary)
                ) {
                    Icon(Icons.Default.AddAPhoto, null)
                    Spacer(Modifier.width(8.dp))
                    Text(if (imageUrl.isEmpty()) "Upload Image" else "Change Image")
                }

                if (imageUrl.isNotEmpty()) {
                    Text("✅ Photo attached", color = SuccessGreen, style = MaterialTheme.typography.labelSmall)
                } else if (viewModel.isLoading.value) {
                    Text("⏳ Uploading...", color = GoldPrimary, style = MaterialTheme.typography.labelSmall)
                }
            }
        },
        confirmButton = {
            var validationError by remember { mutableStateOf<String?>(null) }
            
            LaunchedEffect(imageUrl, name, price) {
                validationError = null
            }
            
            Column(horizontalAlignment = Alignment.End) {
                if (validationError != null) {
                    Text(validationError!!, color = Color.Red, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(bottom = 4.dp))
                }
                Button(onClick = { 
                    if (name.isBlank()) {
                        validationError = "Dish name is required"
                        return@Button
                    }
                    val priceVal = price.toDoubleOrNull()
                    if (priceVal == null || priceVal <= 0) {
                        validationError = "Valid price is required"
                        return@Button
                    }
                    if (imageUrl.isBlank() && !viewModel.isLoading.value) {
                        validationError = "Image is required"
                        return@Button
                    }
                    
                    onConfirm((itemToEdit ?: MenuItem()).copy(
                        name = name, 
                        price = priceVal, 
                        description = description,
                        category = category,
                        imageUrl = imageUrl
                    )) 
                }
                ) {
                    if (viewModel.isLoading.value) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = SurfaceDark, strokeWidth = 2.dp)
                    } else {
                        Text(if (itemToEdit == null) "Add Dish" else "Update Dish")
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
