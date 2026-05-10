package com.gohahotel.connect.ui.auth

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gohahotel.connect.ui.theme.*

@Composable
fun EmailVerificationScreen(
    email: String,
    uiState: AuthUiState,
    onResend: () -> Unit,
    onVerified: () -> Unit,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF071520), Color(0xFF050D18), Color.Black)))
    ) {
        // Ambient glow
        Box(
            modifier = Modifier.fillMaxSize().background(
                Brush.radialGradient(listOf(GoldPrimary.copy(0.06f), Color.Transparent), radius = 1000f)
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(80.dp))

            // ── Icon ──────────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .background(GoldPrimary.copy(0.1f), RoundedCornerShape(28.dp))
                    .border(1.dp, GoldPrimary.copy(0.3f), RoundedCornerShape(28.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.MarkEmailRead,
                    contentDescription = null,
                    tint = GoldPrimary,
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(Modifier.height(24.dp))

            // ── Title ─────────────────────────────────────────────────────────
            Text(
                "Verify Your Email",
                style = MaterialTheme.typography.headlineMedium,
                color = GoldPrimary,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(12.dp))

            // ── Email display ─────────────────────────────────────────────────
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = GoldPrimary.copy(0.08f),
                border = BorderStroke(1.dp, GoldPrimary.copy(0.25f))
            ) {
                Text(
                    email,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = GoldLight,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(Modifier.height(28.dp))

            // ── Instructions card ─────────────────────────────────────────────
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF0E1B2A),
                border = BorderStroke(1.dp, Color.White.copy(0.06f))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "We've sent a verification link to your email. Please follow these steps:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceDark.copy(0.8f),
                        lineHeight = 20.sp
                    )

                    // Step 1
                    InstructionStep(
                        number = "1",
                        icon = Icons.Default.Email,
                        text = "Open your email inbox"
                    )

                    // Step 2
                    InstructionStep(
                        number = "2",
                        icon = Icons.Default.Search,
                        text = "Find the email from Goha Hotel (check spam if needed)"
                    )

                    // Step 3
                    InstructionStep(
                        number = "3",
                        icon = Icons.Default.Link,
                        text = "Click the verification link in the email"
                    )

                    // Step 4
                    InstructionStep(
                        number = "4",
                        icon = Icons.Default.ArrowBack,
                        text = "Come back here and tap \"I've Verified My Email\""
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── Error banner ──────────────────────────────────────────────────
            AnimatedVisibility(
                visible = uiState.error != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = ErrorRed.copy(0.1f),
                    border = BorderStroke(1.dp, ErrorRed.copy(0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            Icons.Default.ErrorOutline,
                            null,
                            tint = ErrorRed,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            uiState.error ?: "",
                            color = ErrorRed,
                            style = MaterialTheme.typography.bodySmall,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            // ── Success banner ────────────────────────────────────────────────
            AnimatedVisibility(
                visible = uiState.message != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SuccessGreen.copy(0.1f),
                    border = BorderStroke(1.dp, SuccessGreen.copy(0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            null,
                            tint = SuccessGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            uiState.message ?: "",
                            color = SuccessGreen,
                            style = MaterialTheme.typography.bodySmall,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            if (uiState.error != null || uiState.message != null) {
                Spacer(Modifier.height(16.dp))
            }

            // ── "I've Verified" button ────────────────────────────────────────
            Button(
                onClick = onVerified,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                enabled = !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        color = Color(0xFF050D18),
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.5.dp
                    )
                } else {
                    Icon(
                        Icons.Default.CheckCircle,
                        null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "I've Verified My Email",
                        color = Color(0xFF050D18),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // ── Resend button ─────────────────────────────────────────────────
            OutlinedButton(
                onClick = onResend,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, GoldPrimary.copy(0.35f)),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = GoldPrimary.copy(0.05f)),
                enabled = !uiState.isLoading
            ) {
                Icon(
                    Icons.Default.Refresh,
                    null,
                    tint = GoldPrimary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Resend Verification Email",
                    color = GoldPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Spacer(Modifier.height(12.dp))

            // ── Back button ───────────────────────────────────────────────────
            TextButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Icon(
                    Icons.Default.ArrowBack,
                    null,
                    tint = OnSurfaceDark.copy(0.6f),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    "Back to Registration",
                    color = OnSurfaceDark.copy(0.6f),
                    style = MaterialTheme.typography.labelMedium
                )
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun InstructionStep(
    number: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Step number badge
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(GoldPrimary.copy(0.15f), RoundedCornerShape(8.dp))
                .border(1.dp, GoldPrimary.copy(0.3f), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                number,
                color = GoldPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }

        // Icon
        Icon(
            icon,
            contentDescription = null,
            tint = GoldPrimary.copy(0.7f),
            modifier = Modifier.size(20.dp).padding(top = 6.dp)
        )

        // Text
        Text(
            text,
            modifier = Modifier.weight(1f).padding(top = 4.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = OnSurfaceDark.copy(0.9f),
            lineHeight = 20.sp
        )
    }
}
