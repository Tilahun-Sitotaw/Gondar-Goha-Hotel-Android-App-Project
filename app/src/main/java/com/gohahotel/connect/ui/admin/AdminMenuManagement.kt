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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Menu Management", fontWeight = FontWeight.Bold) },
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
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(menuItems) { item ->
                AdminMenuItem(
                    item = item,
                    onDelete = { viewModel.deleteMenuItem(item.id) }
                )
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
}

@Composable
private fun AdminMenuItem(item: MenuItem, onDelete: () -> Unit) {
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
                Text(item.name, fontWeight = FontWeight.Bold, color = GoldPrimary)
                Text(item.category.displayName, style = MaterialTheme.typography.bodySmall, color = OnSurfaceDark.copy(alpha = 0.6f))
                Text("${item.price} ${item.currency}", style = MaterialTheme.typography.labelSmall, color = GoldPrimary.copy(alpha = 0.8f))
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, null, tint = Color.Red.copy(alpha = 0.7f))
            }
        }
    }
}

@Composable
fun AddMenuItemDialog(onDismiss: () -> Unit, onConfirm: (MenuItem) -> Unit) {
    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(MenuCategory.ETHIOPIAN) }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Dish") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Dish Name") })
                OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Price") })
                
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
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(MenuItem(name = name, price = price.toDoubleOrNull() ?: 0.0, category = category)) }) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
