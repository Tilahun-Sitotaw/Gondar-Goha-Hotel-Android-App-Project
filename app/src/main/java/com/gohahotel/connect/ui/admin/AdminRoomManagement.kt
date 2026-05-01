package com.gohahotel.connect.ui.admin

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Room Management") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, null, tint = GoldPrimary)
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(rooms) { room ->
                AdminRoomItem(
                    room = room,
                    onDelete = { viewModel.deleteRoom(room.id) }
                )
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
}

@Composable
private fun AdminRoomItem(room: HotelRoom, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Room ${room.name}", fontWeight = FontWeight.Bold, color = GoldPrimary)
                Text(room.type.displayName, style = MaterialTheme.typography.bodySmall, color = OnSurfaceDark.copy(alpha = 0.6f))
                Text("${room.pricePerNight} ${room.currency} / night", style = MaterialTheme.typography.labelSmall, color = GoldPrimary.copy(alpha = 0.8f))
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, null, tint = Color.Red.copy(alpha = 0.7f))
            }
        }
    }
}

@Composable
fun AddRoomDialog(onDismiss: () -> Unit, onConfirm: (HotelRoom) -> Unit) {
    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(RoomType.STANDARD) }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Room") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Room Number/Name") })
                OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Price per Night") })
                
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
            }
        },
        confirmButton = {
            Button(onClick = { 
                onConfirm(HotelRoom(
                    name = name, 
                    pricePerNight = price.toDoubleOrNull() ?: 0.0, 
                    type = type
                )) 
            }) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
