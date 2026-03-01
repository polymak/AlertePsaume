package com.bible.alertepsaume.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = GoldPrimary,
    onPrimary = White,
    secondary = InactiveGrey,
    onSecondary = DarkText,
    background = LightBackground,
    onBackground = DarkText,
    surface = White,
    onSurface = DarkText,
    surfaceVariant = GoldLight,
    onSurfaceVariant = GoldPrimary
)

@Composable
fun AlertePsaumeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Strictly following the provided palette, so we ignore darkTheme and dynamicColor for this specific design request
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
