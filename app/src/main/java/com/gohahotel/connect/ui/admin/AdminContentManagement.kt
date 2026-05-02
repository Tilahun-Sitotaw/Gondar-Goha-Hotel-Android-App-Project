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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.gohahotel.connect.domain.model.GuideCategory
import com.gohahotel.connect.domain.model.GuideEntry
import com.gohahotel.connect.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminContentManagement(
    viewModel: AdminViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val guideEntries by viewModel.guideEntries.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingEntry by remember { mutableStateOf<GuideEntry?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Content & Experiences") },
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
            items(guideEntries) { entry ->
                AdminContentItem(
                    entry = entry,
                    onEdit = { editingEntry = entry },
                    onDelete = { viewModel.deleteGuideEntry(entry.id) }
                )
            }
        }
    }

    if (showAddDialog) {
        AddContentDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { newEntry ->
                viewModel.saveGuideEntry(newEntry)
                showAddDialog = false
            }
        )
    }

    if (editingEntry != null) {
        AddContentDialog(
            entryToEdit = editingEntry,
            onDismiss = { editingEntry = null },
            onConfirm = { updatedEntry ->
                viewModel.saveGuideEntry(updatedEntry)
                editingEntry = null
            }
        )
    }
}

@Composable
private fun AdminContentItem(entry: GuideEntry, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onEdit() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = entry.imageUrls.firstOrNull(),
                contentDescription = null,
                modifier = Modifier.size(60.dp).clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(entry.title, fontWeight = FontWeight.Bold, color = GoldPrimary)
                Text(entry.category.displayName, style = MaterialTheme.typography.bodySmall, color = OnSurfaceDark.copy(alpha = 0.6f))
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
fun AddContentDialog(
    viewModel: AdminViewModel = hiltViewModel(),
    entryToEdit: GuideEntry? = null,
    onDismiss: () -> Unit,
    onConfirm: (GuideEntry) -> Unit
) {
    var title by remember { mutableStateOf(entryToEdit?.title ?: "") }
    var summary by remember { mutableStateOf(entryToEdit?.summary ?: "") }
    var category by remember { mutableStateOf(entryToEdit?.category ?: GuideCategory.HERITAGE) }
    var imageUrls by remember { mutableStateOf<List<String>>(entryToEdit?.imageUrls ?: emptyList()) }
    var expanded by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            viewModel.uploadImage(it, "guide") { url ->
                imageUrls = imageUrls + url
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (entryToEdit == null) "Add New Experience" else "Edit Experience") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title (e.g., Sunset Experience)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = summary, onValueChange = { summary = it }, label = { Text("Summary/Description") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
                
                Box {
                    OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
                        Text("Category: ${category.displayName}")
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        GuideCategory.entries.forEach { guideCategory ->
                            DropdownMenuItem(
                                text = { Text(guideCategory.displayName) },
                                onClick = {
                                    category = guideCategory
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Text("Images", style = MaterialTheme.typography.labelMedium)
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
            }
        },
        confirmButton = {
            Button(onClick = { 
                onConfirm((entryToEdit ?: GuideEntry()).copy(
                    title = title, 
                    summary = summary,
                    category = category,
                    imageUrls = imageUrls
                )) 
            }) {
                Text(if (entryToEdit == null) "Add" else "Update")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
