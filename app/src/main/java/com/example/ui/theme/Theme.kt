package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

private val DarkColorScheme = darkColorScheme(
    primary = GoldAccent,
    onPrimary = EmeraldDarkBackground,
    primaryContainer = EmeraldSecondary,
    onPrimaryContainer = GoldLight,
    secondary = GoldAccent,
    onSecondary = EmeraldDarkBackground,
    background = EmeraldDarkBackground,
    onBackground = TextPrimaryLight,
    surface = EmeraldSurfaceDark,
    onSurface = TextPrimaryLight,
    surfaceVariant = EmeraldSecondary,
    onSurfaceVariant = TextSecondaryLight
)

private val LightColorScheme = lightColorScheme(
    primary = EmeraldPrimary,
    onPrimary = Color.White,
    primaryContainer = EmeraldSecondary,
    onPrimaryContainer = GoldLight,
    secondary = GoldAccent,
    onSecondary = EmeraldPrimary,
    background = IvoryBackground,
    onBackground = TextPrimaryDark,
    surface = IvorySurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = IvorySurfaceVariant,
    onSurfaceVariant = TextSecondaryDark
)

@Composable
fun PrayerSilentTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
