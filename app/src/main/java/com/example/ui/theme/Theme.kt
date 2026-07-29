package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = GooglePastelBlue, // Soft Pastel Blue
    secondary = GooglePastelGreen, // Soft Pastel Green
    tertiary = GooglePastelYellow, // Soft Pastel Yellow
    error = GooglePastelRed, // Soft Pastel Red
    background = BackgroundDark, // Dark Charcoal / Off-Black (#121212)
    surface = SurfaceDark, // Dark Gray / Slate Gray (#1E1E1E)
    surfaceVariant = SurfaceVariantDark, // (#2C2C2C)
    onPrimary = Color(0xFF0F172A),
    onSecondary = Color(0xFF0F172A),
    onBackground = TextPrimaryDark, // Off-White / Soft White (#F8FAFC)
    onSurface = TextPrimaryDark, // Off-White / Soft White (#F8FAFC)
    onSurfaceVariant = TextSecondaryDark, // Light Gray / Medium Gray (#94A3B8)
    outline = Color(0xFF333333)
)

private val LightColorScheme = lightColorScheme(
    primary = MpscNavy,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE2E8F0),
    secondary = MpscEmerald,
    onSecondary = Color.White,
    secondaryContainer = MpscEmeraldLight,
    tertiary = MpscOrange,
    background = BackgroundLight,
    surface = SurfaceLight,
    onBackground = TextPrimaryLight,
    onSurface = TextPrimaryLight
)

@Composable
fun MPSCPrepTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
