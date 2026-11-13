package com.loveall.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.loveall.app.R

object AppSpacing {
    val tiny = 4.dp
    val small = 8.dp
    val compact = 12.dp
    val medium = 16.dp
    val large = 24.dp
    val extraLarge = 40.dp
}

object AppDimensions {
    val contentWidth = 640.dp
    val formWidth = 480.dp
    val touchTarget = 48.dp
    val buttonHeight = 56.dp
    val avatar = 64.dp
    val avatarLarge = 96.dp
    val logo = 80.dp
    val brandMark = 32.dp
    val icon = 24.dp
    val smallIcon = 18.dp
    val photoRadius = 32.dp
    val cardRadius = 24.dp
    val fieldRadius = 18.dp
    val border = 1.dp
    val elevation = 8.dp
    val smallElevation = 2.dp
    val profileAspectRatio = 0.82f
    val discoveryPhotoAspectRatio = 1.05f
    val compactPhoto = 120.dp
    val emptyPhotoHeight = 280.dp
    val artworkHeight = 240.dp
    val illustrationCardWidth = 142.dp
    val illustrationCardHeight = 190.dp
    val actionCircle = 64.dp
    val welcomeAspectRatio = 1.4f
    const val textFieldMaxLines = 5
    const val messageWidthFraction = 0.84f
    const val pressedScale = 0.97f
}

/** Decorative accents complement Material roles; text uses paired on-colors. */
@Immutable
data class BrandColors(
    val peach: Color,
    val lavender: Color,
    val mint: Color,
    val rose: Color,
    val ink: Color,
    val onInk: Color,
    val warmSkin: Color,
    val deepSkin: Color,
    val hair: Color,
    val heroStart: Color,
    val heroEnd: Color,
    val onHero: Color,
)

private val LightBrand =
    BrandColors(
        peach = Color(0xFFFFE7D7),
        lavender = Color(0xFFEAE2FA),
        mint = Color(0xFFDDF0E6),
        rose = Color(0xFFFFCEDB),
        ink = Color(0xFF2C2438),
        onInk = Color(0xFFFFF8F2),
        warmSkin = Color(0xFFE3A885),
        deepSkin = Color(0xFF9D5D48),
        hair = Color(0xFF352638),
        heroStart = Color(0xFFBA2455),
        heroEnd = Color(0xFF762C63),
        onHero = Color.White,
    )
private val DarkBrand =
    LightBrand.copy(
        peach = Color(0xFF4D302D),
        lavender = Color(0xFF382F50),
        mint = Color(0xFF223F38),
        rose = Color(0xFF542738),
        ink = Color(0xFF292334),
        heroStart = Color(0xFF872247),
        heroEnd = Color(0xFF442449),
    )
val LocalBrandColors = staticCompositionLocalOf { LightBrand }
val brandColors: BrandColors
    @Composable get() = LocalBrandColors.current

private val DarkColors =
    darkColorScheme(
        primary = Color(0xFFFFAFC5),
        onPrimary = Color(0xFF5C092A),
        primaryContainer = Color(0xFF702840),
        onPrimaryContainer = Color(0xFFFFD9E3),
        secondary = Color(0xFFD3BDF0),
        onSecondary = Color(0xFF3B2852),
        secondaryContainer = Color(0xFF42334F),
        onSecondaryContainer = Color(0xFFEEDCFB),
        tertiary = Color(0xFFABD2BF),
        onTertiary = Color(0xFF18392C),
        tertiaryContainer = Color(0xFF244C3B),
        onTertiaryContainer = Color(0xFFCAEEDC),
        background = Color(0xFF17141D),
        onBackground = Color(0xFFF4E8ED),
        surface = Color(0xFF1F1B25),
        onSurface = Color(0xFFF4E8ED),
        surfaceVariant = Color(0xFF413541),
        onSurfaceVariant = Color(0xFFD6C2CB),
        surfaceContainer = Color(0xFF27222E),
        surfaceContainerLow = Color(0xFF211C27),
        surfaceContainerHigh = Color(0xFF302936),
        surfaceContainerHighest = Color(0xFF3A303E),
        outlineVariant = Color(0xFF4D3C49),
        outline = Color(0xFFAA919D),
    )
private val LightColors =
    lightColorScheme(
        primary = Color(0xFFB92356),
        onPrimary = Color.White,
        primaryContainer = Color(0xFFFFDFE7),
        onPrimaryContainer = Color(0xFF65132F),
        secondary = Color(0xFF6C4C82),
        onSecondary = Color.White,
        secondaryContainer = Color(0xFFECE1F7),
        onSecondaryContainer = Color(0xFF412353),
        tertiary = Color(0xFF32644E),
        onTertiary = Color.White,
        tertiaryContainer = Color(0xFFDDF0E6),
        onTertiaryContainer = Color(0xFF1F4936),
        background = Color(0xFFFFF8F2),
        onBackground = Color(0xFF2C2438),
        surface = Color(0xFFFFFCF9),
        onSurface = Color(0xFF2C2438),
        surfaceVariant = Color(0xFFF3E8E7),
        onSurfaceVariant = Color(0xFF74616D),
        surfaceContainer = Color(0xFFF9EFEB),
        surfaceContainerLow = Color(0xFFFFFCF9),
        surfaceContainerHigh = Color(0xFFF4E9E5),
        surfaceContainerHighest = Color(0xFFEEDFDA),
        outlineVariant = Color(0xFFEADCD8),
        outline = Color(0xFF9A818C),
    )
private val EditorialFont = FontFamily(Font(R.font.dm_serif_display))
private val BodyFont =
    FontFamily(
        Font(R.font.manrope_regular, FontWeight.Normal),
        Font(R.font.manrope_semibold, FontWeight.SemiBold),
        Font(R.font.manrope_bold, FontWeight.Bold),
    )
private val AppTypography =
    Typography(
        displayLarge =
            TextStyle(
                fontFamily = EditorialFont,
                fontSize = 54.sp,
                lineHeight = 58.sp,
                letterSpacing = (-1.8).sp,
            ),
        displayMedium =
            TextStyle(
                fontFamily = EditorialFont,
                fontSize = 46.sp,
                lineHeight = 50.sp,
                letterSpacing = (-1.3).sp,
            ),
        displaySmall =
            TextStyle(
                fontFamily = EditorialFont,
                fontSize = 38.sp,
                lineHeight = 43.sp,
                letterSpacing = (-0.7).sp,
            ),
        headlineLarge = TextStyle(fontFamily = EditorialFont, fontSize = 34.sp, lineHeight = 40.sp),
        headlineMedium =
            TextStyle(fontFamily = EditorialFont, fontSize = 30.sp, lineHeight = 36.sp),
        headlineSmall = TextStyle(fontFamily = EditorialFont, fontSize = 26.sp, lineHeight = 32.sp),
        titleLarge =
            TextStyle(
                fontFamily = BodyFont,
                fontWeight = FontWeight.Bold,
                fontSize = 21.sp,
                lineHeight = 28.sp,
            ),
        titleMedium =
            TextStyle(
                fontFamily = BodyFont,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                lineHeight = 23.sp,
            ),
        titleSmall =
            TextStyle(
                fontFamily = BodyFont,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                lineHeight = 20.sp,
            ),
        bodyLarge = TextStyle(fontFamily = BodyFont, fontSize = 16.sp, lineHeight = 25.sp),
        bodyMedium = TextStyle(fontFamily = BodyFont, fontSize = 14.sp, lineHeight = 22.sp),
        bodySmall = TextStyle(fontFamily = BodyFont, fontSize = 12.sp, lineHeight = 18.sp),
        labelLarge =
            TextStyle(
                fontFamily = BodyFont,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                lineHeight = 20.sp,
            ),
        labelMedium =
            TextStyle(
                fontFamily = BodyFont,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                lineHeight = 18.sp,
            ),
        labelSmall =
            TextStyle(
                fontFamily = BodyFont,
                fontWeight = FontWeight.SemiBold,
                fontSize = 10.sp,
                lineHeight = 14.sp,
                letterSpacing = 0.4.sp,
            ),
    )

@Composable
fun LoveAllTheme(dark: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalBrandColors provides if (dark) DarkBrand else LightBrand) {
        MaterialTheme(
            colorScheme = if (dark) DarkColors else LightColors,
            typography = AppTypography,
            shapes =
                Shapes(
                    small = RoundedCornerShape(AppDimensions.fieldRadius),
                    medium = RoundedCornerShape(AppDimensions.cardRadius),
                    large = RoundedCornerShape(AppDimensions.photoRadius),
                ),
            content = content,
        )
    }
}
