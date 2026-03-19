package com.exory550.exorygallery.presentation.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.exory550.exorygallery.R

val ExoryFontFamily = FontFamily(
    Font(R.font.exory_regular, FontWeight.Normal),
    Font(R.font.exory_medium, FontWeight.Medium),
    Font(R.font.exory_bold, FontWeight.Bold)
)

val ExoryTypography = Typography(
    displayLarge = TextStyle(fontFamily = ExoryFontFamily, fontWeight = FontWeight.Bold, fontSize = 36.sp),
    displayMedium = TextStyle(fontFamily = ExoryFontFamily, fontWeight = FontWeight.Bold, fontSize = 28.sp),
    headlineLarge = TextStyle(fontFamily = ExoryFontFamily, fontWeight = FontWeight.Bold, fontSize = 24.sp),
    headlineMedium = TextStyle(fontFamily = ExoryFontFamily, fontWeight = FontWeight.Medium, fontSize = 20.sp),
    titleLarge = TextStyle(fontFamily = ExoryFontFamily, fontWeight = FontWeight.Medium, fontSize = 18.sp),
    titleMedium = TextStyle(fontFamily = ExoryFontFamily, fontWeight = FontWeight.Medium, fontSize = 16.sp),
    bodyLarge = TextStyle(fontFamily = ExoryFontFamily, fontWeight = FontWeight.Normal, fontSize = 16.sp),
    bodyMedium = TextStyle(fontFamily = ExoryFontFamily, fontWeight = FontWeight.Normal, fontSize = 14.sp),
    labelLarge = TextStyle(fontFamily = ExoryFontFamily, fontWeight = FontWeight.Medium, fontSize = 14.sp),
    labelMedium = TextStyle(fontFamily = ExoryFontFamily, fontWeight = FontWeight.Normal, fontSize = 12.sp)
)
