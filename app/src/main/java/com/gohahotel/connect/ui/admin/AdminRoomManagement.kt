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
import com.gohahotel.connect.domain.model.HotelRoom
import com.gohahotel.connect.domain.model.RoomType
import com.gohahotel.connect.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminRoomManagement(
    viewModel: AdminViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val rooms by viewModel.rooms.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    var editingRoom by remember { mutableStateOf<HotelRoom?>(null) }

    Scaffold(
        containerColor = SurfaceDark,
        topBar = {
            TopAppBar(
                title = { Text("Room Management", color = OnSurfaceDark, fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = GoldPrimary) } },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, null, tint = GoldPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceDark)
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
            items(rooms) { room ->
                AdminRoomItem(
                    room = room,
                    onEdit = { editingRoom = room },
                    onDelete = { viewModel.deleteRoom(room.id) }
                )
            }
        }
    }
}

    if (showAddDialog) {
        AddRoomDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { newRoom ->
                viewModel.saveRoom(newRoom)
                showAddDialog = false
            }
        )
    }

    if (editingRoom != null) {
        AddRoomDialog(
            roomToEdit = editingRoom,
            onDismiss = { editingRoom = null },
            onConfirm = { updatedRoom ->
                viewModel.saveRoom(updatedRoom)
                editingRoom = null
            }
        )
    }
}

@Composable
private fun AdminRoomItem(room: HotelRoom, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onEdit() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val img = room.imageUrls.firstOrNull() ?: ""
            if (img.isNotEmpty()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(img)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier.size(70.dp).clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Surface(
                    modifier = Modifier.size(70.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(0.05f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Home, null, tint = GoldPrimary.copy(0.3f))
                    }
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Room ${room.name}", fontWeight = FontWeight.Bold, color = GoldPrimary)
                Text(room.type.displayName, style = MaterialTheme.typography.bodySmall, color = OnSurfaceDark.copy(alpha = 0.85f))
                Text("${room.pricePerNight} ${room.currency} / night", style = MaterialTheme.typography.labelSmall, color = GoldLight)
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
fun AddRoomDialog(
    viewModel: AdminViewModel = hiltViewModel(),
    roomToEdit: HotelRoom? = null,
    onDismiss: () -> Unit,
    onConfirm: (HotelRoom) -> Unit
) {
    var name by remember { mutableStateOf(roomToEdit?.name ?: "") }
    var price by remember { mutableStateOf(roomToEdit?.pricePerNight?.toString() ?: "") }
    var description by remember { mutableStateOf(roomToEdit?.description ?: "") }
    var type by remember { mutableStateOf(roomToEdit?.type ?: RoomType.STANDARD) }
    var imageUrls by remember { mutableStateOf<List<String>>(roomToEdit?.imageUrls ?: emptyList()) }
    var expanded by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            viewModel.uploadImage(it, "rooms") { url ->
                imageUrls = imageUrls + url
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (roomToEdit == null) "Add New Room" else "Edit Room") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Room Number/Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Price per Night") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                
                Box {
                    OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
                        Text("Type: ${type.displayName}")
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        RoomType.entries.forEach { roomType ->
                            DropdownMenuItem(
                                text = { Text(roomType.displayName) },
                                onClick = {
                                    type = roomType
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Text("Room Images", style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    imageUrls.forEach { url ->
                        Box {
                            AsyncImage(
                                model = url,
                                contentDescription = null,
                                modifier = Modifier.size(60.dp).clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                            IconButton(
                                onClick = { imageUrls = imageUrls - url },
                                modifier = Modifier.size(20.dp).align(Alignment.TopEnd).background(Color.Black.copy(0.5f), CircleShape)
                            ) {
                                Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(12.dp))
                            }
                        }
                    }
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.1f))
                            .clickable { launcher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.AddAPhoto, null, tint = GoldPrimary)
                    }
                }
                
                if (imageUrls.isNotEmpty()) {
                    Text("✅ ${imageUrls.size} photos attached", color = SuccessGreen, style = MaterialTheme.typography.labelSmall)
                } else if (viewModel.isLoading.value) {
                    Text("⏳ Uploading photo...", color = GoldPrimary, style = MaterialTheme.typography.labelSmall)
                }
            }
        },
        confirmButton = {
            var validationError by remember { mutableStateOf<String?>(null) }
            
            LaunchedEffect(imageUrls.size, name, price) {
                validationError = null
            }
            
            Column(horizontalAlignment = Alignment.End) {
                if (validationError != null) {
                    Text(validationError!!, color = Color.Red, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(bottom = 4.dp))
                }
                Button(onClick = { 
                    if (name.isBlank()) {
                        validationError = "Room name/number is required"
                        return@Button
                    }
                    val priceVal = price.toDoubleOrNull()
                    if (priceVal == null || priceVal <= 0) {
                        validationError = "Valid price is required"
                        return@Button
                    }
                    if (imageUrls.isEmpty() && !viewModel.isLoading.value) {
                        validationError = "At least one image is required"
                        return@Button
                    }
                    
                    onConfirm((roomToEdit ?: HotelRoom()).copy(
                        name = name, 
                        pricePerNight = priceVal, 
                        description = description,
                        type = type,
                        imageUrls = imageUrls
                    )) 
                }
                ) {
                    if (viewModel.isLoading.value) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = SurfaceDark, strokeWidth = 2.dp)
                    } else {
                        Text(if (roomToEdit == null) "Add Room" else "Update Room")
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
