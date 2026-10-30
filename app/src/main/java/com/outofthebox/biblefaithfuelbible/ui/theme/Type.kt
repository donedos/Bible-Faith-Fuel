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
    LORA("Lora"),
    EB_GARAMOND("EB Garamond"),
    GENTIUM_BOOK_PLUS("Gentium Book Plus"),
    ROBOTO_FLEX("Roboto Flex");

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
            EB_GARAMOND -> FontFamily(
                Font(R.font.eb_garamond_regular, FontWeight.Normal),
                Font(R.font.eb_garamond_bold, FontWeight.Bold),
                Font(R.font.eb_garamond_italic, FontWeight.Normal, FontStyle.Italic),
                Font(R.font.eb_garamond_bold_italic, FontWeight.Bold, FontStyle.Italic)
            )
            GENTIUM_BOOK_PLUS -> FontFamily(
                Font(R.font.gentium_book_plus_regular, FontWeight.Normal),
                Font(R.font.gentium_book_plus_bold, FontWeight.Bold),
                Font(R.font.gentium_book_plus_italic, FontWeight.Normal, FontStyle.Italic),
                Font(R.font.gentium_book_plus_bold_italic, FontWeight.Bold, FontStyle.Italic)
            )
            ROBOTO_FLEX -> FontFamily(
                Font(R.font.roboto_flex_variable, FontWeight.Normal)
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
