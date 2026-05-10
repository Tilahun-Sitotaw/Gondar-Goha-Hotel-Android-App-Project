package com.gohahotel.connect.ui.auth

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gohahotel.connect.ui.theme.*

@Composable
fun OtpVerificationScreen(
    email: String,
    uiState: AuthUiState,
    onResend: () -> Unit,
    onVerifyOtp: (String) -> Unit,
    onBack: () -> Unit
) {
    var otpCode by remember { mutableStateOf("") }

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
                "Enter Verification Code",
                style = MaterialTheme.typography.headlineMedium,
                color = GoldPrimary,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(12.dp))

            // ── Email display ─────────────────────────────────────────────────
            Text(
                "We've sent a 6-digit code to:",
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurfaceDark.copy(0.6f),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(8.dp))

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

            // ── OTP Input Field ───────────────────────────────────────────────
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF0E1B2A),
                border = BorderStroke(1.dp, Color.White.copy(0.06f))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "Check your email inbox and enter the 6-digit code below:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceDark.copy(0.8f),
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )

                    OutlinedTextField(
                        value = otpCode,
                        onValueChange = { if (it.length <= 6 && it.all { char -> char.isDigit() }) otpCode = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Verification Code") },
                        placeholder = { Text("000000") },
                        leadingIcon = {
                            Icon(Icons.Default.Pin, null, tint = GoldPrimary.copy(0.7f), modifier = Modifier.size(20.dp))
                        },
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = OnSurfaceDark,
                            unfocusedTextColor = OnSurfaceDark.copy(0.9f),
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = Color.White.copy(0.12f),
                            focusedLabelColor = GoldPrimary,
                            unfocusedLabelColor = OnSurfaceDark.copy(0.4f),
                            focusedLeadingIconColor = GoldPrimary,
                            unfocusedLeadingIconColor = GoldPrimary.copy(0.4f),
                            cursorColor = GoldPrimary,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        ),
                        textStyle = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 8.sp,
                            textAlign = TextAlign.Center
                        )
                    )

                    Text(
                        "💡 Tip: The code is valid for 10 minutes",
                        style = MaterialTheme.typography.labelSmall,
                        color = OnSurfaceDark.copy(0.4f),
                        textAlign = TextAlign.Center
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

            // ── "Verify Code" button ──────────────────────────────────────────
            Button(
                onClick = { onVerifyOtp(otpCode) },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                enabled = !uiState.isLoading && otpCode.length == 6
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
                        "Verify & Create Account",
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
                    "Resend Code",
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
