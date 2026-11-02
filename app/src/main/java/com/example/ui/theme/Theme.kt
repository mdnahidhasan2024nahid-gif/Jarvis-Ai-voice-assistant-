package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val NovaDarkColorScheme = darkColorScheme(
    primary = NovaCyanGlow,
    onPrimary = NovaDarkNavy,
    primaryContainer = NovaDeepNavy,
    onPrimaryContainer = NovaSoftSky,
    secondary = NovaSecondaryBlue,
    onSecondary = Color.White,
    background = NovaDarkNavy,
    surface = NovaDeepNavy,
    onBackground = Color.White,
    onSurface = Color.White,
    surfaceVariant = Color(0xFF1E3A6E),
    onSurfaceVariant = NovaSoftSky,
    outline = Color(0xFF2E4E8E)
)

private val NovaLightColorScheme = lightColorScheme(
    primary = NovaElectricBlue,
    onPrimary = Color.White,
    primaryContainer = NovaSoftSky,
    onPrimaryContainer = NovaDeepNavy,
    secondary = NovaSecondaryBlue,
    onSecondary = Color.White,
    background = NovaBackgroundIce,
    surface = NovaSurfaceWhite,
    onBackground = NovaTextPrimary,
    onSurface = NovaTextPrimary,
    surfaceVariant = NovaSoftSky,
    onSurfaceVariant = NovaTextSecondary,
    outline = NovaBorderLight
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Preserve brand identity by default
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) NovaDarkColorScheme else NovaLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
