package com.zombiethumb.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = MutedTeal,
    onPrimary = DeepNavy,
    primaryContainer = SlateBlue,
    onPrimaryContainer = BrightTeal,
    secondary = WarmAmber,
    onSecondary = DeepNavy,
    secondaryContainer = MidNavy,
    onSecondaryContainer = WarmAmber,
    tertiary = PaleGreen,
    onTertiary = DeepNavy,
    error = SoftCoral,
    onError = DeepNavy,
    background = DeepNavy,
    onBackground = OnSurfaceHigh,
    surface = SurfaceDark,
    onSurface = OnSurfaceHigh,
    surfaceVariant = SurfaceCard,
    onSurfaceVariant = OnSurfaceMed,
    outline = OnSurfaceLow,
)

@Composable
fun ZombieThumbTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = DeepNavy.toArgb()
            window.navigationBarColor = DeepNavy.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = ZombieTypography,
        content = content
    )
}
