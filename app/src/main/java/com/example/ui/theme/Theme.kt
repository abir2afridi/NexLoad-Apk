package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

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
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val colorScheme = when {
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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
