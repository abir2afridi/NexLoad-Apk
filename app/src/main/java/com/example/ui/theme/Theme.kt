package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val DarkColorScheme = darkColorScheme(
    primary = TealAccent,
    secondary = PurpleGrey80,
    tertiary = Pink80,
    background = SlateDark,
    surface = CardDark,
    onBackground = Color.White,
    onSurface = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = TealPrimary,
    secondary = PurpleGrey40,
    tertiary = Pink40,
    background = Color.White,
    surface = LightGray,
    onBackground = Color.Black,
    onSurface = Color.Black
)

private val BentoLightColorScheme = lightColorScheme(
    primary = BentoPrimary,
    secondary = BentoContainer,
    tertiary = BentoLightBlue,
    background = BentoBg,
    surface = Color.White,
    onBackground = BentoText,
    onSurface = BentoText,
    onSurfaceVariant = BentoText.copy(alpha = 0.7f),
    primaryContainer = BentoContainer,
    onPrimaryContainer = BentoOnContainer,
    surfaceVariant = BentoLightBlue,
    outline = BentoBorder
)

private val BentoDarkColorScheme = darkColorScheme(
    primary = BentoPrimary,
    secondary = BentoContainer,
    tertiary = BentoLightBlue,
    background = BentoDarkContainer,
    surface = Color(0xFF202325),
    onBackground = Color.White,
    onSurface = Color.White,
    primaryContainer = BentoPrimary,
    onPrimaryContainer = Color.White,
    surfaceVariant = Color(0xFF282B2E),
    outline = Color(0xFF3A3E42)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    isAmoled: Boolean = false,
    accentColor: String = "Bento",
    dynamicColor: Boolean = true,
    isMonochrome: Boolean = false,
    fontScale: Float = 1.0f,
    cornerRoundness: Float = 1.0f,
    selectedFontFamily: String = "Default",
    surfaceTintIntensity: Float = 0.0f,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    var colorScheme = when {
        isMonochrome -> {
            if (darkTheme) {
                darkColorScheme(
                    primary = Color.White,
                    onPrimary = Color.Black,
                    secondary = Color.LightGray,
                    background = if (isAmoled) Color.Black else Color(0xFF121212),
                    surface = if (isAmoled) Color.Black else Color(0xFF1E1E1E)
                )
            } else {
                lightColorScheme(
                    primary = Color.Black,
                    onPrimary = Color.White,
                    secondary = Color.DarkGray,
                    background = Color.White,
                    surface = Color(0xFFF5F5F5)
                )
            }
        }
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> {
            val base = when (accentColor) {
                "Bento" -> BentoDarkColorScheme
                "Teal" -> darkColorScheme(primary = TealAccent)
                "Blue" -> darkColorScheme(primary = BlueAccent)
                "Orange" -> darkColorScheme(primary = OrangeAccent)
                "Green" -> darkColorScheme(primary = GreenAccent)
                "Red" -> darkColorScheme(primary = RedAccent)
                "Purple" -> darkColorScheme(primary = PurpleAccent)
                "Pink" -> darkColorScheme(primary = PinkAccent)
                "Indigo" -> darkColorScheme(primary = IndigoAccent)
                "Cyan" -> darkColorScheme(primary = CyanAccent)
                "Amber" -> darkColorScheme(primary = AmberAccent)
                "Lime" -> darkColorScheme(primary = LimeAccent)
                "Deep Orange" -> darkColorScheme(primary = DeepOrangeAccent)
                "Brown" -> darkColorScheme(primary = BrownAccent)
                "Grey" -> darkColorScheme(primary = GreyAccent)
                "Blue Grey" -> darkColorScheme(primary = BlueGreyAccent)
                "Deep Purple" -> darkColorScheme(primary = DeepPurpleAccent)
                "Light Blue" -> darkColorScheme(primary = LightBlueAccent)
                "Light Green" -> darkColorScheme(primary = LightGreenAccent)
                else -> {
                    try {
                        darkColorScheme(primary = Color(android.graphics.Color.parseColor(accentColor)))
                    } catch (e: Exception) {
                        DarkColorScheme
                    }
                }
            }
            if (isAmoled) base.copy(background = PureBlack, surface = PureBlack) else base
        }
        else -> {
            when (accentColor) {
                "Bento" -> BentoLightColorScheme
                "Teal" -> lightColorScheme(primary = TealPrimary)
                "Blue" -> lightColorScheme(primary = BluePrimary)
                "Orange" -> lightColorScheme(primary = OrangePrimary)
                "Green" -> lightColorScheme(primary = GreenPrimary)
                "Red" -> lightColorScheme(primary = RedPrimary)
                "Purple" -> lightColorScheme(primary = PurplePrimary)
                "Pink" -> lightColorScheme(primary = PinkPrimary)
                "Indigo" -> lightColorScheme(primary = IndigoPrimary)
                "Cyan" -> lightColorScheme(primary = CyanPrimary)
                "Amber" -> lightColorScheme(primary = AmberPrimary)
                "Lime" -> lightColorScheme(primary = LimePrimary)
                "Deep Orange" -> lightColorScheme(primary = DeepOrangePrimary)
                "Brown" -> lightColorScheme(primary = BrownPrimary)
                "Grey" -> lightColorScheme(primary = GreyPrimary)
                "Blue Grey" -> lightColorScheme(primary = BlueGreyPrimary)
                "Deep Purple" -> lightColorScheme(primary = DeepPurplePrimary)
                "Light Blue" -> lightColorScheme(primary = LightBluePrimary)
                "Light Green" -> lightColorScheme(primary = LightGreenPrimary)
                else -> {
                    try {
                        lightColorScheme(primary = Color(android.graphics.Color.parseColor(accentColor)))
                    } catch (e: Exception) {
                        LightColorScheme
                    }
                }
            }
        }
    }

    // Apply surface tint intensity
    if (surfaceTintIntensity > 0f) {
        val tint = colorScheme.primary
        colorScheme = colorScheme.copy(
            surface = lerp(colorScheme.surface, tint, surfaceTintIntensity * 0.15f),
            surfaceVariant = lerp(colorScheme.surfaceVariant, tint, surfaceTintIntensity * 0.25f),
        )
    }

    // Apply corner roundness to shapes
    val shapes = Shapes(
        extraSmall = RoundedCornerShape((4 * cornerRoundness).dp),
        small = RoundedCornerShape((8 * cornerRoundness).dp),
        medium = RoundedCornerShape((12 * cornerRoundness).dp),
        large = RoundedCornerShape((16 * cornerRoundness).dp),
        extraLarge = RoundedCornerShape((28 * cornerRoundness).dp)
    )

    val fontFamily = when (selectedFontFamily) {
        "Sans-Serif" -> FontFamily.SansSerif
        "Serif" -> FontFamily.Serif
        "Monospace" -> FontFamily.Monospace
        else -> FontFamily.Default
    }

    // Apply font scale to typography
    val typography = Typography.copy(
        bodyLarge = Typography.bodyLarge.copy(fontSize = (Typography.bodyLarge.fontSize.value * fontScale).sp, fontFamily = fontFamily),
        bodyMedium = Typography.bodyMedium.copy(fontSize = (Typography.bodyMedium.fontSize.value * fontScale).sp, fontFamily = fontFamily),
        bodySmall = Typography.bodySmall.copy(fontSize = (Typography.bodySmall.fontSize.value * fontScale).sp, fontFamily = fontFamily),
        titleLarge = Typography.titleLarge.copy(fontSize = (Typography.titleLarge.fontSize.value * fontScale).sp, fontFamily = fontFamily),
        titleMedium = Typography.titleMedium.copy(fontSize = (Typography.titleMedium.fontSize.value * fontScale).sp, fontFamily = fontFamily),
        titleSmall = Typography.titleSmall.copy(fontSize = (Typography.titleSmall.fontSize.value * fontScale).sp, fontFamily = fontFamily),
        headlineLarge = Typography.headlineLarge.copy(fontSize = (Typography.headlineLarge.fontSize.value * fontScale).sp, fontFamily = fontFamily),
        headlineMedium = Typography.headlineMedium.copy(fontSize = (Typography.headlineMedium.fontSize.value * fontScale).sp, fontFamily = fontFamily),
        headlineSmall = Typography.headlineSmall.copy(fontSize = (Typography.headlineSmall.fontSize.value * fontScale).sp, fontFamily = fontFamily),
        labelLarge = Typography.labelLarge.copy(fontSize = (Typography.labelLarge.fontSize.value * fontScale).sp, fontFamily = fontFamily),
        labelMedium = Typography.labelMedium.copy(fontSize = (Typography.labelMedium.fontSize.value * fontScale).sp, fontFamily = fontFamily),
        labelSmall = Typography.labelSmall.copy(fontSize = (Typography.labelSmall.fontSize.value * fontScale).sp, fontFamily = fontFamily)
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        shapes = shapes,
        content = content
    )
}

private fun lerp(start: Color, stop: Color, fraction: Float): Color {
    return Color(
        red = start.red + (stop.red - start.red) * fraction,
        green = start.green + (stop.green - start.green) * fraction,
        blue = start.blue + (stop.blue - start.blue) * fraction,
        alpha = start.alpha + (stop.alpha - start.alpha) * fraction
    )
}
