package com.secyra.ai.core.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Dark Color Scheme - SECYRA AI uses dark theme only
private val DarkColorScheme = darkColorScheme(
    primary = TiffanyBlue,
    secondary = AzureBlue,
    tertiary = AzureBlue,
    background = SecyraBackground,
    surface = SecyraSurface,
    surfaceVariant = SecyraSurfaceElevated,
    onPrimary = SecyraWhite,
    onSecondary = SecyraWhite,
    onTertiary = SecyraWhite,
    onBackground = SecyraWhite,
    onSurface = SecyraWhite,
    onSurfaceVariant = SecyraTextSecondary,
    outline = SecyraBorder,
    outlineVariant = SecyraBorderLight
)

// Light Color Scheme (fallback, though app is designed for dark theme)
private val LightColorScheme = lightColorScheme(
    primary = TiffanyBlue,
    secondary = AzureBlue,
    tertiary = AzureBlue,
    background = SecyraBackground,
    surface = SecyraSurface,
    surfaceVariant = SecyraSurfaceElevated,
    onPrimary = SecyraWhite,
    onSecondary = SecyraWhite,
    onTertiary = SecyraWhite,
    onBackground = SecyraWhite,
    onSurface = SecyraWhite,
    onSurfaceVariant = SecyraTextSecondary,
    outline = SecyraBorder,
    outlineVariant = SecyraBorderLight
)

@Composable
fun SecyraTheme(
    darkTheme: Boolean = true, // SECYRA AI is designed for dark theme
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            window.decorView.setBackgroundColor(SecyraBackground.toArgb())
            WindowCompat.setDecorFitsSystemWindows(window, false)
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
            
            // Force the navigation bar to be transparent with light navigation area
            window.isNavigationBarContrastEnforced = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
