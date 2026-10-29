package com.outofthebox.biblefaithfuelbible.ui.theme

import androidx.compose.ui.graphics.Color

enum class IosAccentColor(val displayName: String, val primaryHex: Long, val containerHex: Long) {
    BLUE("iOS Blue", 0xFF007AFF, 0xFFE5F1FF),
    PURPLE("iOS Purple", 0xFF5856D6, 0xFFEFEFFD),
    INDIGO("iOS Indigo", 0xFF5E5CE6, 0xFFEFEFFE),
    TEAL("iOS Teal", 0xFF30B0C7, 0xFFEBF8FA),
    GREEN("iOS Green", 0xFF34C759, 0xFFEBF9EE),
    ORANGE("iOS Orange", 0xFFFF9500, 0xFFFFF4E5),
    PINK("iOS Pink", 0xFFFF2D55, 0xFFFFEAEE)
}

val IOSBackgroundLight = Color(0xFFF2F2F7)
val IOSSurfaceLight = Color(0xFFFFFFFF)

val IOSBackgroundDark = Color(0xFF121214) // Deep Dark Background
val IOSSurfaceDark = Color(0xFF1C1C1E)    // Dark Surface
