package com.aevrontech.finevo.ui.theme

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import com.aevrontech.finevo.R

/** Android-specific Inter FontFamily using Google Fonts */
private val fontProvider =
    GoogleFont.Provider(
        providerAuthority = "com.google.android.gms.fonts",
        providerPackage = "com.google.android.gms",
        certificates = R.array.com_google_android_gms_fonts_certs
    )

private val interFont = GoogleFont("Inter")

actual val InterFontFamily: FontFamily =
    FontFamily(
        Font(
            googleFont = interFont,
            fontProvider = fontProvider,
            weight = FontWeight.Normal
        ),
        Font(
            googleFont = interFont,
            fontProvider = fontProvider,
            weight = FontWeight.Medium
        ),
        Font(
            googleFont = interFont,
            fontProvider = fontProvider,
            weight = FontWeight.SemiBold
        ),
        Font(googleFont = interFont, fontProvider = fontProvider, weight = FontWeight.Bold)
    )
