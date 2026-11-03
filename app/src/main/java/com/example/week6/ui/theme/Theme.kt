package com.example.week6.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val SteamDarkColorScheme = darkColorScheme(
    primary = SteamBluePrimary,
    onPrimary = Color.Black,
    primaryContainer = SteamBlueContainer,
    onPrimaryContainer = Color.White,
    secondary = SteamGreenAccent,
    onSecondary = Color.White,
    secondaryContainer = SteamGreenContainer,
    onSecondaryContainer = Color.White,
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
    primaryContainer = Color(0xFFF3E5F5),
    secondary = SteamGreenDark,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFE3E8),
    background = Color(0xFFFAF5FF),
    surface = Color.White,
    surfaceVariant = Color(0xFFF1E6F7),
    onSurface = Color(0xFF23182C),
    outline = Color(0xFFD8B4E2),
    error = SteamSaleRed
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
