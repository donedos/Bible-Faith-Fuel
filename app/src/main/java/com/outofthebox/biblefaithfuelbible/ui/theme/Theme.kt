package com.outofthebox.biblefaithfuelbible.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

fun getIosColorScheme(accent: IosAccentColor, darkTheme: Boolean): ColorScheme {
    val primaryColor = Color(accent.primaryHex)
    val containerColor = Color(accent.containerHex)

    return if (darkTheme) {
        darkColorScheme(
            primary = primaryColor,
            onPrimary = Color.White,
            primaryContainer = Color(0xFF2C2C2E),
            onPrimaryContainer = Color.White,
            secondary = primaryColor,
            onSecondary = Color.White,
            secondaryContainer = Color(0xFF2C2C2E),
            onSecondaryContainer = Color.White,
            tertiary = primaryColor,
            tertiaryContainer = Color(0xFF2C2C2E),
            onTertiaryContainer = Color.White,
            background = IOSBackgroundDark,
            onBackground = Color.White,
            surface = IOSSurfaceDark,
            onSurface = Color.White,
            surfaceVariant = Color(0xFF2C2C2E),
            onSurfaceVariant = Color(0xFFE5E5EA)
        )
    } else {
        lightColorScheme(
            primary = primaryColor,
            onPrimary = Color.White,
            primaryContainer = containerColor,
            onPrimaryContainer = Color(0xFF1C1C1E),
            secondary = primaryColor,
            onSecondary = Color.White,
            secondaryContainer = containerColor,
            onSecondaryContainer = Color(0xFF1C1C1E),
            tertiary = primaryColor,
            tertiaryContainer = containerColor,
            onTertiaryContainer = Color(0xFF1C1C1E),
            background = IOSBackgroundLight,
            onBackground = Color(0xFF1C1C1E),
            surface = IOSSurfaceLight,
            onSurface = Color(0xFF1C1C1E),
            surfaceVariant = Color(0xFFE5E5EA),
            onSurfaceVariant = Color(0xFF1C1C1E)
        )
    }
}

@Composable
fun BibleFaithFuelBibleTheme(
    accentColor: IosAccentColor = IosAccentColor.BLUE,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = getIosColorScheme(accentColor, darkTheme)

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
