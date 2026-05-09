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
import com.gohahotel.connect.domain.model.GuideCategory
import com.gohahotel.connect.domain.model.GuideEntry
import com.gohahotel.connect.ui.theme.*

private fun guideCategoryColor(cat: GuideCategory): Color = when (cat) {
    GuideCategory.HERITAGE  -> Color(0xFFD4A843)
    GuideCategory.CHURCHES  -> Color(0xFF9B59B6)
    GuideCategory.MARKETS   -> Color(0xFFE29B4A)
    GuideCategory.NATURE    -> Color(0xFF50C878)
    GuideCategory.HISTORY   -> Color(0xFF4A90E2)
    GuideCategory.DINING    -> Color(0xFFE24A8A)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminContentManagement(
    viewModel: AdminViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val guideEntries by viewModel.guideEntries.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingEntry by remember { mutableStateOf<GuideEntry?>(null) }
    var entryToDelete by remember { mutableStateOf<GuideEntry?>(null) }

    Scaffold(
        containerColor = SurfaceDark,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Cultural Experiences", color = OnSurfaceDark,
                            fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                        Text("${guideEntries.size} entries",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF9B59B6).copy(alpha = 0.8f))
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
                            modifier = Modifier.size(36.dp).background(Color(0xFF9B59B6), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Add, null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceDark)
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(SurfaceDark, Color(0xFF0D0A1A), Color(0xFF050D18))))
                .padding(padding)
        ) {
            if (guideEntries.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.Place, null, tint = Color(0xFF9B59B6).copy(0.3f),
                        modifier = Modifier.size(64.dp))
                    Spacer(Modifier.height(16.dp))
                    Text("No experiences yet", color = OnSurfaceDark.copy(0.5f), fontWeight = FontWeight.Bold)
                    Text("Tap + to add a cultural experience", color = OnSurfaceDark.copy(0.3f),
                        style = MaterialTheme.typography.bodySmall)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(guideEntries, key = { it.id }) { entry ->
                        AdminCulturalCard(
                            entry = entry,
                            onEdit = { editingEntry = entry },
                            onDelete = { entryToDelete = entry }
                        )
                    }
                }
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

    editingEntry?.let { entry ->
        AddContentDialog(
            entryToEdit = entry,
            onDismiss = { editingEntry = null },
            onConfirm = { updated ->
                viewModel.saveGuideEntry(updated)
                editingEntry = null
            }
        )
    }

    entryToDelete?.let { entry ->
        AlertDialog(
            onDismissRequest = { entryToDelete = null },
            containerColor = Color(0xFF0D0A1A),
            title = { Text("Delete Experience?", color = Color(0xFFE24A4A), fontWeight = FontWeight.Bold) },
            text = { Text("Remove \"${entry.title}\" permanently?", color = OnSurfaceDark.copy(0.8f)) },
            confirmButton = {
                Button(
                    onClick = { viewModel.deleteGuideEntry(entry.id); entryToDelete = null },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE24A4A))
                ) { Text("Delete", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { entryToDelete = null }) { Text("Cancel", color = GoldPrimary) }
            }
        )
    }
}

@Composable
private fun AdminCulturalCard(entry: GuideEntry, onEdit: () -> Unit, onDelete: () -> Unit) {
    val accent = guideCategoryColor(entry.category)
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onEdit() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF100D1E)),
        border = BorderStroke(1.dp, accent.copy(0.25f))
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(76.dp).clip(RoundedCornerShape(14.dp)).background(accent.copy(0.1f))
            ) {
                val img = entry.imageUrls.firstOrNull() ?: entry.imageUrl
                if (img.isNotEmpty()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current).data(img).crossfade(true).build(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(entry.category.icon, fontSize = 28.sp)
                    }
                }
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(entry.title, fontWeight = FontWeight.ExtraBold, color = OnSurfaceDark, fontSize = 15.sp)
                Spacer(Modifier.height(4.dp))
                Surface(shape = RoundedCornerShape(6.dp), color = accent.copy(0.18f)) {
                    Text(
                        "${entry.category.icon} ${entry.category.displayName}",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = accent, fontWeight = FontWeight.Bold
                    )
                }
                if (entry.summary.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(entry.summary, style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceDark.copy(0.5f), maxLines = 2)
                }
                if (entry.distanceFromHotelKm > 0) {
                    Spacer(Modifier.height(4.dp))
                    Text("📍 ${entry.distanceFromHotelKm} km from hotel",
                        style = MaterialTheme.typography.labelSmall, color = accent.copy(0.7f))
                }
            }

            Column {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, null, tint = GoldLight)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, null, tint = Color(0xFFE24A4A).copy(0.8f))
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Add / Edit Cultural Experience Dialog
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun AddContentDialog(
    viewModel: AdminViewModel = hiltViewModel(),
    entryToEdit: GuideEntry? = null,
    onDismiss: () -> Unit,
    onConfirm: (GuideEntry) -> Unit
) {
    var title       by remember { mutableStateOf(entryToEdit?.title ?: "") }
    var summary     by remember { mutableStateOf(entryToEdit?.summary ?: "") }
    var content     by remember { mutableStateOf(entryToEdit?.content ?: "") }
    var category    by remember { mutableStateOf(entryToEdit?.category ?: GuideCategory.HERITAGE) }
    var distance    by remember { mutableStateOf(entryToEdit?.distanceFromHotelKm?.toString() ?: "") }
    var entryFee    by remember { mutableStateOf(entryToEdit?.entryFee ?: "") }
    var openingHours by remember { mutableStateOf(entryToEdit?.openingHours ?: "") }
    var imageUrls   by remember { mutableStateOf<List<String>>(entryToEdit?.imageUrls ?: emptyList()) }
    var validationError by remember { mutableStateOf<String?>(null) }

    // Dedicated upload state — not shared isLoading
    val isUploading by viewModel.isUploading.collectAsState()
    val uploadError by viewModel.uploadError.collectAsState()

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            validationError = null
            viewModel.uploadImage(it, "guide") { url -> imageUrls = imageUrls + url }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF100D1E),
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(36.dp).background(Color(0xFF9B59B6).copy(0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Default.Place, null, tint = Color(0xFF9B59B6), modifier = Modifier.size(20.dp)) }
                Spacer(Modifier.width(10.dp))
                Text(
                    if (entryToEdit == null) "Add Experience" else "Edit Experience",
                    color = Color(0xFFBB86FC), fontWeight = FontWeight.ExtraBold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Category selector
                Text("Category", style = MaterialTheme.typography.labelMedium, color = Color(0xFFBB86FC).copy(0.8f))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    GuideCategory.entries.forEach { cat ->
                        val accent = guideCategoryColor(cat)
                        val selected = category == cat
                        Surface(
                            onClick = { category = cat },
                            shape = RoundedCornerShape(10.dp),
                            color = if (selected) accent.copy(0.25f) else Color.White.copy(0.05f),
                            border = BorderStroke(1.dp, if (selected) accent else Color.White.copy(0.1f))
                        ) {
                            Text(
                                "${cat.icon} ${cat.displayName}",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (selected) accent else OnSurfaceDark.copy(0.5f),
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                ColoredTextField(value = title, onValueChange = { title = it; validationError = null },
                    label = "Title (e.g. Fasilides Castle)", icon = Icons.Default.Place)
                ColoredTextField(value = summary, onValueChange = { summary = it; validationError = null },
                    label = "Short Summary", icon = Icons.Default.Info, minLines = 2)
                ColoredTextField(value = content, onValueChange = { content = it },
                    label = "Full Description (optional)", icon = Icons.Default.Description, minLines = 3)

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ColoredTextField(value = distance, onValueChange = { distance = it },
                        label = "Distance (km)", icon = Icons.Default.NearMe, modifier = Modifier.weight(1f))
                    ColoredTextField(value = entryFee, onValueChange = { entryFee = it },
                        label = "Entry Fee", icon = Icons.Default.AttachMoney, modifier = Modifier.weight(1f))
                }
                ColoredTextField(value = openingHours, onValueChange = { openingHours = it },
                    label = "Opening Hours (e.g. 8am–5pm)", icon = Icons.Default.Schedule)

                // Images
                Text("Photos", style = MaterialTheme.typography.labelMedium, color = Color(0xFFBB86FC).copy(0.8f))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    imageUrls.forEach { url ->
                        Box {
                            AsyncImage(
                                model = url, contentDescription = null,
                                modifier = Modifier.size(64.dp).clip(RoundedCornerShape(10.dp)),
                                contentScale = ContentScale.Crop
                            )
                            IconButton(
                                onClick = { imageUrls = imageUrls - url },
                                modifier = Modifier.size(20.dp).align(Alignment.TopEnd)
                                    .background(Color(0xFFE24A4A), CircleShape)
                            ) {
                                Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(12.dp))
                            }
                        }
                    }
                    Box(
                        modifier = Modifier.size(64.dp).clip(RoundedCornerShape(10.dp))
                            .background(if (isUploading) Color(0xFF9B59B6).copy(0.05f) else Color(0xFF9B59B6).copy(0.12f))
                            .border(1.dp, Color(0xFF9B59B6).copy(if (isUploading) 0.1f else 0.3f), RoundedCornerShape(10.dp))
                            .clickable(enabled = !isUploading) { launcher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isUploading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp),
                                color = Color(0xFFBB86FC), strokeWidth = 2.dp)
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.AddAPhoto, null, tint = Color(0xFFBB86FC), modifier = Modifier.size(20.dp))
                                Text("Add", style = MaterialTheme.typography.labelSmall, color = Color(0xFFBB86FC))
                            }
                        }
                    }
                }
                if (isUploading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFFBB86FC), trackColor = Color(0xFF9B59B6).copy(0.15f))
                    Text("⏳ Uploading, please wait...",
                        color = Color(0xFFBB86FC), style = MaterialTheme.typography.labelSmall)
                }
                if (imageUrls.isNotEmpty()) {
                    Text("✅ ${imageUrls.size} photo(s) attached",
                        color = Color(0xFF50C878), style = MaterialTheme.typography.labelSmall)
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
                        title.isBlank()     -> validationError = "Title is required"
                        summary.isBlank()   -> validationError = "Summary is required"
                        isUploading         -> validationError = "Please wait — photo is still uploading"
                        imageUrls.isEmpty() -> validationError = "At least one photo is required"
                        else -> onConfirm(
                            (entryToEdit ?: GuideEntry()).copy(
                                title               = title,
                                summary             = summary,
                                content             = content,
                                category            = category,
                                distanceFromHotelKm = distance.toDoubleOrNull() ?: 0.0,
                                entryFee            = entryFee,
                                openingHours        = openingHours,
                                imageUrls           = imageUrls
                            )
                        )
                    }
                },
                enabled = !isUploading,
                colors  = ButtonDefaults.buttonColors(containerColor = Color(0xFF9B59B6)),
                shape   = RoundedCornerShape(12.dp)
            ) {
                if (isUploading) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Icon(if (entryToEdit == null) Icons.Default.Add else Icons.Default.Check,
                        null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(if (entryToEdit == null) "Add Experience" else "Update Experience",
                        color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = OnSurfaceDark.copy(0.6f)) }
        }
    )
}
