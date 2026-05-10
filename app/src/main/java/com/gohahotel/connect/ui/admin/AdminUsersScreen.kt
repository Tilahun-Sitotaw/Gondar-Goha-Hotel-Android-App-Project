package com.gohahotel.connect.ui.admin

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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.gohahotel.connect.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUsersScreen(
    onBack: () -> Unit,
    viewModel: AdminViewModel = hiltViewModel()
) {
    val users by viewModel.users.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var filterRole  by remember { mutableStateOf("ALL") }

    val filteredUsers = remember(users, searchQuery, filterRole) {
        users.filter { user ->
            val name  = ((user["displayName"] as? String) ?: (user["email"] as? String) ?: "").lowercase()
            val email = (user["email"] as? String ?: "").lowercase()
            val role  = user["role"] as? String ?: "GUEST"
            val matchSearch = searchQuery.isBlank() ||
                name.contains(searchQuery.lowercase()) ||
                email.contains(searchQuery.lowercase())
            val matchRole = filterRole == "ALL" || role == filterRole
            matchSearch && matchRole
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("User Management", fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp, color = GoldPrimary)
                        Text("${users.size} registered users",
                            style = MaterialTheme.typography.labelSmall,
                            color = OnSurfaceDark.copy(0.5f))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = GoldPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.fetchUsers() }) {
                        Icon(Icons.Default.Refresh, null, tint = GoldPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceDark.copy(0.97f)
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color(0xFF071520), Color(0xFF050D18))))
                .padding(padding)
        ) {
            Column {
                // ── Search bar ────────────────────────────────────────────────
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by name or email…", color = OnSurfaceDark.copy(0.35f)) },
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = GoldPrimary.copy(0.6f), modifier = Modifier.size(18.dp)) },
                    trailingIcon = if (searchQuery.isNotBlank()) {
                        { IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, null, tint = OnSurfaceDark.copy(0.4f), modifier = Modifier.size(16.dp))
                        }}
                    } else null,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor      = GoldPrimary.copy(0.5f),
                        unfocusedBorderColor    = Color.White.copy(0.1f),
                        focusedTextColor        = OnSurfaceDark,
                        unfocusedTextColor      = OnSurfaceDark,
                        cursorColor             = GoldPrimary,
                        focusedContainerColor   = Color.White.copy(0.04f),
                        unfocusedContainerColor = Color.White.copy(0.03f)
                    )
                )

                // ── Role filter chips ─────────────────────────────────────────
                val roles = listOf("ALL", "ADMIN", "GUEST", "STAFF", "KITCHEN", "RECEPTION", "HOUSEKEEPING")
                androidx.compose.foundation.lazy.LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    items(roles) { r ->
                        val selected = filterRole == r
                        FilterChip(
                            selected = selected,
                            onClick  = { filterRole = r },
                            label    = { Text(r, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal) },
                            colors   = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GoldPrimary.copy(0.2f),
                                selectedLabelColor     = GoldPrimary,
                                containerColor         = Color.White.copy(0.04f),
                                labelColor             = OnSurfaceDark.copy(0.6f)
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true, selected = selected,
                                selectedBorderColor = GoldPrimary.copy(0.4f),
                                borderColor = Color.White.copy(0.1f)
                            )
                        )
                    }
                }

                // ── User list ─────────────────────────────────────────────────
                if (users.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = GoldPrimary)
                    }
                } else if (filteredUsers.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("👤", fontSize = 48.sp)
                            Spacer(Modifier.height(8.dp))
                            Text("No users match your search",
                                color = OnSurfaceDark.copy(0.4f),
                                style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredUsers, key = { it["uid"] as? String ?: it.hashCode().toString() }) { user ->
                            UserCard(
                                user = user,
                                onRoleChange = { newRole ->
                                    val uid = user["uid"] as? String ?: return@UserCard
                                    viewModel.updateUserRole(uid, newRole)
                                },
                                onDelete = {
                                    val uid = user["uid"] as? String ?: return@UserCard
                                    viewModel.deleteUser(uid)
                                }
                            )
                        }
                        item { Spacer(Modifier.height(16.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
fun UserCard(
    user: Map<String, Any>,
    onRoleChange: (String) -> Unit,
    onDelete: () -> Unit
) {
    val name = (user["displayName"] as? String)?.takeIf { it.isNotBlank() }
        ?: (user["email"] as? String)?.substringBefore("@")?.replaceFirstChar { it.uppercase() }
        ?: "Guest"
    val email       = user["email"] as? String ?: ""
    val role        = user["role"] as? String ?: "GUEST"
    val phone       = user["phoneNumber"] as? String ?: ""
    val address     = user["address"] as? String ?: ""
    val uid         = user["uid"] as? String ?: ""
    val profileImg  = user["profileImage"] as? String ?: ""
    val isAnonymous = email.isBlank()
    val isAdmin     = role == "ADMIN"

    var roleMenuExpanded    by remember { mutableStateOf(false) }
    var showDeleteConfirm   by remember { mutableStateOf(false) }
    var expanded            by remember { mutableStateOf(false) }

    val roleColor = when (role) {
        "ADMIN"        -> GoldPrimary
        "KITCHEN"      -> Color(0xFFFF8C00)
        "RECEPTION"    -> InfoBlue
        "HOUSEKEEPING" -> Color(0xFF9B59B6)
        "STAFF"        -> TealLight
        else           -> SuccessGreen.copy(0.8f)
    }

    // ── Delete confirmation dialog ────────────────────────────────────────────
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            containerColor   = Color(0xFF0A1424),
            shape            = RoundedCornerShape(20.dp),
            icon = { Icon(Icons.Default.DeleteForever, null, tint = ErrorRed, modifier = Modifier.size(32.dp)) },
            title = { Text("Remove User?", color = ErrorRed, fontWeight = FontWeight.Bold) },
            text  = {
                Text("This will remove $name ($email) from the system. This action cannot be undone.",
                    color = OnSurfaceDark.copy(0.7f), style = MaterialTheme.typography.bodyMedium)
            },
            confirmButton = {
                Button(
                    onClick = { showDeleteConfirm = false; onDelete() },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                    shape  = RoundedCornerShape(10.dp)
                ) { Text("Remove", color = Color.White, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel", color = OnSurfaceDark.copy(0.6f))
                }
            }
        )
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFF0C1A28),
        border = BorderStroke(1.dp, roleColor.copy(0.2f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // ── Header row ────────────────────────────────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Avatar
                Box(modifier = Modifier.size(52.dp)) {
                    if (profileImg.isNotEmpty()) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(profileImg).crossfade(true).build(),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(roleColor.copy(0.15f), CircleShape)
                                .border(1.dp, roleColor.copy(0.3f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                name.firstOrNull()?.uppercase() ?: "?",
                                color = roleColor,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 20.sp
                            )
                        }
                    }
                    // Role dot
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .align(Alignment.BottomEnd)
                            .background(roleColor, CircleShape)
                            .border(2.dp, Color(0xFF0C1A28), CircleShape)
                    )
                }

                // Name + email + role badge
                Column(modifier = Modifier.weight(1f)) {
                    Text(name, fontWeight = FontWeight.ExtraBold,
                        color = OnSurfaceDark, fontSize = 16.sp, maxLines = 1,
                        overflow = TextOverflow.Ellipsis)
                    if (email.isNotBlank()) {
                        Text(email, style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceDark.copy(0.5f), maxLines = 1,
                            overflow = TextOverflow.Ellipsis)
                    }
                    Spacer(Modifier.height(5.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = roleColor.copy(0.15f),
                            border = BorderStroke(1.dp, roleColor.copy(0.3f))
                        ) {
                            Text(role,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = roleColor, fontWeight = FontWeight.Bold)
                        }
                        if (isAnonymous) {
                            Surface(shape = RoundedCornerShape(6.dp), color = Color.White.copy(0.05f)) {
                                Text("Anonymous",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = OnSurfaceDark.copy(0.35f))
                            }
                        }
                    }
                }

                // Action buttons
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Expand/collapse
                    IconButton(
                        onClick = { expanded = !expanded },
                        modifier = Modifier.size(32.dp).background(Color.White.copy(0.04f), CircleShape)
                    ) {
                        Icon(
                            if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            null, tint = OnSurfaceDark.copy(0.5f), modifier = Modifier.size(16.dp)
                        )
                    }
                    // Role change (non-admin only)
                    if (!isAdmin) {
                        Box {
                            IconButton(
                                onClick = { roleMenuExpanded = true },
                                modifier = Modifier.size(32.dp).background(GoldPrimary.copy(0.08f), CircleShape)
                            ) {
                                Icon(Icons.Default.ManageAccounts, null,
                                    tint = GoldPrimary, modifier = Modifier.size(16.dp))
                            }
                            DropdownMenu(
                                expanded = roleMenuExpanded,
                                onDismissRequest = { roleMenuExpanded = false },
                                modifier = Modifier
                                    .background(Color(0xFF0E1B2A))
                                    .border(1.dp, GoldPrimary.copy(0.2f), RoundedCornerShape(8.dp))
                            ) {
                                listOf("GUEST", "RECEPTION", "KITCHEN", "HOUSEKEEPING", "STAFF").forEach { r ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                if (r == role) {
                                                    Icon(Icons.Default.Check, null,
                                                        tint = GoldPrimary, modifier = Modifier.size(14.dp))
                                                } else {
                                                    Spacer(Modifier.size(14.dp))
                                                }
                                                Text(r, color = if (r == role) GoldPrimary else OnSurfaceDark)
                                            }
                                        },
                                        onClick = { roleMenuExpanded = false; onRoleChange(r) }
                                    )
                                }
                            }
                        }
                    } else {
                        // Admin lock icon
                        Box(
                            modifier = Modifier.size(32.dp).background(GoldPrimary.copy(0.08f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.AdminPanelSettings, null,
                                tint = GoldPrimary, modifier = Modifier.size(16.dp))
                        }
                    }
                    // Delete (non-admin only)
                    if (!isAdmin && uid.isNotBlank()) {
                        IconButton(
                            onClick = { showDeleteConfirm = true },
                            modifier = Modifier.size(32.dp).background(ErrorRed.copy(0.08f), CircleShape)
                        ) {
                            Icon(Icons.Default.PersonRemove, null,
                                tint = ErrorRed.copy(0.8f), modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // ── Expanded details ──────────────────────────────────────────────
            if (expanded) {
                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = Color.White.copy(0.06f))
                Spacer(Modifier.height(12.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (uid.isNotBlank()) {
                        DetailRow(Icons.Default.Fingerprint, "User ID", uid.take(20) + "…")
                    }
                    if (phone.isNotBlank()) {
                        DetailRow(Icons.Default.Phone, "Phone", phone)
                    }
                    if (address.isNotBlank()) {
                        DetailRow(Icons.Default.LocationOn, "Address", address)
                    }
                    if (phone.isBlank() && address.isBlank() && uid.isBlank()) {
                        Text("No additional information available",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceDark.copy(0.3f))
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(icon, null, tint = GoldPrimary.copy(0.5f), modifier = Modifier.size(14.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = OnSurfaceDark.copy(0.4f))
            Text(value, style = MaterialTheme.typography.bodySmall, color = OnSurfaceDark.copy(0.85f))
        }
    }
}

// Keep old name for backward compat
@Composable
fun UserIdentityCard(user: Map<String, Any>, onRoleChange: (String) -> Unit) =
    UserCard(user = user, onRoleChange = onRoleChange, onDelete = {})
