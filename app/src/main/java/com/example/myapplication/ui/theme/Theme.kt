package com.example.myapplication.ui.theme

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

private val LightColorScheme = lightColorScheme(
    primary             = PaniniRed,
    onPrimary           = PaniniWhite,
    primaryContainer    = PaniniRedContainer,
    onPrimaryContainer  = PaniniRedDark,

    secondary           = PaniniGold,
    onSecondary         = PaniniBlack,
    secondaryContainer  = PaniniGoldContainer,
    onSecondaryContainer = PaniniGoldDark,

    tertiary            = PaniniBlack,
    onTertiary          = PaniniWhite,
    tertiaryContainer   = Color(0xFFE0E0E0),
    onTertiaryContainer = PaniniBlack,

    error               = PaniniRed,
    onError             = PaniniWhite,
    errorContainer      = PaniniRedContainer,
    onErrorContainer    = PaniniRedDark,

    background          = PaniniBackground,
    onBackground        = PaniniOnSurface,
    surface             = PaniniSurface,
    onSurface           = PaniniOnSurface,
    onSurfaceVariant    = PaniniGrayText,
    surfaceVariant      = Color(0xFFF5F0DC),
    outline             = Color(0xFFD4C99A)
)

private val DarkColorScheme = darkColorScheme(
    primary             = PaniniGold,
    onPrimary           = PaniniBlack,
    primaryContainer    = PaniniGoldDark,
    onPrimaryContainer  = PaniniGoldLight,

    secondary           = PaniniRedLight,
    onSecondary         = PaniniBlack,
    secondaryContainer  = PaniniRedDark,
    onSecondaryContainer = PaniniRedLight,

    tertiary            = Color(0xFFE0E0E0),
    onTertiary          = PaniniBlack,
    tertiaryContainer   = Color(0xFF3D3D3D),
    onTertiaryContainer = Color(0xFFE0E0E0),

    error               = PaniniRedLight,
    onError             = PaniniBlack,

    background          = Color(0xFF1A1400),
    onBackground        = PaniniGoldLight,
    surface             = Color(0xFF2A2000),
    onSurface           = Color(0xFFFFF5CC),
    onSurfaceVariant    = Color(0xFFD4C99A),
    surfaceVariant      = Color(0xFF3A3000),
    outline             = Color(0xFF6B5C00)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = PaniniRed.toArgb()
            WindowCompat.getInsetsController(window, view)
                .isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = Typography,
        content     = content
    )
}