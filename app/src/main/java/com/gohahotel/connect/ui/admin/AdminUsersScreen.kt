package com.gohahotel.connect.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
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
                title = { Text("Guest Directory") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            Text(
                "Registered App Users",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(16.dp)
            )

            if (users.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GoldPrimary)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(users) { user ->
                        UserAdminCard(
                            user = user,
                            onRoleToggle = {
                                val uid = user["uid"] as? String ?: ""
                                val currentRole = user["role"] as? String ?: "GUEST"
                                val newRole = if (currentRole == "ADMIN") "GUEST" else "ADMIN"
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
fun UserAdminCard(user: Map<String, Any>, onRoleToggle: () -> Unit) {
    val name = user["name"] as? String ?: "Anonymous"
    val email = user["email"] as? String ?: "No Email"
    val role = user["role"] as? String ?: "GUEST"

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardDark)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = MaterialTheme.shapes.small,
                color = GoldPrimary.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Person, null, tint = GoldPrimary)
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(name, fontWeight = FontWeight.Bold, color = OnSurfaceDark)
                Text(email, style = MaterialTheme.typography.bodySmall, color = OnSurfaceDark.copy(alpha = 0.6f))
                Text("Role: $role", style = MaterialTheme.typography.labelSmall, color = GoldLight)
            }
            
            IconButton(onClick = onRoleToggle) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = "Toggle Admin Role",
                    tint = if (role == "ADMIN") GoldPrimary else OnSurfaceDark.copy(alpha = 0.3f)
                )
            }
        }
    }
}
