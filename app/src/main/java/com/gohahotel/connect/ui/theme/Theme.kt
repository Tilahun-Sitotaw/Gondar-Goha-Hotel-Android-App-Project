package com.gohahotel.connect.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary          = GoldPrimary,
    onPrimary        = SurfaceDark,
    primaryContainer = GoldDark,
    onPrimaryContainer = GoldLight,

    secondary        = TealLight,
    onSecondary      = SurfaceDark,
    secondaryContainer = TealDark,
    onSecondaryContainer = TealLight,

    tertiary         = TerracottaLight,
    onTertiary       = SurfaceDark,
    tertiaryContainer = TerracottaDark,
    onTertiaryContainer = TerracottaLight,

    background       = SurfaceDark,
    onBackground     = OnSurfaceDark,

    surface          = SurfaceDark,
    onSurface        = OnSurfaceDark,
    surfaceVariant   = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceDark,

    outline          = OutlineDark,
    error            = ErrorRed,
)

private val LightColorScheme = lightColorScheme(
    primary          = GoldDark,
    onPrimary        = CardLight,
    primaryContainer = GoldLight,
    onPrimaryContainer = GoldDark,

    secondary        = TealPrimary,
    onSecondary      = CardLight,
    secondaryContainer = TealLight,
    onSecondaryContainer = TealDark,

    tertiary         = TerracottaPrimary,
    onTertiary       = CardLight,
    tertiaryContainer = TerracottaLight,
    onTertiaryContainer = TerracottaDark,

    background       = SurfaceLight,
    onBackground     = OnSurfaceLight,

    surface          = SurfaceLight,
    onSurface        = OnSurfaceLight,
    surfaceVariant   = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceLight,

    outline          = OutlineLight,
    error            = ErrorRed,
)

@Composable
fun GohaHotelTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = GohaTypography,
        content     = content
    )
}
