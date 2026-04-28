package com.gohahotel.connect.ui.rooms

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gohahotel.connect.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InRoomRequestScreen(
    viewModel: RoomViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var notes by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("") }

    LaunchedEffect(uiState.requestSubmitted) {
        if (uiState.requestSubmitted) {
            notes = ""
            selectedType = ""
            viewModel.resetBookingState()
        }
    }

    val requestTypes = listOf(
        Triple("Extra Towels",   Icons.Default.Spa,           "TOWELS"),
        Triple("Extra Pillows",  Icons.Default.BedtimeOff,    "PILLOWS"),
        Triple("Maintenance",    Icons.Default.Build,         "MAINTENANCE"),
        Triple("Housekeeping",   Icons.Default.CleaningServices, "HOUSEKEEPING"),
        Triple("Room Service",   Icons.Default.RoomService,   "ROOM_SERVICE"),
        Triple("Wake-Up Call",   Icons.Default.Alarm,         "WAKE_UP"),
        Triple("Extra Blanket",  Icons.Default.BeachAccess,   "BLANKET"),
        Triple("Toiletries",     Icons.Default.LocalPharmacy, "TOILETRIES"),
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("In-Room Request", fontWeight = FontWeight.Bold)
                        Text("We'll be there shortly",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(0.6f))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (uiState.requestSubmitted) {
                Card(
                    shape  = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SuccessGreen.copy(0.1f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, null, tint = SuccessGreen,
                            modifier = Modifier.size(32.dp))
                        Column {
                            Text("Request Submitted!", fontWeight = FontWeight.Bold,
                                color = SuccessGreen)
                            Text("Our team will be with you shortly.",
                                style = MaterialTheme.typography.bodySmall,
                                color = SuccessGreen.copy(0.8f))
                        }
                    }
                }
            }

            Text("What do you need?", style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold)

            requestTypes.chunked(2).forEach { row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    row.forEach { (label, icon, type) ->
                        RequestTypeCard(
                            label    = label,
                            icon     = icon,
                            selected = selectedType == type,
                            onClick  = { selectedType = if (selectedType == type) "" else type },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (row.size == 1) Spacer(Modifier.weight(1f))
                }
            }

            Text("Additional Notes (optional)", style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium)

            OutlinedTextField(
                value       = notes,
                onValueChange = { notes = it },
                placeholder = { Text("Any specific instructions...") },
                modifier    = Modifier.fillMaxWidth().height(100.dp),
                shape       = RoundedCornerShape(14.dp),
                maxLines    = 4
            )

            if (uiState.isLoading) LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(), color = GoldPrimary)

            Button(
                onClick = {
                    if (selectedType.isNotBlank()) {
                        viewModel.submitInRoomRequest(
                            roomNumber = "101",  // Replace with actual room from session
                            guestId    = "guest",
                            type       = selectedType,
                            notes      = notes
                        )
                    }
                },
                enabled   = selectedType.isNotBlank() && !uiState.isLoading,
                modifier  = Modifier.fillMaxWidth().height(54.dp),
                shape     = RoundedCornerShape(14.dp),
                colors    = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
            ) {
                Icon(Icons.Default.Send, null, tint = SurfaceDark, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Submit Request", color = SurfaceDark, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(8.dp))
            Text(
                "Requests are typically fulfilled within 15–20 minutes. For urgent assistance, dial 0 from your room phone.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(0.5f)
            )
        }
    }
}

@Composable
private fun RequestTypeCard(
    label: String, icon: ImageVector, selected: Boolean,
    onClick: () -> Unit, modifier: Modifier = Modifier
) {
    Card(
        onClick  = onClick,
        modifier = modifier.height(80.dp),
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(
            containerColor = if (selected) GoldPrimary else CardDark
        ),
        border   = if (selected) null else
            BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(0.2f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(10.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(icon, null, Modifier.size(20.dp),
                tint = if (selected) SurfaceDark else GoldPrimary)
            Text(label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = if (selected) SurfaceDark else MaterialTheme.colorScheme.onSurface)
        }
    }
}
