package com.gohahotel.connect.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gohahotel.connect.ui.theme.*

@Composable
fun GohaFooter(
    onAboutClick: () -> Unit = {},
    onContactClick: () -> Unit = {},
    onPrivacyClick: () -> Unit = {},
    onTermsClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0A1424))
            .padding(vertical = 24.dp, horizontal = 16.dp)
    ) {
        // Logo and tagline
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .size(48.dp)
                .background(Brush.linearGradient(listOf(GoldLight, GoldPrimary)), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("G", color = Color(0xFF050D18), fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
        }

        Spacer(Modifier.height(8.dp))

        Text(
            "GOHA HOTEL",
            modifier = Modifier.align(Alignment.CenterHorizontally),
            style = MaterialTheme.typography.titleMedium,
            color = GoldPrimary,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 2.sp
        )

        Text(
            "GONDAR · ETHIOPIA",
            modifier = Modifier.align(Alignment.CenterHorizontally),
            style = MaterialTheme.typography.labelSmall,
            color = GoldLight.copy(0.5f),
            letterSpacing = 1.sp
        )

        Spacer(Modifier.height(16.dp))

        // Quick links
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FooterLink("About", onAboutClick)
            FooterLink("Contact", onContactClick)
            FooterLink("Privacy", onPrivacyClick)
            FooterLink("Terms", onTermsClick)
        }

        Spacer(Modifier.height(16.dp))

        // Contact info
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Phone, null, tint = GoldPrimary, modifier = Modifier.size(16.dp))
            Text("+251 911 234 567", style = MaterialTheme.typography.labelSmall, color = OnSurfaceDark.copy(0.7f))
        }

        Spacer(Modifier.height(6.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Email, null, tint = GoldPrimary, modifier = Modifier.size(16.dp))
            Text("info@gohahotel.com", style = MaterialTheme.typography.labelSmall, color = OnSurfaceDark.copy(0.7f))
        }

        Spacer(Modifier.height(16.dp))

        // Copyright
        Text(
            "© 2026 Goha Hotel. All rights reserved.\nHigh Above the Historic City of Gondar",
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally),
            style = MaterialTheme.typography.labelSmall,
            color = OnSurfaceDark.copy(0.3f),
            textAlign = TextAlign.Center,
            lineHeight = 16.sp
        )
    }
}

@Composable
private fun FooterLink(
    text: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = GoldPrimary.copy(0.1f),
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text,
            style = MaterialTheme.typography.labelSmall,
            color = GoldPrimary,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
