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
import com.gohahotel.connect.domain.model.HotelRoom
import com.gohahotel.connect.domain.model.RoomType
import com.gohahotel.connect.ui.theme.*

private fun roomTypeColor(type: RoomType): Color = when (type) {
    RoomType.STANDARD     -> Color(0xFF4A90E2)
    RoomType.TWIN         -> Color(0xFF50C878)
    RoomType.KING         -> Color(0xFFD4A843)
    RoomType.SUITE        -> Color(0xFFB04AE2)
    RoomType.PRESIDENTIAL -> Color(0xFFE24A4A)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminRoomManagement(
    viewModel: AdminViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val rooms by viewModel.rooms.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingRoom   by remember { mutableStateOf<HotelRoom?>(null) }
    var roomToDelete  by remember { mutableStateOf<HotelRoom?>(null) }

    Scaffold(
        containerColor = SurfaceDark,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Room Management", color = OnSurfaceDark,
                            fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                        Text("${rooms.size} rooms · ${rooms.count { it.isAvailable }} available",
                            style = MaterialTheme.typography.labelSmall,
                            color = GoldPrimary.copy(alpha = 0.7f))
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
                            modifier = Modifier.size(36.dp).background(GoldPrimary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) { Icon(Icons.Default.Add, null, tint = Color.Black, modifier = Modifier.size(20.dp)) }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceDark)
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(SurfaceDark, Color(0xFF0A1424), Color(0xFF050D18))))
                .padding(padding)
        ) {
            if (rooms.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.Home, null, tint = GoldPrimary.copy(0.3f), modifier = Modifier.size(64.dp))
                    Spacer(Modifier.height(16.dp))
                    Text("No rooms yet", color = OnSurfaceDark.copy(0.5f), fontWeight = FontWeight.Bold)
                    Text("Tap + to add your first room", color = OnSurfaceDark.copy(0.3f),
                        style = MaterialTheme.typography.bodySmall)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(rooms, key = { it.id }) { room ->
                        AdminRoomCard(
                            room     = room,
                            onEdit   = { editingRoom = room },
                            onDelete = { roomToDelete = room }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddRoomDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { newRoom -> viewModel.saveRoom(newRoom); showAddDialog = false }
        )
    }

    editingRoom?.let { room ->
        AddRoomDialog(
            roomToEdit = room,
            onDismiss  = { editingRoom = null },
            onConfirm  = { updated -> viewModel.saveRoom(updated); editingRoom = null }
        )
    }

    roomToDelete?.let { room ->
        AlertDialog(
            onDismissRequest = { roomToDelete = null },
            containerColor   = Color(0xFF1A2535),
            title = { Text("Delete Room?", color = Color(0xFFE24A4A), fontWeight = FontWeight.Bold) },
            text  = { Text("Remove \"Room ${room.name}\" permanently?", color = OnSurfaceDark.copy(0.8f)) },
            confirmButton = {
                Button(
                    onClick = { viewModel.deleteRoom(room.id); roomToDelete = null },
                    colors  = ButtonDefaults.buttonColors(containerColor = Color(0xFFE24A4A))
                ) { Text("Delete", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { roomToDelete = null }) { Text("Cancel", color = GoldPrimary) }
            }
        )
    }
}

@Composable
private fun AdminRoomCard(room: HotelRoom, onEdit: () -> Unit, onDelete: () -> Unit) {
    val accent = roomTypeColor(room.type)
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onEdit() },
        shape    = RoundedCornerShape(20.dp),
        colors   = CardDefaults.cardColors(containerColor = Color(0xFF111E2E)),
        border   = BorderStroke(1.dp, accent.copy(alpha = 0.25f))
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(76.dp).clip(RoundedCornerShape(14.dp))
                    .background(accent.copy(0.12f))
            ) {
                val img = room.imageUrls.firstOrNull() ?: room.imageUrl
                if (img.isNotEmpty()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current).data(img).crossfade(true).build(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Home, null, tint = accent.copy(0.5f), modifier = Modifier.size(32.dp))
                    }
                }
                Box(
                    modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).size(10.dp)
                        .background(if (room.isAvailable) Color(0xFF50C878) else Color(0xFFE24A4A), CircleShape)
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Room ${room.name}", fontWeight = FontWeight.ExtraBold,
                        color = OnSurfaceDark, fontSize = 15.sp)
                    Spacer(Modifier.width(8.dp))
                    Surface(shape = RoundedCornerShape(6.dp), color = accent.copy(0.18f)) {
                        Text(room.type.displayName,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = accent, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text("${room.pricePerNight.toInt()} ${room.currency} / night",
                    color = GoldPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                if (room.description.isNotBlank()) {
                    Text(room.description, style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceDark.copy(0.5f), maxLines = 1)
                }
            }
            Column {
                IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, null, tint = GoldLight) }
                IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, null, tint = Color(0xFFE24A4A).copy(0.8f)) }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Add / Edit Room Dialog  — fixed upload + validation
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun AddRoomDialog(
    viewModel: AdminViewModel = hiltViewModel(),
    roomToEdit: HotelRoom? = null,
    onDismiss: () -> Unit,
    onConfirm: (HotelRoom) -> Unit
) {
    var name        by remember { mutableStateOf(roomToEdit?.name ?: "") }
    var price       by remember { mutableStateOf(roomToEdit?.pricePerNight?.toString() ?: "") }
    var description by remember { mutableStateOf(roomToEdit?.description ?: "") }
    var type        by remember { mutableStateOf(roomToEdit?.type ?: RoomType.STANDARD) }
    var capacity    by remember { mutableStateOf(roomToEdit?.capacity?.toString() ?: "2") }
    var floor       by remember { mutableStateOf(roomToEdit?.floorNumber?.toString() ?: "1") }
    var hasView     by remember { mutableStateOf(roomToEdit?.hasView ?: false) }
    var isAvailable by remember { mutableStateOf(roomToEdit?.isAvailable ?: true) }
    var imageUrls   by remember { mutableStateOf<List<String>>(roomToEdit?.imageUrls ?: emptyList()) }
    var validationError by remember { mutableStateOf<String?>(null) }

    // Use dedicated upload state — not the shared isLoading
    val isUploading by viewModel.isUploading.collectAsState()
    val uploadError by viewModel.uploadError.collectAsState()

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            validationError = null
            viewModel.uploadImage(it, "rooms") { url -> imageUrls = imageUrls + url }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = Color(0xFF111E2E),
        shape            = RoundedCornerShape(24.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(36.dp).background(GoldPrimary.copy(0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Default.Home, null, tint = GoldPrimary, modifier = Modifier.size(20.dp)) }
                Spacer(Modifier.width(10.dp))
                Text(if (roomToEdit == null) "Add New Room" else "Edit Room",
                    color = GoldPrimary, fontWeight = FontWeight.ExtraBold)
            }
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Room type selector
                Text("Room Type", style = MaterialTheme.typography.labelMedium, color = GoldPrimary.copy(0.8f))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RoomType.entries.forEach { rt ->
                        val accent   = roomTypeColor(rt)
                        val selected = type == rt
                        Surface(
                            onClick = { type = rt },
                            shape   = RoundedCornerShape(10.dp),
                            color   = if (selected) accent.copy(0.25f) else Color.White.copy(0.05f),
                            border  = BorderStroke(1.dp, if (selected) accent else Color.White.copy(0.1f))
                        ) {
                            Text(rt.displayName,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                style    = MaterialTheme.typography.labelSmall,
                                color    = if (selected) accent else OnSurfaceDark.copy(0.6f),
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                }

                ColoredTextField(value = name, onValueChange = { name = it; validationError = null },
                    label = "Room Number / Name", icon = Icons.Default.Home)
                ColoredTextField(value = price, onValueChange = { price = it; validationError = null },
                    label = "Price per Night (ETB)", icon = Icons.Default.AttachMoney)
                ColoredTextField(value = description, onValueChange = { description = it },
                    label = "Description", icon = Icons.Default.Description, minLines = 2)

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ColoredTextField(value = capacity, onValueChange = { capacity = it },
                        label = "Capacity", icon = Icons.Default.People, modifier = Modifier.weight(1f))
                    ColoredTextField(value = floor, onValueChange = { floor = it },
                        label = "Floor", icon = Icons.Default.Layers, modifier = Modifier.weight(1f))
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ToggleChip("🌄 Has View",  hasView,     Color(0xFF50C878), Modifier.weight(1f)) { hasView = it }
                    ToggleChip("✅ Available", isAvailable, TealPrimary,       Modifier.weight(1f)) { isAvailable = it }
                }

                // ── Photos section ────────────────────────────────────────────
                Text("Room Photos", style = MaterialTheme.typography.labelMedium, color = GoldPrimary.copy(0.8f))

                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    imageUrls.forEach { url ->
                        Box {
                            AsyncImage(
                                model = url, contentDescription = null,
                                modifier = Modifier.size(72.dp).clip(RoundedCornerShape(10.dp)),
                                contentScale = ContentScale.Crop
                            )
                            IconButton(
                                onClick  = { imageUrls = imageUrls - url },
                                modifier = Modifier.size(22.dp).align(Alignment.TopEnd)
                                    .background(Color(0xFFE24A4A), CircleShape)
                            ) {
                                Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(12.dp))
                            }
                        }
                    }
                    // Add photo button — disabled while uploading
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isUploading) GoldPrimary.copy(0.05f) else GoldPrimary.copy(0.12f))
                            .border(1.dp, GoldPrimary.copy(if (isUploading) 0.1f else 0.3f), RoundedCornerShape(10.dp))
                            .clickable(enabled = !isUploading) { launcher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isUploading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp),
                                color = GoldPrimary, strokeWidth = 2.dp)
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.AddAPhoto, null, tint = GoldPrimary, modifier = Modifier.size(22.dp))
                                Text("Add", style = MaterialTheme.typography.labelSmall, color = GoldPrimary)
                            }
                        }
                    }
                }

                // Upload status
                when {
                    isUploading -> {
                        Row(verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            CircularProgressIndicator(modifier = Modifier.size(14.dp),
                                color = GoldPrimary, strokeWidth = 2.dp)
                            Text("Uploading photo...", color = GoldPrimary,
                                style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    imageUrls.isNotEmpty() -> {
                        Text("✅ ${imageUrls.size} photo(s) ready",
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
                            validationError = "Room name/number is required"
                        price.toDoubleOrNull()?.let { it <= 0 } != false ->
                            validationError = "Enter a valid price"
                        isUploading ->
                            validationError = "Please wait — photo is still uploading"
                        imageUrls.isEmpty() ->
                            validationError = "At least one photo is required"
                        else -> onConfirm(
                            (roomToEdit ?: HotelRoom()).copy(
                                name         = name,
                                pricePerNight = price.toDouble(),
                                description  = description,
                                type         = type,
                                capacity     = capacity.toIntOrNull() ?: 2,
                                floorNumber  = floor.toIntOrNull() ?: 1,
                                hasView      = hasView,
                                isAvailable  = isAvailable,
                                imageUrls    = imageUrls
                            )
                        )
                    }
                },
                // Disable while uploading so user can't submit mid-upload
                enabled = !isUploading,
                colors  = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                shape   = RoundedCornerShape(12.dp)
            ) {
                Icon(if (roomToEdit == null) Icons.Default.Add else Icons.Default.Check,
                    null, tint = Color.Black, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(if (roomToEdit == null) "Add Room" else "Update Room",
                    color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = OnSurfaceDark.copy(0.6f)) }
        }
    )
}

// ── Shared helpers (used by all admin dialogs) ────────────────────────────────
@Composable
internal fun ColoredTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier.fillMaxWidth(),
    minLines: Int = 1
) {
    OutlinedTextField(
        value         = value,
        onValueChange = onValueChange,
        label         = { Text(label, color = OnSurfaceDark.copy(0.5f)) },
        leadingIcon   = { Icon(icon, null, tint = GoldPrimary.copy(0.7f), modifier = Modifier.size(18.dp)) },
        modifier      = modifier,
        minLines      = minLines,
        colors        = OutlinedTextFieldDefaults.colors(
            focusedTextColor     = OnSurfaceDark,
            unfocusedTextColor   = OnSurfaceDark.copy(0.9f),
            focusedBorderColor   = GoldPrimary,
            unfocusedBorderColor = Color.White.copy(0.15f),
            cursorColor          = GoldPrimary,
            focusedLabelColor    = GoldPrimary,
            focusedContainerColor   = Color.Transparent,
            unfocusedContainerColor = Color.Transparent
        ),
        shape = RoundedCornerShape(12.dp)
    )
}

@Composable
internal fun ToggleChip(
    label: String,
    checked: Boolean,
    color: Color,
    modifier: Modifier = Modifier,
    onToggle: (Boolean) -> Unit
) {
    Surface(
        onClick   = { onToggle(!checked) },
        modifier  = modifier,
        shape     = RoundedCornerShape(10.dp),
        color     = if (checked) color.copy(0.2f) else Color.White.copy(0.05f),
        border    = BorderStroke(1.dp, if (checked) color else Color.White.copy(0.1f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(label,
                style      = MaterialTheme.typography.labelSmall,
                color      = if (checked) color else OnSurfaceDark.copy(0.5f),
                fontWeight = if (checked) FontWeight.Bold else FontWeight.Normal)
        }
    }
}
