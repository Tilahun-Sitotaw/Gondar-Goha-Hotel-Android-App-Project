package com.gohahotel.connect.ui.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gohahotel.connect.ui.theme.GoldLight
import com.gohahotel.connect.ui.theme.GoldPrimary
import com.gohahotel.connect.ui.theme.OnSurfaceDark
import com.gohahotel.connect.ui.theme.SurfaceDark
import com.gohahotel.connect.ui.theme.TealDark
import com.gohahotel.connect.ui.theme.TealPrimary
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    viewModel: SplashViewModel = hiltViewModel(),
    onNavigateToLogin: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToAdmin: () -> Unit,
    onNavigateToStaff: () -> Unit
) {
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val userRole by viewModel.userRole.collectAsState()

    // ── 3D & Modern Animations ──────────────────────────────────────────────
    val infiniteTransition = rememberInfiniteTransition(label = "3d_rotation")
    
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val logoScale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(1200, easing = EaseOutBack),
        label = "logo_scale"
    )

    var startAnimation by remember { mutableStateOf(false) }
    val animatedAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(1500),
        label = "alpha"
    )

    val bounceTranslation by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce"
    )

    LaunchedEffect(Unit) {
        startAnimation = true
        delay(3000)
        
        // Always navigate to login first as per user request
        onNavigateToLogin()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(TealDark, SurfaceDark, Color.Black)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Subtle ambient light effect in background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.1f)
                .background(
                    Brush.radialGradient(
                        colors = listOf(GoldPrimary, Color.Transparent),
                        radius = 1000f
                    )
                )
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.offset(y = bounceTranslation.dp)
        ) {
            // 3D Animated Logo
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .scale(logoScale)
                    .alpha(animatedAlpha)
                    .graphicsLayer {
                        rotationY = rotation
                        cameraDistance = 12f * density
                    }
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(GoldLight, GoldPrimary, GoldPrimary.copy(alpha = 0.8f))
                        )
                    )
                    .border(2.dp, GoldLight.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "G",
                    color = SurfaceDark,
                    fontSize = 72.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.graphicsLayer {
                        // Counter-rotate text slightly for a "floating" feel inside the 3D disk
                        rotationY = -rotation / 2
                    }
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "GOHA HOTEL",
                style = MaterialTheme.typography.displaySmall,
                color = GoldPrimary,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 8.sp,
                modifier = Modifier.alpha(animatedAlpha)
            )
            
            Text(
                text = "GONDAR . ETHIOPIA",
                style = MaterialTheme.typography.labelLarge,
                color = GoldLight.copy(alpha = 0.7f),
                letterSpacing = 6.sp,
                modifier = Modifier.alpha(animatedAlpha).padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Glassmorphic tagline container
            Box(
                modifier = Modifier
                    .alpha(animatedAlpha)
                    .padding(horizontal = 32.dp)
                    .background(
                        Color.White.copy(alpha = 0.05f),
                        shape = MaterialTheme.shapes.medium
                    )
                    .border(1.dp, Color.White.copy(alpha = 0.1f), MaterialTheme.shapes.medium)
                    .padding(vertical = 12.dp, horizontal = 24.dp)
            ) {
                Text(
                    text = "Where History Meets the Sky",
                    style = MaterialTheme.typography.bodyLarge,
                    color = OnSurfaceDark.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Light
                )
            }
        }

        // Animated progress indicator at bottom
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 60.dp)
                .width(100.dp)
                .height(2.dp)
                .background(Color.White.copy(alpha = 0.1f))
        ) {
            val progressWidth by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 100f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1500, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "progress"
            )
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(progressWidth.dp)
                    .background(GoldPrimary)
            )
        }
    }
}
