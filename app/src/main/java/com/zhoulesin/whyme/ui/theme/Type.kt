package com.zhoulesin.whyme.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.unit.sp

// Linear Design System Typography
val Typography = Typography(
    // Display XL
    displayLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight(510),
        fontSize = 72.sp,
        lineHeight = 72.sp, // 1.00 (tight)
        letterSpacing = (-1.584).sp
    ),
    // Display Large
    displayMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight(510),
        fontSize = 64.sp,
        lineHeight = 64.sp, // 1.00 (tight)
        letterSpacing = (-1.408).sp
    ),
    // Display
    displaySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight(510),
        fontSize = 48.sp,
        lineHeight = 48.sp, // 1.00 (tight)
        letterSpacing = (-1.056).sp
    ),
    // Heading 1
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight(400),
        fontSize = 32.sp,
        lineHeight = 36.sp, // 1.13 (tight)
        letterSpacing = (-0.704).sp
    ),
    // Heading 2
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight(400),
        fontSize = 24.sp,
        lineHeight = 32.sp, // 1.33
        letterSpacing = (-0.288).sp
    ),
    // Heading 3
    headlineSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight(590),
        fontSize = 20.sp,
        lineHeight = 27.sp, // 1.33
        letterSpacing = (-0.24).sp
    ),
    // Body Large
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight(400),
        fontSize = 18.sp,
        lineHeight = 29.sp, // 1.60 (relaxed)
        letterSpacing = (-0.165).sp
    ),
    // Body
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight(400),
        fontSize = 16.sp,
        lineHeight = 24.sp, // 1.50
        letterSpacing = 0.sp
    ),
    // Body Medium
    bodySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight(510),
        fontSize = 16.sp,
        lineHeight = 24.sp, // 1.50
        letterSpacing = 0.sp
    ),
    // Small
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight(400),
        fontSize = 15.sp,
        lineHeight = 24.sp, // 1.60 (relaxed)
        letterSpacing = (-0.165).sp
    ),
    // Caption
    labelMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight(400),
        fontSize = 13.sp,
        lineHeight = 20.sp, // 1.50
        letterSpacing = (-0.13).sp
    ),
    // Label
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight(400),
        fontSize = 12.sp,
        lineHeight = 17.sp, // 1.40
        letterSpacing = 0.sp
    )
)