package com.gohahotel.connect.ui.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToHome: (() -> Unit)? = null,
    onNavigateToAdmin: (() -> Unit)? = null,
    onNavigateToStaff: (() -> Unit)? = null
) {
    var isVisible by remember { mutableStateOf(true) }
    
    // Logo scale animation
    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.8f,
        animationSpec = tween(durationMillis = 1500, easing = EaseInOutCubic),
        label = "logoScale"
    )
    
    // Logo alpha animation
    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 1500, easing = EaseInOutCubic),
        label = "logoAlpha"
    )
    
    // Text alpha animation (delayed)
    val textAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 1000, delayMillis = 500, easing = EaseInOutCubic),
        label = "textAlpha"
    )

    // Navigate after animation completes
    LaunchedEffect(Unit) {
        delay(3000) // 3 second splash screen
        isVisible = false
        delay(500) // Wait for fade out
        onNavigateToLogin()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF050D18),
                        Color(0xFF0A1424)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo/Icon
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .scale(scale)
                    .alpha(alpha),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "🏨",
                    fontSize = 80.sp,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Hotel Name
            Text(
                "GOHA HOTEL",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFFD4AF37), // Gold color
                modifier = Modifier.alpha(textAlpha)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Tagline
            Text(
                "Premium Hotel Management",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(0.7f),
                modifier = Modifier.alpha(textAlpha)
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Subtitle
            Text(
                "& Guest Experience Platform",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(0.7f),
                modifier = Modifier.alpha(textAlpha)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Loading indicator
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .alpha(textAlpha),
                contentAlignment = Alignment.Center
            ) {
                CircularLoadingIndicator()
            }
        }
    }
}

@Composable
private fun CircularLoadingIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "loading")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Box(
        modifier = Modifier
            .size(40.dp)
            .background(
                brush = Brush.sweepGradient(
                    listOf(
                        Color(0xFFD4AF37).copy(alpha = 0f),
                        Color(0xFFD4AF37),
                        Color(0xFFD4AF37).copy(alpha = 0f)
                    )
                ),
                shape = androidx.compose.foundation.shape.CircleShape
            )
    )
}
