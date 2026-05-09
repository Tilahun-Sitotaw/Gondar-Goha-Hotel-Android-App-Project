package com.gohahotel.connect.ui.admin

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.platform.LocalContext
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Guest Directory", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text("Identity & Access Management", style = MaterialTheme.typography.labelSmall, color = GoldPrimary.copy(alpha = 0.6f))
                    }
                },
                navigationIcon = { 
                    IconButton(onClick = onBack) { 
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = GoldPrimary) 
                    } 
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceDark.copy(alpha = 0.95f),
                    titleContentColor = GoldPrimary
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(SurfaceDark, Color(0xFF0A1424), Color.Black)))
                .padding(padding)
        ) {
            if (users.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GoldPrimary)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(1), // Using 1 for detailed cards, can use 2 for compact
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(users) { user ->
                        UserIdentityCard(
                            user = user,
                            onRoleChange = { newRole ->
                                val uid = user["uid"] as? String ?: ""
                                if (uid.isNotEmpty()) viewModel.updateUserRole(uid, newRole)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun UserIdentityCard(user: Map<String, Any>, onRoleChange: (String) -> Unit) {
    val name = (user["displayName"] as? String)?.takeIf { it.isNotBlank() }
        ?: (user["name"] as? String)?.takeIf { it.isNotBlank() }
        ?: (user["email"] as? String)?.substringBefore("@")?.replaceFirstChar { it.uppercase() }
        ?: "Guest"
    val email = user["email"] as? String ?: "No email"
    val role = user["role"] as? String ?: "GUEST"
    val profileImage = user["profileImage"] as? String ?: ""
    val phone = user["phoneNumber"] as? String ?: ""
    val address = user["address"] as? String ?: ""
    val uid = user["uid"] as? String ?: ""
    val isAnonymous = uid.isBlank() || email == "No email"
    
    var expanded by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = CardDark.copy(alpha = 0.7f),
        border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Profile Image with Placeholder logic
                Box(modifier = Modifier.size(64.dp)) {
                    if (profileImage.isNotEmpty()) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(profileImage)
                                .crossfade(true)
                                .build(),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            shape = CircleShape,
                            color = GoldPrimary.copy(alpha = 0.1f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Person, null, tint = GoldPrimary, modifier = Modifier.size(32.dp))
                            }
                        }
                    }
                    
                    // Status Badge
                    Surface(
                        modifier = Modifier.size(16.dp).align(Alignment.BottomEnd),
                        shape = CircleShape,
                        color = if (role == "ADMIN") GoldPrimary else SuccessGreen,
                        border = BorderStroke(2.dp, CardDark)
                    ) {}
                }

                Spacer(Modifier.width(20.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(name, fontWeight = FontWeight.ExtraBold, color = OnSurfaceDark, fontSize = 18.sp)
                    Text(email, style = MaterialTheme.typography.bodySmall, color = OnSurfaceDark.copy(alpha = 0.5f))
                    
                    Spacer(Modifier.height(8.dp))
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Surface(
                            color = when (role) {
                                "ADMIN" -> GoldPrimary.copy(alpha = 0.15f)
                                "GUEST" -> TealPrimary.copy(alpha = 0.15f)
                                else -> Color.White.copy(alpha = 0.05f)
                            },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                role,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = when (role) {
                                    "ADMIN" -> GoldPrimary
                                    "GUEST" -> TealPrimary
                                    else -> GoldLight
                                },
                                fontWeight = FontWeight.Bold
                            )
                        }
                        if (isAnonymous) {
                            Surface(
                                color = Color.White.copy(0.05f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Anonymous",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = OnSurfaceDark.copy(0.4f))
                            }
                        }
                    }
                }

                // Only show role management for non-admin users
                if (role != "ADMIN") {
                    IconButton(
                        onClick = { expanded = true },
                        modifier = Modifier.background(Color.White.copy(0.03f), CircleShape)
                    ) {
                        Icon(Icons.Default.Shield, "Manage Role", tint = GoldPrimary.copy(alpha = 0.8f))
                    }
                } else {
                    // Admin badge — no role change allowed
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(GoldPrimary.copy(0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.AdminPanelSettings, null,
                            tint = GoldPrimary, modifier = Modifier.size(20.dp))
                    }
                }
            }

            if (phone.isNotEmpty() || address.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                Spacer(Modifier.height(16.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    if (phone.isNotEmpty()) {
                        InfoRow(Icons.Default.Phone, phone)
                    }
                    if (address.isNotEmpty()) {
                        InfoRow(Icons.Default.LocationOn, address)
                    }
                }
            }
        }

        // Role Dropdown — only for non-admin users
        if (role != "ADMIN") {
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(CardDark).border(1.dp, GoldPrimary.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
            ) {
                listOf("GUEST", "RECEPTION", "KITCHEN", "HOUSEKEEPING", "STAFF").forEach { r ->
                    DropdownMenuItem(
                        text = { Text(r, color = if(r == role) GoldPrimary else OnSurfaceDark) },
                        onClick = { expanded = false; onRoleChange(r) }
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = GoldPrimary.copy(alpha = 0.4f), modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(8.dp))
        Text(text, style = MaterialTheme.typography.labelSmall, color = OnSurfaceDark.copy(alpha = 0.6f))
    }
}
