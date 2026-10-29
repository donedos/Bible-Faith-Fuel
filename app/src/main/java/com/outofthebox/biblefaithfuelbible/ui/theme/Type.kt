package com.outofthebox.biblefaithfuelbible.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.outofthebox.biblefaithfuelbible.R

enum class AppFontOption(val displayName: String) {
    DEFAULT("Default System"),
    INTER("Inter"),
    LIBRE_BASKERVILLE("Libre Baskerville"),
    LORA("Lora");

    fun toFontFamily(): FontFamily {
        return when (this) {
            DEFAULT -> FontFamily.Default
            INTER -> FontFamily(
                Font(R.font.inter_regular, FontWeight.Normal),
                Font(R.font.inter_bold, FontWeight.Bold),
                Font(R.font.inter_italic, FontWeight.Normal, FontStyle.Italic)
            )
            LIBRE_BASKERVILLE -> FontFamily(
                Font(R.font.libre_baskerville_regular, FontWeight.Normal),
                Font(R.font.libre_baskerville_bold, FontWeight.Bold),
                Font(R.font.libre_baskerville_italic, FontWeight.Normal, FontStyle.Italic)
            )
            LORA -> FontFamily(
                Font(R.font.lora_regular, FontWeight.Normal),
                Font(R.font.lora_bold, FontWeight.Bold),
                Font(R.font.lora_italic, FontWeight.Normal, FontStyle.Italic)
            )
        }
    }
}

val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
)
