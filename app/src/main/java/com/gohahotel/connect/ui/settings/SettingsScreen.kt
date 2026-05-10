package com.gohahotel.connect.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.gohahotel.connect.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

// -----------------------------------------------------------------------------
// SettingsScreen
// -----------------------------------------------------------------------------

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

    // Photo picker launcher
    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.uploadProfilePhoto(it) }
    }

    // Auto-clear messages after 4 seconds
    LaunchedEffect(uiState.successMessage, uiState.errorMessage) {
        if (uiState.successMessage != null || uiState.errorMessage != null) {
            kotlinx.coroutines.delay(4_000)
            viewModel.clearMessages()
        }
    }

    // Logout confirmation dialog
    if (showLogoutDialog) {
        LogoutConfirmDialog(
            onConfirm = {
                showLogoutDialog = false
                viewModel.logout(onLogout)
            },
            onDismiss = { showLogoutDialog = false }
        )
    }

    val bgGradient = Brush.verticalGradient(
        listOf(Color(0xFF071520), Color(0xFF060F1C), Color(0xFF050D18))
    )

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Settings",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            color = OnSurfaceDark
                        )
                        Text(
                            "Account & Preferences",
                            style = MaterialTheme.typography.labelSmall,
                            color = GoldPrimary.copy(alpha = 0.7f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = GoldPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF071520).copy(alpha = 0.97f),
                    titleContentColor = OnSurfaceDark
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(bgGradient)
        ) {
            // Ambient glow
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            listOf(GoldPrimary.copy(0.04f), Color.Transparent),
                            radius = 1200f
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
            ) {
                // -- Success / Error banners ------------------------------------
                AnimatedVisibility(
                    visible = uiState.successMessage != null,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    uiState.successMessage?.let { msg ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = SuccessGreen.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = SuccessGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    msg,
                                    color = SuccessGreen,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                AnimatedVisibility(
                    visible = uiState.errorMessage != null,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    uiState.errorMessage?.let { msg ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = ErrorRed.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, ErrorRed.copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    Icons.Default.ErrorOutline,
                                    contentDescription = null,
                                    tint = ErrorRed,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    msg,
                                    color = ErrorRed,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                // -- Profile Header --------------------------------------------
                ProfileHeader(
                    uiState = uiState,
                    onPickPhoto = { photoLauncher.launch("image/*") }
                )

                Spacer(Modifier.height(16.dp))

                // -- Edit Profile ----------------------------------------------
                if (uiState.guestEmail.isNotBlank()) {
                    EditProfileSection(
                        uiState = uiState,
                        onSave = { name, phone, addr ->
                            viewModel.updateDisplayName(name)
                            viewModel.updatePhone(phone)
                            viewModel.updateAddress(addr)
                        }
                    )
                    Spacer(Modifier.height(12.dp))
                }

                // -- Admin / Staff card ----------------------------------------
                if (uiState.userRole == "ADMIN" ||
                    uiState.userRole in listOf("STAFF", "KITCHEN", "RECEPTION", "HOUSEKEEPING")) {
                    AdminStaffCard(
                        role = uiState.userRole,
                        onNavigateToAdmin = onNavigateToAdmin,
                        onNavigateToStaff = onNavigateToStaff
                    )
                    Spacer(Modifier.height(12.dp))
                }

                // -- Notifications ---------------------------------------------
                SettingsSectionCard(title = "NOTIFICATIONS") {
                    NotificationsRow(
                        enabled = uiState.notificationsOn,
                        onToggle = { viewModel.toggleNotifications(it) }
                    )
                }
                Spacer(Modifier.height(12.dp))

                // -- Language --------------------------------------------------
                SettingsSectionCard(title = "LANGUAGE") {
                    LanguageSection(
                        selected = uiState.selectedLanguage,
                        onSelect = { viewModel.setLanguage(it) }
                    )
                }
                Spacer(Modifier.height(12.dp))

                // -- About -----------------------------------------------------
                SettingsSectionCard(title = "ABOUT GOHA HOTEL") {
                    AboutSection(appVersion = uiState.appVersion)
                }
                Spacer(Modifier.height(12.dp))

                // -- Support ---------------------------------------------------
                SettingsSectionCard(title = "SUPPORT") {
                    SupportSection()
                }
                Spacer(Modifier.height(12.dp))

                // -- Privacy & Legal -------------------------------------------
                SettingsSectionCard(title = "PRIVACY & LEGAL") {
                    PrivacyLegalSection()
                }
                Spacer(Modifier.height(20.dp))

                // -- Sign Out --------------------------------------------------
                Button(
                    onClick = { showLogoutDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ErrorRed.copy(alpha = 0.15f)
                    ),
                    border = BorderStroke(1.dp, ErrorRed.copy(alpha = 0.5f))
                ) {
                    Icon(
                        Icons.Default.Logout,
                        contentDescription = null,
                        tint = ErrorRed,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "Sign Out",
                        color = ErrorRed,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                Spacer(Modifier.height(24.dp))

                // -- Footer ----------------------------------------------------
                SettingsFooter()

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

// -----------------------------------------------------------------------------
// ProfileHeader
// -----------------------------------------------------------------------------

@Composable
private fun ProfileHeader(
    uiState: SettingsUiState,
    onPickPhoto: () -> Unit
) {
    val roleColor = when (uiState.userRole) {
        "ADMIN" -> GoldPrimary
        "STAFF" -> TealPrimary
        else    -> OnSurfaceDark.copy(alpha = 0.5f)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0C1E30), Color(0xFF071520))
                )
            )
            .padding(vertical = 28.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Avatar with camera overlay
            Box(
                modifier = Modifier.size(96.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                // Avatar circle
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .border(2.dp, GoldPrimary.copy(alpha = 0.6f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (uiState.profilePhotoUrl.isNotBlank()) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(uiState.profilePhotoUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Profile photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.linearGradient(listOf(GoldDark, GoldPrimary)),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = uiState.guestName.firstOrNull()?.uppercaseChar()?.toString() ?: "G",
                                color = Color(0xFF050D18),
                                fontSize = 38.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    // Upload overlay
                    if (uiState.isUploading) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.55f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = GoldPrimary,
                                modifier = Modifier.size(28.dp),
                                strokeWidth = 2.5.dp
                            )
                        }
                    }
                }

                // Camera button
                Surface(
                    onClick = onPickPhoto,
                    modifier = Modifier.size(30.dp),
                    shape = CircleShape,
                    color = GoldPrimary,
                    border = BorderStroke(2.dp, Color(0xFF071520))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.CameraAlt,
                            contentDescription = "Change photo",
                            tint = Color(0xFF050D18),
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }

            // Name
            Text(
                text = uiState.guestName.ifBlank { "Guest" },
                style = MaterialTheme.typography.titleLarge,
                color = GoldPrimary,
                fontWeight = FontWeight.Bold
            )

            // Email
            if (uiState.guestEmail.isNotBlank()) {
                Text(
                    text = uiState.guestEmail,
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceDark.copy(alpha = 0.6f)
                )
            }

            // Role badge
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = roleColor.copy(alpha = 0.15f),
                border = BorderStroke(1.dp, roleColor.copy(alpha = 0.4f))
            ) {
                Text(
                    text = uiState.userRole,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = roleColor,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )
            }
        }
    }
}

// -----------------------------------------------------------------------------
// EditProfileSection
// -----------------------------------------------------------------------------

@Composable
private fun EditProfileSection(
    uiState: SettingsUiState,
    onSave: (name: String, phone: String, addr: String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var editName    by remember(uiState.guestName)    { mutableStateOf(uiState.guestName) }
    var editPhone   by remember(uiState.phoneNumber)  { mutableStateOf(uiState.phoneNumber) }
    var editAddress by remember(uiState.address)      { mutableStateOf(uiState.address) }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor        = OnSurfaceDark,
        unfocusedTextColor      = OnSurfaceDark.copy(alpha = 0.9f),
        focusedBorderColor      = GoldPrimary,
        unfocusedBorderColor    = Color.White.copy(alpha = 0.12f),
        focusedLabelColor       = GoldPrimary,
        unfocusedLabelColor     = OnSurfaceDark.copy(alpha = 0.4f),
        focusedLeadingIconColor    = GoldPrimary,
        unfocusedLeadingIconColor  = GoldPrimary.copy(alpha = 0.4f),
        cursorColor             = GoldPrimary,
        focusedContainerColor   = Color.Transparent,
        unfocusedContainerColor = Color.Transparent
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFF0C1A28),
        border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.15f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = null,
                        tint = GoldPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        "EDIT PROFILE",
                        style = MaterialTheme.typography.labelLarge,
                        color = GoldPrimary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (expanded) "Collapse" else "Expand",
                        tint = GoldPrimary.copy(alpha = 0.7f)
                    )
                }
            }

            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + expandVertically(),
                exit  = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier.padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Display Name") },
                        leadingIcon = {
                            Icon(Icons.Default.Person, null, modifier = Modifier.size(18.dp))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = fieldColors
                    )
                    OutlinedTextField(
                        value = editPhone,
                        onValueChange = { editPhone = it },
                        label = { Text("Phone Number") },
                        leadingIcon = {
                            Icon(Icons.Default.Phone, null, modifier = Modifier.size(18.dp))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = fieldColors
                    )
                    OutlinedTextField(
                        value = editAddress,
                        onValueChange = { editAddress = it },
                        label = { Text("Address") },
                        leadingIcon = {
                            Icon(Icons.Default.LocationOn, null, modifier = Modifier.size(18.dp))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = fieldColors
                    )

                    Button(
                        onClick = { onSave(editName, editPhone, editAddress) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                        enabled = !uiState.isLoading
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                color = Color(0xFF050D18),
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                Icons.Default.Save,
                                contentDescription = null,
                                tint = Color(0xFF050D18),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Save Changes",
                                color = Color(0xFF050D18),
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// AdminStaffCard
// -----------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminStaffCard(
    role: String,
    onNavigateToAdmin: () -> Unit,
    onNavigateToStaff: () -> Unit
) {
    val isAdmin = role == "ADMIN"
    val accentColor = if (isAdmin) GoldPrimary else TealPrimary
    val icon = when (role) {
        "ADMIN"        -> Icons.Default.AdminPanelSettings
        "KITCHEN"      -> Icons.Default.Restaurant
        "RECEPTION"    -> Icons.Default.Desk
        "HOUSEKEEPING" -> Icons.Default.CleaningServices
        else           -> Icons.Default.ManageAccounts
    }
    val label = when (role) {
        "ADMIN"        -> "Admin Control Center"
        "KITCHEN"      -> "Kitchen Dashboard"
        "RECEPTION"    -> "Front Desk Portal"
        "HOUSEKEEPING" -> "Housekeeping Portal"
        else           -> "Staff Portal"
    }
    val subtitle = when (role) {
        "ADMIN"        -> "Manage rooms, orders, users & content"
        "KITCHEN"      -> "View and manage food orders in real time"
        "RECEPTION"    -> "Manage check-ins, bookings & guest requests"
        "HOUSEKEEPING" -> "View room assignments and cleaning tasks"
        else           -> "View orders and manage guest requests"
    }

    Surface(
        onClick = if (isAdmin) onNavigateToAdmin else onNavigateToStaff,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFF0C1A28),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(14.dp),
                color = accentColor.copy(alpha = 0.15f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = accentColor, modifier = Modifier.size(26.dp))
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    label,
                    style = MaterialTheme.typography.titleSmall,
                    color = OnSurfaceDark,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = OnSurfaceDark.copy(alpha = 0.5f)
                )
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = accentColor.copy(alpha = 0.7f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// -----------------------------------------------------------------------------
// SettingsSectionCard � generic wrapper
// -----------------------------------------------------------------------------

@Composable
private fun SettingsSectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = GoldPrimary,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF0C1A28),
            border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.15f))
        ) {
            Column(modifier = Modifier.padding(4.dp)) {
                content()
            }
        }
    }
}

// -----------------------------------------------------------------------------
// NotificationsRow
// -----------------------------------------------------------------------------

@Composable
private fun NotificationsRow(
    enabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Surface(
            modifier = Modifier.size(40.dp),
            shape = RoundedCornerShape(12.dp),
            color = if (enabled) GoldPrimary.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.Notifications,
                    contentDescription = null,
                    tint = if (enabled) GoldPrimary else OnSurfaceDark.copy(alpha = 0.4f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "Push Notifications",
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurfaceDark,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                "Receive alerts for bookings, orders, and hotel updates",
                style = MaterialTheme.typography.labelSmall,
                color = OnSurfaceDark.copy(alpha = 0.5f)
            )
        }
        Switch(
            checked = enabled,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor       = Color(0xFF050D18),
                checkedTrackColor       = GoldPrimary,
                uncheckedThumbColor     = OnSurfaceDark.copy(alpha = 0.5f),
                uncheckedTrackColor     = Color.White.copy(alpha = 0.08f),
                uncheckedBorderColor    = Color.White.copy(alpha = 0.15f)
            )
        )
    }
}

// -----------------------------------------------------------------------------
// LanguageSection
// -----------------------------------------------------------------------------

@Composable
private fun LanguageSection(
    selected: String,
    onSelect: (String) -> Unit
) {
    data class LangOption(val code: String, val label: String, val flag: String)

    val languages = listOf(
        LangOption("en", "English",  "\uD83C\uDDEC\uD83C\uDDE7"),
        LangOption("am", "????",    "\uD83C\uDDEA\uD83C\uDDF9"),
        LangOption("fr", "Fran�ais", "\uD83C\uDDEB\uD83C\uDDF7")
    )

    Column {
        languages.forEachIndexed { index, lang ->
            if (index > 0) {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = Color.White.copy(alpha = 0.05f)
                )
            }
            val isSelected = selected == lang.code
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelect(lang.code) }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(lang.flag, fontSize = 22.sp)
                Text(
                    lang.label,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isSelected) GoldPrimary else OnSurfaceDark,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
                if (isSelected) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = GoldPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// AboutSection
// -----------------------------------------------------------------------------

@Composable
private fun AboutSection(appVersion: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        // Description
        Text(
            "Goha Hotel is a 4-star hotel perched on the hilltop of Gondar, offering panoramic views of the historic city. Built in the 1960s, it remains one of Ethiopia's most iconic hotels.",
            style = MaterialTheme.typography.bodySmall,
            color = OnSurfaceDark.copy(alpha = 0.6f),
            fontStyle = FontStyle.Italic,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            lineHeight = 18.sp
        )

        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 16.dp),
            color = Color.White.copy(alpha = 0.05f)
        )

        InfoRow(emoji = "\uD83C\uDFE8", label = "Hotel",      value = "Goha Hotel, Gondar, Ethiopia")
        InfoRow(emoji = "\uD83D\uDCDE", label = "Front Desk", value = "+251 58 111 0000 � Ext. 100")
        InfoRow(emoji = "\uD83D\uDCE7", label = "Email",      value = "gohahotel34@gmail.com")
        InfoRow(emoji = "\uD83D\uDCCD", label = "Location",   value = "Gondar, Amhara Region, Ethiopia")
        InfoRow(emoji = "\u2139\uFE0F",  label = "App Version", value = appVersion)
        InfoRow(emoji = "\uD83D\uDCBB", label = "Technology", value = "Kotlin � Jetpack Compose � Firebase")
    }
}

@Composable
private fun InfoRow(emoji: String, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(emoji, fontSize = 18.sp, modifier = Modifier.width(26.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = OnSurfaceDark.copy(alpha = 0.45f),
                fontWeight = FontWeight.SemiBold
            )
            Text(
                value,
                style = MaterialTheme.typography.bodySmall,
                color = OnSurfaceDark.copy(alpha = 0.85f)
            )
        }
    }
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp),
        color = Color.White.copy(alpha = 0.04f)
    )
}

// -----------------------------------------------------------------------------
// SupportSection
// -----------------------------------------------------------------------------

@Composable
private fun SupportSection() {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        InfoRow(emoji = "\uD83D\uDECE\uFE0F", label = "24/7 Concierge", value = "Chat with us anytime in the app")
        InfoRow(emoji = "\uD83D\uDEA8",        label = "Emergency",      value = "Dial 0 from any room phone")
        InfoRow(emoji = "\uD83C\uDF10",        label = "Website",        value = "www.gohahotel.com")
        InfoRow(emoji = "\uD83D\uDCF1",        label = "WhatsApp",       value = "+251 91 234 5678")
    }
}

// -----------------------------------------------------------------------------
// PrivacyLegalSection
// -----------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PrivacyLegalSection() {
    val uriHandler = LocalUriHandler.current

    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        // Privacy Policy � clickable
        Surface(
            onClick = {
                try { uriHandler.openUri("https://gohahotel.com/privacy") } catch (_: Exception) {}
            },
            color = Color.Transparent,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("\uD83D\uDD12", fontSize = 18.sp, modifier = Modifier.width(26.dp))
                Text(
                    "Privacy Policy",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceDark.copy(alpha = 0.85f)
                )
                Icon(
                    Icons.Default.OpenInNew,
                    contentDescription = null,
                    tint = GoldPrimary.copy(alpha = 0.6f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 16.dp),
            color = Color.White.copy(alpha = 0.05f)
        )

        // Terms of Service � clickable
        Surface(
            onClick = {
                try { uriHandler.openUri("https://gohahotel.com/terms") } catch (_: Exception) {}
            },
            color = Color.Transparent,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("\uD83D\uDCDC", fontSize = 18.sp, modifier = Modifier.width(26.dp))
                Text(
                    "Terms of Service",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceDark.copy(alpha = 0.85f)
                )
                Icon(
                    Icons.Default.OpenInNew,
                    contentDescription = null,
                    tint = GoldPrimary.copy(alpha = 0.6f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 16.dp),
            color = Color.White.copy(alpha = 0.05f)
        )

        // Data & Storage � info only
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("\uD83D\uDDC4\uFE0F", fontSize = 18.sp, modifier = Modifier.width(26.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Data & Storage",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceDark.copy(alpha = 0.85f)
                )
                Text(
                    "Your data is stored securely on Firebase",
                    style = MaterialTheme.typography.labelSmall,
                    color = OnSurfaceDark.copy(alpha = 0.45f)
                )
            }
        }
    }
}


// -----------------------------------------------------------------------------

@Composable
private fun LogoutConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0C1A28),
        shape = RoundedCornerShape(24.dp),
        icon = {
            Surface(
                modifier = Modifier.size(52.dp),
                shape = CircleShape,
                color = ErrorRed.copy(alpha = 0.15f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Logout,
                        contentDescription = null,
                        tint = ErrorRed,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        },
        title = {
            Text(
                "Sign Out",
                color = OnSurfaceDark,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Text(
                "Are you sure you want to sign out of your Goha Hotel account?",
                color = OnSurfaceDark.copy(alpha = 0.7f),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Sign Out", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancel", color = OnSurfaceDark.copy(alpha = 0.7f))
            }
        }
    )
}

// -----------------------------------------------------------------------------
// SettingsFooter
// -----------------------------------------------------------------------------

@Composable
private fun SettingsFooter() {
    val year = remember {
        SimpleDateFormat("yyyy", Locale.getDefault()).format(Date())
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        HorizontalDivider(color = GoldPrimary.copy(alpha = 0.1f))

        Spacer(Modifier.height(8.dp))

        // Five gold stars
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            repeat(5) {
                Icon(
                    Icons.Default.Star,
                    contentDescription = null,
                    tint = GoldPrimary.copy(alpha = 0.7f),
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        Text(
            text = "� $year Goha Hotel, Gondar, Ethiopia",
            style = MaterialTheme.typography.labelSmall,
            color = OnSurfaceDark.copy(alpha = 0.35f),
            textAlign = TextAlign.Center
        )

        Text(
            text = "High Above the Historic City of Gondar",
            style = MaterialTheme.typography.labelSmall,
            color = GoldLight.copy(alpha = 0.3f),
            fontStyle = FontStyle.Italic,
            textAlign = TextAlign.Center
        )

        Text(
            text = "All rights reserved � Powered by Kotlin & Firebase",
            style = MaterialTheme.typography.labelSmall,
            color = OnSurfaceDark.copy(alpha = 0.2f),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(4.dp))
    }
}
