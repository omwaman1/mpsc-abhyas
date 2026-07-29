package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.example.R

val fontProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

val poppinsFont = GoogleFont("Poppins")

val PoppinsFontFamily = FontFamily(
    Font(googleFont = poppinsFont, fontProvider = fontProvider, weight = FontWeight.Normal),
    Font(googleFont = poppinsFont, fontProvider = fontProvider, weight = FontWeight.Medium),
    Font(googleFont = poppinsFont, fontProvider = fontProvider, weight = FontWeight.SemiBold),
    Font(googleFont = poppinsFont, fontProvider = fontProvider, weight = FontWeight.Bold),
    Font(googleFont = poppinsFont, fontProvider = fontProvider, weight = FontWeight.ExtraBold)
)

// Global Poppins Typography for Material 3 Theme
val Typography = Typography(
    displayLarge = TextStyle(fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Normal, fontSize = 57.sp),
    displayMedium = TextStyle(fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Normal, fontSize = 45.sp),
    displaySmall = TextStyle(fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Normal, fontSize = 36.sp),
    headlineLarge = TextStyle(fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Bold, fontSize = 32.sp),
    headlineMedium = TextStyle(fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Bold, fontSize = 28.sp),
    headlineSmall = TextStyle(fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Bold, fontSize = 24.sp),
    titleLarge = TextStyle(fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Bold, fontSize = 22.sp),
    titleMedium = TextStyle(fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp),
    titleSmall = TextStyle(fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp),
    bodyLarge = TextStyle(fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall = TextStyle(fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 16.sp),
    labelLarge = TextStyle(fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Medium, fontSize = 14.sp),
    labelMedium = TextStyle(fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Medium, fontSize = 12.sp),
    labelSmall = TextStyle(fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Medium, fontSize = 11.sp)
)
