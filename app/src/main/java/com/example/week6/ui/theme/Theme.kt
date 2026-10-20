package com.example.week6.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val SteamDarkColorScheme = darkColorScheme(
    primary = SteamBluePrimary,
    onPrimary = Color.Black,
    primaryContainer = SteamBlueContainer,
    onPrimaryContainer = SteamBluePrimary,
    secondary = SteamGreenAccent,
    onSecondary = Color.Black,
    secondaryContainer = SteamGreenContainer,
    onSecondaryContainer = SteamGreenAccent,
    tertiary = SteamGold,
    onTertiary = Color.Black,
    background = SteamDarkBg,
    onBackground = SteamTextWhite,
    surface = SteamDarkSurface,
    onSurface = SteamTextWhite,
    surfaceVariant = SteamSurfaceVariant,
    onSurfaceVariant = SteamTextMuted,
    outline = SteamCardBorder,
    error = SteamSaleRed
)

private val SteamLightColorScheme = lightColorScheme(
    primary = SteamBlueHover,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD6EFFF),
    secondary = SteamGreenDark,
    onSecondary = Color.White,
    background = Color(0xFFF1F5F9),
    surface = Color.White,
    surfaceVariant = Color(0xFFE2E8F0),
    onSurface = Color(0xFF1E293B),
    outline = Color(0xFFCBD5E1)
)

@Composable
fun Week6Theme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) SteamDarkColorScheme else SteamLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
