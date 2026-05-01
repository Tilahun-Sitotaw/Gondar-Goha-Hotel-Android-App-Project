package com.gohahotel.connect.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
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
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            Text(
                "Manage Daily Events, Promotions, and Videos",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(16.dp),
                color = OnSurfaceDark.copy(alpha = 0.6f)
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
        colors = CardDefaults.cardColors(containerColor = CardDark)
    ) {
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

@Composable
fun AddPromotionDialog(onDismiss: () -> Unit, onConfirm: (Promotion) -> Unit) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var videoUrl by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(PromotionType.EVENT) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Event/Promo", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") })
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") })
                OutlinedTextField(value = imageUrl, onValueChange = { imageUrl = it }, label = { Text("Image URL") })
                OutlinedTextField(value = videoUrl, onValueChange = { videoUrl = it }, label = { Text("Video URL (Optional)") })
                
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
            }
        },
        confirmButton = {
            Button(onClick = { 
                onConfirm(Promotion(
                    title = title, 
                    description = description, 
                    imageUrl = imageUrl, 
                    videoUrl = videoUrl,
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
