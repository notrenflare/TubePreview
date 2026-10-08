package com.renflare.tubepreview.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.renflare.tubepreview.R

val RoundedUiFont = FontFamily(
    Font(R.font.nunito_regular, FontWeight.Normal),
    Font(R.font.nunito_semibold, FontWeight.SemiBold),
    Font(R.font.nunito_bold, FontWeight.Bold),
    Font(R.font.nunito_extrabold, FontWeight.ExtraBold)
)

val Typography = Typography(
    headlineLarge = TextStyle(fontFamily = RoundedUiFont, fontWeight = FontWeight.ExtraBold,
        fontSize = 34.sp, lineHeight = 39.sp, letterSpacing = (-0.6).sp),
    titleLarge = TextStyle(fontFamily = RoundedUiFont, fontWeight = FontWeight.ExtraBold,
        fontSize = 22.sp, lineHeight = 28.sp),
    titleMedium = TextStyle(fontFamily = RoundedUiFont, fontWeight = FontWeight.Bold,
        fontSize = 16.sp, lineHeight = 22.sp),
    bodyLarge = TextStyle(fontFamily = RoundedUiFont, fontWeight = FontWeight.Normal,
        fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontFamily = RoundedUiFont, fontWeight = FontWeight.Normal,
        fontSize = 13.sp, lineHeight = 18.sp),
    bodySmall = TextStyle(fontFamily = RoundedUiFont, fontWeight = FontWeight.Normal,
        fontSize = 12.sp, lineHeight = 17.sp),
    labelLarge = TextStyle(fontFamily = RoundedUiFont, fontWeight = FontWeight.Bold,
        fontSize = 14.sp, lineHeight = 20.sp),
    labelSmall = TextStyle(fontFamily = RoundedUiFont, fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp, lineHeight = 14.sp)
)
