package app.teloleo.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val Paper = Color(0xFFF6F0E6)
private val Ink = Color(0xFF1C1714)
private val Green = Color(0xFF0C6B62)
private val Card = Color(0xFFFFFBF5)
private val Line = Color(0xFFE5D8C7)
private val Muted = Color(0xFF6F645A)
private val Soft = Color(0xFFDDF1EC)
private val Coral = Color(0xFFC4482A)

private val Night = Color(0xFF1C1714)
private val NightRaised = Color(0xFF2A241F)
private val NightCard = Color(0xFF322B26)
private val NightInk = Color(0xFFF6F0E6)
private val NightMuted = Color(0xFFCDBFAF)
private val NightLine = Color(0xFF4E433A)
private val NightGreen = Color(0xFF8ED4C8)
private val NightSoft = Color(0xFF123F3B)
private val NightCoral = Color(0xFFFF8A72)

private val LightColors = lightColorScheme(
    primary = Green,
    onPrimary = Color.White,
    primaryContainer = Soft,
    onPrimaryContainer = Green,
    background = Paper,
    onBackground = Ink,
    surface = Paper,
    onSurface = Ink,
    surfaceVariant = Color(0xFFEFE6D8),
    onSurfaceVariant = Muted,
    surfaceContainerLow = Card,
    surfaceContainer = Card,
    surfaceContainerHigh = Card,
    outline = Line,
    outlineVariant = Line,
    error = Coral,
    onError = Color.White,
    inverseSurface = Ink,
    inverseOnSurface = Color.White,
)

private val DarkColors = darkColorScheme(
    primary = NightGreen,
    onPrimary = Night,
    primaryContainer = NightSoft,
    onPrimaryContainer = NightInk,
    background = Night,
    onBackground = NightInk,
    surface = Night,
    onSurface = NightInk,
    surfaceVariant = NightRaised,
    onSurfaceVariant = NightMuted,
    surfaceContainerLow = NightCard,
    surfaceContainer = NightCard,
    surfaceContainerHigh = NightRaised,
    outline = NightLine,
    outlineVariant = NightLine,
    error = NightCoral,
    onError = Night,
    inverseSurface = Paper,
    inverseOnSurface = Ink,
)

private val Serif = TextStyle(
    fontFamily = FontFamily.Serif,
    fontWeight = FontWeight.Medium,
)

private val BoardTypography = Typography(
    headlineLarge = Serif.copy(
        fontSize = 48.sp,
        lineHeight = 50.sp,
        letterSpacing = (-0.8).sp,
    ),
    headlineMedium = Serif.copy(fontSize = 32.sp, lineHeight = 36.sp),
    headlineSmall = Serif.copy(fontSize = 24.sp, lineHeight = 28.sp),
    titleLarge = Serif.copy(fontSize = 28.sp, lineHeight = 32.sp),
)

@Composable
fun TeLoLeoTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        typography = BoardTypography,
        content = content,
    )
}
