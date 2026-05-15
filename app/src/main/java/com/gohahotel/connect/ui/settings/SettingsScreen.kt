package com.gohahotel.connect.ui.settings

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gohahotel.connect.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onLogout: () -> Unit,
    onNavigateToAdmin: () -> Unit,
    onNavigateToStaff: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }

    val languages = listOf(
        Triple("en", "English", "🇬🇧"),
        Triple("am", "አማርኛ (Amharic)", "🇪🇹"),
        Triple("fr", "Français (French)", "🇫🇷")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Profile header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(TealDark, SurfaceDark)))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier.size(72.dp)
                            .background(Brush.radialGradient(listOf(GoldLight, GoldPrimary)), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(uiState.guestName.take(1).uppercase(),
                            fontSize = 32.sp, fontWeight = FontWeight.Bold, color = SurfaceDark)
                    }
                    Text(uiState.guestName, style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold, color = GoldPrimary)
                    Text(uiState.guestEmail, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(0.6f))
                }
            }

            Spacer(Modifier.height(16.dp))

            // Admin Access
            if (uiState.userRole == "ADMIN") {
                SectionHeader("🛠️ Administration")
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    shape    = RoundedCornerShape(18.dp),
                    colors   = CardDefaults.cardColors(containerColor = GoldPrimary.copy(alpha = 0.15f)),
                    onClick  = onNavigateToAdmin
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.AdminPanelSettings, null, tint = GoldPrimary)
                        Spacer(Modifier.width(16.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Admin Dashboard", fontWeight = FontWeight.Bold, color = GoldPrimary)
                            Text("Manage rooms, menu and system", style = MaterialTheme.typography.labelSmall, color = GoldPrimary.copy(alpha = 0.7f))
                        }
                        Icon(Icons.Default.ArrowForwardIos, null, Modifier.size(14.dp), tint = GoldPrimary)
                    }
                }
                Spacer(Modifier.height(16.dp))
            } else if (uiState.userRole in listOf("STAFF", "KITCHEN", "RECEPTION", "HOUSEKEEPING")) {
                val title = when (uiState.userRole) {
                    "KITCHEN" -> "Kitchen Operations"
                    "RECEPTION" -> "Front Desk & Reception"
                    "HOUSEKEEPING" -> "Housekeeping"
                    else -> "Staff Dashboard"
                }
                val icon = when (uiState.userRole) {
                    "KITCHEN" -> Icons.Default.RestaurantMenu
                    "RECEPTION" -> Icons.Default.Desk
                    "HOUSEKEEPING" -> Icons.Default.CleaningServices
                    else -> Icons.Default.Badge
                }
                SectionHeader("🛎️ Staff Operations")
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    shape    = RoundedCornerShape(18.dp),
                    colors   = CardDefaults.cardColors(containerColor = TealPrimary.copy(alpha = 0.15f)),
                    onClick  = onNavigateToStaff
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(icon, null, tint = TealPrimary)
                        Spacer(Modifier.width(16.dp))
                        Column(Modifier.weight(1f)) {
                            Text(title, fontWeight = FontWeight.Bold, color = TealLight)
                            Text("Manage assigned hotel operations", style = MaterialTheme.typography.labelSmall, color = TealLight.copy(alpha = 0.7f))
                        }
                        Icon(Icons.Default.ArrowForwardIos, null, Modifier.size(14.dp), tint = TealPrimary)
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            // Language section
            SectionHeader("🌍 Language")
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape    = RoundedCornerShape(18.dp),
                colors   = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column {
                    languages.forEachIndexed { i, (code, name, flag) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.setLanguage(code) }
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment     = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically) {
                                Text(flag, fontSize = 24.sp)
                                Text(name, style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            if (uiState.selectedLanguage == code) {
                                Icon(Icons.Default.CheckCircle, null,
                                    tint = GoldPrimary, modifier = Modifier.size(20.dp))
                            }
                        }
                        if (i < languages.lastIndex) {
                            HorizontalDivider(Modifier.padding(horizontal = 16.dp),
                                color = MaterialTheme.colorScheme.outline.copy(0.15f))
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // About section
            SectionHeader("ℹ️ About")
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape    = RoundedCornerShape(18.dp),
                colors   = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column {
                    SettingsRow(Icons.Default.Hotel, "Hotel", "Goha Hotel, Gondar")
                    HorizontalDivider(Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outline.copy(0.15f))
                    SettingsRow(Icons.Default.PhoneInTalk, "Front Desk", "Extension 100")
                    HorizontalDivider(Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outline.copy(0.15f))
                    SettingsRow(Icons.Default.Info, "App Version", uiState.appVersion)
                    HorizontalDivider(Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outline.copy(0.15f))
                    SettingsRow(Icons.Default.Code, "Technology", "Kotlin · Jetpack Compose")
                }
            }

            Spacer(Modifier.height(16.dp))

            // Support
            SectionHeader("🛎️ Support")
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape    = RoundedCornerShape(18.dp),
                colors   = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column {
                    SettingsRow(Icons.Default.SupportAgent, "24/7 Concierge", "Chat with us anytime")
                    HorizontalDivider(Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outline.copy(0.15f))
                    SettingsRow(Icons.Default.Emergency, "Emergency", "Dial 0 from room phone")
                }
            }

            Spacer(Modifier.height(24.dp))

            // Logout
            Button(
                onClick  = { showLogoutDialog = true },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(54.dp),
                shape    = RoundedCornerShape(14.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = ErrorRed.copy(0.12f))
            ) {
                Icon(Icons.Default.Logout, null, tint = ErrorRed, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Sign Out", color = ErrorRed, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(32.dp))

            val currentDateTime = remember {
                SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(Date())
            }

            Text(
                text = "Today: $currentDateTime",
                style = MaterialTheme.typography.labelMedium,
                color = GoldPrimary,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "© ${Calendar.getInstance().get(Calendar.YEAR)} Goha Hotel · Gondar, Ethiopia\nAll rights reserved.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onBackground.copy(0.35f),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(16.dp))
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            icon  = { Icon(Icons.Default.Logout, null, tint = ErrorRed) },
            title = { Text("Sign Out", fontWeight = FontWeight.Bold) },
            text  = { Text("Are you sure you want to sign out from Goha Hotel Connect?") },
            confirmButton = {
                Button(onClick = { viewModel.logout(onLogout) },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                    shape  = RoundedCornerShape(12.dp)) {
                    Text("Sign Out", color = MaterialTheme.colorScheme.onPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text("Cancel") }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(title, style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold, color = GoldPrimary,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp))
}

@Composable
private fun SettingsRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, Modifier.size(18.dp), tint = GoldPrimary)
            Text(label, style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(value, style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.7f))
    }
}
