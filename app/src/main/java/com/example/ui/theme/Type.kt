package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

// Typography matching Mobbin spec (Saans / Plus Jakarta Sans)
// Tight leading for headings, zero letter spacing, clean hierarchy
val MobbinFontFamily = FontFamily(
    Font(R.font.plus_jakarta_sans, FontWeight.Normal),
    Font(R.font.plus_jakarta_sans, FontWeight.Medium),
    Font(R.font.plus_jakarta_sans, FontWeight.SemiBold),
    Font(R.font.plus_jakarta_sans, FontWeight.Bold),
    Font(R.font.plus_jakarta_sans, FontWeight.ExtraBold)
)

val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = MobbinFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        lineHeight = 38.sp,
        letterSpacing = 0.sp,
        color = MobbinInk
    ),
    displayMedium = TextStyle(
        fontFamily = MobbinFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 30.sp,
        lineHeight = 34.sp,
        letterSpacing = 0.sp,
        color = MobbinInk
    ),
    displaySmall = TextStyle(
        fontFamily = MobbinFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp,
        color = MobbinInk
    ),
    headlineLarge = TextStyle(
        fontFamily = MobbinFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.sp,
        color = MobbinInk
    ),
    headlineMedium = TextStyle(
        fontFamily = MobbinFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp,
        color = MobbinInk
    ),
    headlineSmall = TextStyle(
        fontFamily = MobbinFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.sp,
        color = MobbinInk
    ),
    titleLarge = TextStyle(
        fontFamily = MobbinFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp,
        color = MobbinInk
    ),
    titleMedium = TextStyle(
        fontFamily = MobbinFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.sp,
        color = MobbinInk
    ),
    titleSmall = TextStyle(
        fontFamily = MobbinFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp,
        color = MobbinInk
    ),
    bodyLarge = TextStyle(
        fontFamily = MobbinFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.sp,
        color = MobbinInk
    ),
    bodyMedium = TextStyle(
        fontFamily = MobbinFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp,
        color = MobbinInkSoft
    ),
    bodySmall = TextStyle(
        fontFamily = MobbinFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.sp,
        color = MobbinInkSoft
    ),
    labelLarge = TextStyle(
        fontFamily = MobbinFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.sp,
        color = MobbinInk
    ),
    labelMedium = TextStyle(
        fontFamily = MobbinFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.sp,
        color = MobbinInkSoft
    ),
    labelSmall = TextStyle(
        fontFamily = MobbinFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 10.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.sp,
        color = MobbinTextFaint
    )
)
