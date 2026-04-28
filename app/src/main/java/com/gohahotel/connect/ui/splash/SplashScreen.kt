package com.gohahotel.connect.ui.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gohahotel.connect.ui.theme.GoldLight
import com.gohahotel.connect.ui.theme.GoldPrimary
import com.gohahotel.connect.ui.theme.SurfaceDark
import com.gohahotel.connect.ui.theme.TealDark
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    viewModel: SplashViewModel = hiltViewModel(),
    onNavigateToLogin: () -> Unit,
    onNavigateToHome: () -> Unit
) {
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()

    // ── Animations ────────────────────────────────────────────────────────────
    val logoScale by animateFloatAsState(
        targetValue  = 1f,
        animationSpec = tween(800, easing = EaseOutBack),
        label        = "logo_scale"
    )
    var alpha by remember { mutableFloatStateOf(0f) }
    val animatedAlpha by animateFloatAsState(
        targetValue  = alpha,
        animationSpec = tween(1000),
        label        = "alpha"
    )

    LaunchedEffect(Unit) {
        alpha = 1f
        delay(2500)
        if (isLoggedIn == true) onNavigateToHome() else onNavigateToLogin()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(TealDark, SurfaceDark, SurfaceDark)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Hotel crest / logo placeholder
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .scale(logoScale)
                    .alpha(animatedAlpha)
                    .background(
                        Brush.radialGradient(listOf(GoldLight, GoldPrimary)),
                        shape = androidx.compose.foundation.shape.CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text  = "G",
                    color = SurfaceDark,
                    fontSize    = 64.sp,
                    fontWeight  = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text      = "GOHA HOTEL",
                style     = MaterialTheme.typography.headlineLarge,
                color     = GoldPrimary,
                fontWeight = FontWeight.Bold,
                modifier  = Modifier.alpha(animatedAlpha)
            )
            Text(
                text     = "GONDAR · ETHIOPIA",
                style    = MaterialTheme.typography.labelLarge,
                color    = GoldLight.copy(alpha = 0.7f),
                letterSpacing = 4.sp,
                modifier = Modifier.alpha(animatedAlpha)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text      = "Where History Meets the Sky",
                style     = MaterialTheme.typography.bodyMedium,
                color     = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                textAlign = TextAlign.Center,
                modifier  = Modifier.alpha(animatedAlpha)
            )
        }

        // Version tag
        Text(
            text     = "v1.0",
            style    = MaterialTheme.typography.labelSmall,
            color    = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
                .alpha(animatedAlpha)
        )
    }
}
