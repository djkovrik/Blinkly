package com.sedsoftware.blinkly.compose.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import blinkly.shared.compose.generated.resources.Res
import blinkly.shared.compose.generated.resources.inter_bold
import blinkly.shared.compose.generated.resources.inter_bold_italic
import blinkly.shared.compose.generated.resources.inter_italic
import blinkly.shared.compose.generated.resources.inter_light
import blinkly.shared.compose.generated.resources.inter_light_italic
import blinkly.shared.compose.generated.resources.inter_medium
import blinkly.shared.compose.generated.resources.inter_medium_italic
import blinkly.shared.compose.generated.resources.inter_regular
import blinkly.shared.compose.generated.resources.inter_semibold
import blinkly.shared.compose.generated.resources.inter_semibold_italic
import org.jetbrains.compose.resources.Font

internal object BlinklyFonts {

    @Composable
    private fun interFontFamily() = FontFamily(
        Font(Res.font.inter_bold, FontWeight.Bold, FontStyle.Normal),
        Font(Res.font.inter_bold_italic, FontWeight.Bold, FontStyle.Italic),
        Font(Res.font.inter_medium, FontWeight.Medium, FontStyle.Normal),
        Font(Res.font.inter_medium_italic, FontWeight.Medium, FontStyle.Italic),
        Font(Res.font.inter_regular, FontWeight.Normal, FontStyle.Normal),
        Font(Res.font.inter_italic, FontWeight.Normal, FontStyle.Italic),
        Font(Res.font.inter_light, FontWeight.Light, FontStyle.Normal),
        Font(Res.font.inter_light_italic, FontWeight.Light, FontStyle.Italic),
        Font(Res.font.inter_semibold, FontWeight.SemiBold, FontStyle.Normal),
        Font(Res.font.inter_semibold_italic, FontWeight.SemiBold, FontStyle.Italic),
    )

    @Composable
    @Suppress("LongMethod")
    fun interTypography(): Typography =
        Typography(
            headlineLarge = TextStyle(
                fontFamily = interFontFamily(),
                fontWeight = FontWeight.SemiBold,
                fontStyle = FontStyle.Normal,
                fontSize = 40.sp,
                lineHeight = 48.sp,
                letterSpacing = 0.0.sp,
            ),
            headlineMedium = TextStyle(
                fontFamily = interFontFamily(),
                fontWeight = FontWeight.SemiBold,
                fontStyle = FontStyle.Normal,
                fontSize = 36.sp,
                lineHeight = 44.sp,
                letterSpacing = 0.0.sp,
            ),
            headlineSmall = TextStyle(
                fontFamily = interFontFamily(),
                fontWeight = FontWeight.Medium,
                fontStyle = FontStyle.Normal,
                fontSize = 32.sp,
                lineHeight = 40.sp,
                letterSpacing = 0.0.sp,
            ),
            titleLarge = TextStyle(
                fontFamily = interFontFamily(),
                fontWeight = FontWeight.SemiBold,
                fontStyle = FontStyle.Normal,
                fontSize = 28.sp,
                lineHeight = 34.sp,
                letterSpacing = 0.0.sp,
            ),
            titleMedium = TextStyle(
                fontFamily = interFontFamily(),
                fontWeight = FontWeight.Medium,
                fontStyle = FontStyle.Normal,
                fontSize = 24.sp,
                lineHeight = 30.sp,
                letterSpacing = 0.2.sp,
            ),
            titleSmall = TextStyle(
                fontFamily = interFontFamily(),
                fontWeight = FontWeight.Normal,
                fontStyle = FontStyle.Normal,
                fontSize = 20.sp,
                lineHeight = 26.sp,
                letterSpacing = 0.1.sp,
            ),
            bodyLarge = TextStyle(
                fontFamily = interFontFamily(),
                fontWeight = FontWeight.Normal,
                fontStyle = FontStyle.Normal,
                fontSize = 16.sp,
                lineHeight = 24.sp,
                letterSpacing = 0.5.sp,
            ),
            bodyMedium = TextStyle(
                fontFamily = interFontFamily(),
                fontWeight = FontWeight.Normal,
                fontStyle = FontStyle.Normal,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                letterSpacing = 0.2.sp,
            ),
            bodySmall = TextStyle(
                fontFamily = interFontFamily(),
                fontWeight = FontWeight.Normal,
                fontStyle = FontStyle.Normal,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                letterSpacing = 0.4.sp,
            ),
            labelLarge = TextStyle(
                fontFamily = interFontFamily(),
                fontWeight = FontWeight.SemiBold,
                fontStyle = FontStyle.Normal,
                fontSize = 14.sp,
                lineHeight = 18.sp,
                letterSpacing = 0.5.sp,
            ),
            labelMedium = TextStyle(
                fontFamily = interFontFamily(),
                fontWeight = FontWeight.Medium,
                fontStyle = FontStyle.Normal,
                fontSize = 14.sp,
                lineHeight = 18.sp,
                letterSpacing = 0.5.sp,
            ),
            labelSmall = TextStyle(
                fontFamily = interFontFamily(),
                fontWeight = FontWeight.Normal,
                fontStyle = FontStyle.Italic,
                fontSize = 14.sp,
                lineHeight = 18.sp,
                letterSpacing = 0.5.sp,
            )
        )
}
