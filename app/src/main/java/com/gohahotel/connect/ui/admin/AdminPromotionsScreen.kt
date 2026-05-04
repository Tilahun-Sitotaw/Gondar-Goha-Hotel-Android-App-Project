package com.gohahotel.connect.ui.admin

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.VideoCall
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
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.gohahotel.connect.domain.model.Promotion
import com.gohahotel.connect.domain.model.PromotionType
import com.gohahotel.connect.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPromotionsScreen(
    onBack: () -> Unit,
    viewModel: AdminViewModel = hiltViewModel()
) {
    val promotions by viewModel.promotions.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Promotions & Events") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, null, tint = GoldPrimary)
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(SurfaceDark, Color(0xFF0A1424), Color.Black)))
                .padding(padding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Text(
                    "Manage Daily Events, Promotions, and Videos",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp),
                    color = GoldPrimary.copy(alpha = 0.6f)
                )
            
            if (promotions.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No active promotions. Click + to add.", color = OnSurfaceDark.copy(alpha = 0.4f))
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(promotions) { promo ->
                        PromotionAdminCard(
                            promotion = promo,
                            onDelete = { viewModel.deletePromotion(promo.id) }
                        )
                    }
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
}

@Composable
fun PromotionAdminCard(promotion: Promotion, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark)
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(180.dp).background(Color.White.copy(0.05f))) {
                if (promotion.imageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(promotion.imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Add, null, tint = GoldPrimary.copy(0.2f), modifier = Modifier.size(48.dp))
                    }
                }
                if (promotion.videoUrl.isNotEmpty()) {
                    Icon(
                        Icons.Default.PlayCircle,
                        null,
                        modifier = Modifier.align(Alignment.Center).size(48.dp),
                        tint = Color.White
                    )
                }
            }
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(promotion.title, fontWeight = FontWeight.Bold, color = GoldPrimary)
                    Text(promotion.description, style = MaterialTheme.typography.bodySmall, color = OnSurfaceDark.copy(alpha = 0.7f))
                    Text("Type: ${promotion.type}", style = MaterialTheme.typography.labelSmall, color = GoldLight)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.6f))
                }
            }
        }
    }
}

@Composable
fun AddPromotionDialog(
    viewModel: AdminViewModel = hiltViewModel(),
    onDismiss: () -> Unit,
    onConfirm: (Promotion) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var videoUrl by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(PromotionType.EVENT) }

    val imageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { viewModel.uploadImage(it, "promotions") { url -> imageUrl = url } }
    }

    val videoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { viewModel.uploadVideo(it, "promotions") { url -> videoUrl = url } }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Event/Promo", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
                
                Text("Type:", style = MaterialTheme.typography.labelLarge)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PromotionType.entries.forEach { promoType ->
                        FilterChip(
                            selected = type == promoType,
                            onClick = { type = promoType },
                            label = { Text(promoType.name, fontSize = 10.sp) }
                        )
                    }
                }

                Text("Image", style = MaterialTheme.typography.labelMedium)
                if (imageUrl.isNotEmpty()) {
                    AsyncImage(model = imageUrl, contentDescription = null, modifier = Modifier.fillMaxWidth().height(100.dp).clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop)
                }
                Button(onClick = { imageLauncher.launch("image/*") }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.AddAPhoto, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Upload Image")
                }

                Text("Video (Optional)", style = MaterialTheme.typography.labelMedium)
                if (videoUrl.isNotEmpty()) {
                    Text("Video uploaded: ${videoUrl.take(30)}...", style = MaterialTheme.typography.bodySmall, color = SuccessGreen)
                }
                Button(onClick = { videoLauncher.launch("video/*") }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.VideoCall, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Upload Video")
                }
            }
        },
        confirmButton = {
            var validationError by remember { mutableStateOf<String?>(null) }
            
            LaunchedEffect(imageUrl, title) {
                validationError = null
            }
            
            Column(horizontalAlignment = Alignment.End) {
                if (validationError != null) {
                    Text(validationError!!, color = Color.Red, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(bottom = 4.dp))
                }
                Button(onClick = { 
                    if (title.isBlank()) {
                        validationError = "Title is required"
                        return@Button
                    }
                    if (imageUrl.isBlank() && !viewModel.isLoading.value) {
                        validationError = "Image is required"
                        return@Button
                    }
                    onConfirm(Promotion(
                        title = title, 
                        description = description, 
                        imageUrl = imageUrl, 
                        videoUrl = videoUrl,
                        type = type
                    )) 
                }) {
                    if (viewModel.isLoading.value) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = SurfaceDark, strokeWidth = 2.dp)
                    } else {
                        Text("Add Promotion")
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
