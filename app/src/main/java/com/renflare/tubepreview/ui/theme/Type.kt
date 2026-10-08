package com.renflare.tubepreview.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Android's native sans-serif gives the feed a familiar Roboto rhythm.
private val FeedFont = FontFamily.SansSerif

val Typography = Typography(
    titleLarge = TextStyle(
        fontFamily = FeedFont, fontWeight = FontWeight.Bold,
        fontSize = 22.sp, lineHeight = 28.sp, letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FeedFont, fontWeight = FontWeight.Medium,
        fontSize = 16.sp, lineHeight = 22.sp, letterSpacing = 0.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FeedFont, fontWeight = FontWeight.Normal,
        fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FeedFont, fontWeight = FontWeight.Normal,
        fontSize = 12.sp, lineHeight = 17.sp, letterSpacing = 0.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FeedFont, fontWeight = FontWeight.Medium,
        fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FeedFont, fontWeight = FontWeight.Normal,
        fontSize = 10.sp, lineHeight = 12.sp, letterSpacing = 0.sp
    )
)
