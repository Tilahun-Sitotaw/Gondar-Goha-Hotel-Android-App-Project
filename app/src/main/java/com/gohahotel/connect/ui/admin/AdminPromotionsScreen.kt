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
import com.gohahotel.connect.domain.model.Promotion
import com.gohahotel.connect.domain.model.PromotionType
import com.gohahotel.connect.ui.components.VideoPlayer
import com.gohahotel.connect.ui.theme.*

private fun promoTypeColor(type: PromotionType): Color = when (type) {
    PromotionType.EVENT     -> Color(0xFFE29B4A)
    PromotionType.PROMOTION -> Color(0xFF50C878)
    PromotionType.CULTURAL  -> Color(0xFF9B59B6)
    PromotionType.VIDEO     -> Color(0xFF4A90E2)
}

private fun promoTypeIcon(type: PromotionType): String = when (type) {
    PromotionType.EVENT     -> "🎉"
    PromotionType.PROMOTION -> "🏷️"
    PromotionType.CULTURAL  -> "🎭"
    PromotionType.VIDEO     -> "🎬"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPromotionsScreen(
    onBack: () -> Unit,
    viewModel: AdminViewModel = hiltViewModel()
) {
    val promotions by viewModel.promotions.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingPromo by remember { mutableStateOf<Promotion?>(null) }
    var promoToDelete by remember { mutableStateOf<Promotion?>(null) }

    Scaffold(
        containerColor = SurfaceDark,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Events & Promotions", color = OnSurfaceDark,
                            fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                        Text("${promotions.size} active · ${promotions.count { it.isActive }} live",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFE29B4A).copy(alpha = 0.8f))
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
                            modifier = Modifier.size(36.dp).background(Color(0xFFE29B4A), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Add, null, tint = Color.Black, modifier = Modifier.size(20.dp))
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
                .background(Brush.verticalGradient(listOf(SurfaceDark, Color(0xFF0F0A05), Color(0xFF050D18))))
                .padding(padding)
        ) {
            if (promotions.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("🎉", fontSize = 56.sp)
                    Spacer(Modifier.height(16.dp))
                    Text("No events yet", color = OnSurfaceDark.copy(0.5f), fontWeight = FontWeight.Bold)
                    Text("Tap + to add an event or promotion", color = OnSurfaceDark.copy(0.3f),
                        style = MaterialTheme.typography.bodySmall)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(promotions, key = { it.id }) { promo ->
                        AdminPromoCard(
                            promotion = promo,
                            onEdit = { editingPromo = promo },
                            onDelete = { promoToDelete = promo }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddPromotionDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { promo ->
                viewModel.savePromotion(promo)
                showAddDialog = false
            }
        )
    }

    editingPromo?.let { promo ->
        AddPromotionDialog(
            promoToEdit = promo,
            onDismiss = { editingPromo = null },
            onConfirm = { updated ->
                viewModel.savePromotion(updated)
                editingPromo = null
            }
        )
    }

    promoToDelete?.let { promo ->
        AlertDialog(
            onDismissRequest = { promoToDelete = null },
            containerColor = Color(0xFF1A1005),
            title = { Text("Delete Event?", color = Color(0xFFE24A4A), fontWeight = FontWeight.Bold) },
            text = { Text("Remove \"${promo.title}\" permanently?", color = OnSurfaceDark.copy(0.8f)) },
            confirmButton = {
                Button(
                    onClick = { viewModel.deletePromotion(promo.id); promoToDelete = null },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE24A4A))
                ) { Text("Delete", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { promoToDelete = null }) { Text("Cancel", color = GoldPrimary) }
            }
        )
    }
}

@Composable
private fun AdminPromoCard(promotion: Promotion, onEdit: () -> Unit, onDelete: () -> Unit) {
    val accent = promoTypeColor(promotion.type)
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onEdit() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF141008)),
        border = BorderStroke(1.dp, accent.copy(0.3f))
    ) {
        Column {
            // Banner — show video with controls if available, otherwise image
            Box(
                modifier = Modifier.fillMaxWidth().height(180.dp)
                    .background(accent.copy(0.08f))
            ) {
                if (promotion.videoUrl.isNotEmpty()) {
                    VideoPlayer(
                        videoUrl     = promotion.videoUrl,
                        modifier     = Modifier.fillMaxSize(),
                        autoPlay     = false,
                        muted        = true,
                        showControls = true
                    )
                } else if (promotion.imageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(promotion.imageUrl).crossfade(true).build(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier.fillMaxSize()
                            .background(Brush.verticalGradient(
                                listOf(Color.Transparent, Color.Black.copy(0.6f))
                            ))
                    )
                } else {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(promoTypeIcon(promotion.type), fontSize = 48.sp)
                    }
                }
                // Type badge
                Surface(
                    modifier = Modifier.align(Alignment.TopStart).padding(10.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = accent.copy(0.85f)
                ) {
                    Text(
                        "${promoTypeIcon(promotion.type)} ${promotion.type.name}",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Black, fontWeight = FontWeight.Bold
                    )
                }
                // Active badge
                Surface(
                    modifier = Modifier.align(Alignment.TopEnd).padding(10.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = if (promotion.isActive) Color(0xFF50C878).copy(0.85f) else Color(0xFFE24A4A).copy(0.85f)
                ) {
                    Text(
                        if (promotion.isActive) "● LIVE" else "● OFF",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Black, fontWeight = FontWeight.Bold
                    )
                }
            }

            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(promotion.title, fontWeight = FontWeight.ExtraBold,
                        color = OnSurfaceDark, fontSize = 15.sp)
                    if (promotion.description.isNotBlank()) {
                        Spacer(Modifier.height(4.dp))
                        Text(promotion.description, style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceDark.copy(0.6f), maxLines = 2)
                    }
                    if (promotion.date.isNotBlank()) {
                        Spacer(Modifier.height(4.dp))
                        Text("📅 ${promotion.date}", style = MaterialTheme.typography.labelSmall,
                            color = accent.copy(0.8f))
                    }
                }
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
//  Add / Edit Promotion Dialog
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun AddPromotionDialog(
    viewModel: AdminViewModel = hiltViewModel(),
    promoToEdit: Promotion? = null,
    onDismiss: () -> Unit,
    onConfirm: (Promotion) -> Unit
) {
    var title       by remember { mutableStateOf(promoToEdit?.title ?: "") }
    var description by remember { mutableStateOf(promoToEdit?.description ?: "") }
    var date        by remember { mutableStateOf(promoToEdit?.date ?: "") }
    var imageUrl    by remember { mutableStateOf(promoToEdit?.imageUrl ?: "") }
    var videoUrl    by remember { mutableStateOf(promoToEdit?.videoUrl ?: "") }
    var type        by remember { mutableStateOf(promoToEdit?.type ?: PromotionType.EVENT) }
    var isActive    by remember { mutableStateOf(promoToEdit?.isActive ?: true) }
    var validationError by remember { mutableStateOf<String?>(null) }
    // Dedicated upload state
    val isUploading by viewModel.isUploading.collectAsState()
    val uploadError by viewModel.uploadError.collectAsState()

    val imageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            validationError = null
            viewModel.uploadImage(it, "promotions") { url -> imageUrl = url }
        }
    }
    val videoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            validationError = null
            viewModel.uploadVideo(it, "promotions") { url -> videoUrl = url }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF141008),
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(36.dp).background(Color(0xFFE29B4A).copy(0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) { Text("🎉", fontSize = 18.sp) }
                Spacer(Modifier.width(10.dp))
                Text(
                    if (promoToEdit == null) "Add Event / Promo" else "Edit Event / Promo",
                    color = Color(0xFFE29B4A), fontWeight = FontWeight.ExtraBold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Type selector
                Text("Type", style = MaterialTheme.typography.labelMedium, color = Color(0xFFE29B4A).copy(0.8f))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    PromotionType.entries.forEach { pt ->
                        val accent = promoTypeColor(pt)
                        val selected = type == pt
                        Surface(
                            onClick = { type = pt },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            color = if (selected) accent.copy(0.25f) else Color.White.copy(0.05f),
                            border = BorderStroke(1.dp, if (selected) accent else Color.White.copy(0.1f))
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(promoTypeIcon(pt), fontSize = 16.sp)
                                Text(pt.name, style = MaterialTheme.typography.labelSmall,
                                    color = if (selected) accent else OnSurfaceDark.copy(0.5f),
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
                            }
                        }
                    }
                }

                ColoredTextField(value = title, onValueChange = { title = it; validationError = null },
                    label = "Title", icon = Icons.Default.Title)
                ColoredTextField(value = description, onValueChange = { description = it },
                    label = "Description", icon = Icons.Default.Description, minLines = 2)
                ColoredTextField(value = date, onValueChange = { date = it },
                    label = "Date (e.g. June 15, 2026)", icon = Icons.Default.CalendarToday)

                // Active toggle
                ToggleChip("✅ Active / Live", isActive, Color(0xFF50C878)) { isActive = it }

                // Image
                Text("Banner Image", style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFFE29B4A).copy(0.8f))
                if (imageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = imageUrl, contentDescription = null,
                        modifier = Modifier.fillMaxWidth().height(130.dp).clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
                Button(
                    onClick = { imageLauncher.launch("image/*") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE29B4A).copy(0.15f),
                        contentColor = Color(0xFFE29B4A)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFFE29B4A).copy(0.4f))
                ) {
                    Icon(Icons.Default.AddAPhoto, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(if (imageUrl.isEmpty()) "Upload Banner Image" else "Change Image")
                }

                // Video (optional)
                Text("Video (Optional)", style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFF4A90E2).copy(0.8f))
                if (videoUrl.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF4A90E2).copy(0.1f),
                        border = BorderStroke(1.dp, Color(0xFF4A90E2).copy(0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.PlayCircle, null, tint = Color(0xFF4A90E2),
                                modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Video uploaded ✅", color = Color(0xFF4A90E2),
                                style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
                Button(
                    onClick = { videoLauncher.launch("video/*") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4A90E2).copy(0.15f),
                        contentColor = Color(0xFF4A90E2)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFF4A90E2).copy(0.4f))
                ) {
                    Icon(Icons.Default.VideoCall, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(if (videoUrl.isEmpty()) "Upload Video" else "Change Video")
                }

                if (isUploading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFFE29B4A), trackColor = Color(0xFFE29B4A).copy(0.15f))
                    Text("⏳ Uploading, please wait...",
                        color = Color(0xFFE29B4A), style = MaterialTheme.typography.labelSmall)
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
                        title.isBlank()   -> validationError = "Title is required"
                        isUploading       -> validationError = "Please wait — upload in progress"
                        imageUrl.isBlank() -> validationError = "Banner image is required"
                        else -> onConfirm(
                            (promoToEdit ?: Promotion()).copy(
                                title = title,
                                description = description,
                                date = date,
                                imageUrl = imageUrl,
                                videoUrl = videoUrl,
                                type = type,
                                isActive = isActive
                            )
                        )
                    }
                },
                enabled = !isUploading,
                colors  = ButtonDefaults.buttonColors(containerColor = Color(0xFFE29B4A)),
                shape   = RoundedCornerShape(12.dp)
            ) {
                if (isUploading) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.Black, strokeWidth = 2.dp)
                } else {
                    Icon(if (promoToEdit == null) Icons.Default.Add else Icons.Default.Check,
                        null, tint = Color.Black, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(if (promoToEdit == null) "Add Event" else "Update Event",
                        color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = OnSurfaceDark.copy(0.6f)) }
        }
    )
}
