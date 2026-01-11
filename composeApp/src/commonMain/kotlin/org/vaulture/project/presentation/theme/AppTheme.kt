package org.vaulture.project.presentation.theme

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
import mindsetpulse.composeapp.generated.resources.Res
import mindsetpulse.composeapp.generated.resources.poppins_bold
import mindsetpulse.composeapp.generated.resources.poppins_medium
import mindsetpulse.composeapp.generated.resources.poppins_regular
import org.jetbrains.compose.resources.Font


@Composable
fun PoppinsFontFamily(): FontFamily = FontFamily(
    Font(Res.font.poppins_regular, weight = FontWeight.Normal),
    Font(Res.font.poppins_bold, weight = FontWeight.Bold),
    Font(Res.font.poppins_medium, weight = FontWeight.Medium)
)

@Composable
fun PoppinsTypography(): Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = PoppinsFontFamily(),
        fontWeight = FontWeight.Bold,
        fontSize = 34.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = PoppinsFontFamily(),
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = PoppinsFontFamily(),
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    ),
    labelLarge = TextStyle(
        fontFamily = PoppinsFontFamily(),
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp
    ),
    bodySmall = TextStyle(
        fontFamily = PoppinsFontFamily(),
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp
    )
)

private val NatureLightColors = lightColorScheme(
    primary = Color(0xFF2E7D32),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD0E4FF),
    onPrimaryContainer = Color(0xFF001D36),
    secondary = Color(0xFF535F70),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD7E3F7),
    onSecondaryContainer = Color(0xFF101C2B),
    tertiary = Color(0xFF9C27B0),
    onTertiary = Color(0xFF3B2948),
    tertiaryContainer = Color(0xFFF2DAFF),
    onTertiaryContainer = Color(0xFF251431),
    error = Color(0xFFBA1A1A),
    background = Color(0xFFFDFCFF),
    onBackground = Color(0xFF1A1C1E),
    surface = Color(0xFFFDFCFF),
    onSurface = Color(0xFF1A1C1E),
)

private val NatureDarkColors = darkColorScheme(
    primary = Color(0xFF2E7D32),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF00497D),
    onPrimaryContainer = Color(0xFFD0E4FF),
    secondary = Color(0xFFBBC7DB),
    onSecondary = Color(0xFF253140),
    secondaryContainer = Color(0xFF3B4858),
    onSecondaryContainer = Color(0xFFD7E3F7),
    tertiary = Color(0xFF9C27B0),
    onTertiary = Color(0xFF3B2948),
    tertiaryContainer = Color(0xFF523F5F),
    onTertiaryContainer = Color(0xFFF2DAFF),
    error = Color(0xFFFFB4AB),
    background = Color.Black,
    onBackground = Color(0xFFE2E2E6),
    surface = Color(0xFF1A1C1E),
    onSurface = Color(0xFFE2E2E6),
)

private val OceanLightColors = lightColorScheme(
    primary = Color(0xFF42A5F5),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF42A5F5).copy(alpha = 0.1f),
    onPrimaryContainer = Color(0xFF42A5F5).copy(alpha = 0.9f),
    secondary = Color(0xFF26A69A),
    onSecondary = Color(0xFFFFFFFF), // Teal is dark enough for white text
    secondaryContainer = Color(0xFF26A69A).copy(alpha = 0.1f),
    onSecondaryContainer = Color(0xFF26A69A).copy(alpha = 0.9f),
    background = Color(0xFFFFFFFF),
    onBackground = Color(0xFF0A1014),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF0A1014),
    surfaceVariant = Color(0xFFE0E0E0).copy(alpha = 0.5f),
    onSurfaceVariant = Color(0xFF0A1014).copy(alpha = 0.7f),
    error = Color(0xFFF48FB1),
    onError = Color(0xFF0A1014),
    outline = Color(0xFF42A5F5).copy(alpha = 0.5f)
)

private val OceanDarkColors = darkColorScheme(
    primary = Color(0xFF42A5F5).copy(alpha = 0.8f),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF42A5F5).copy(alpha = 0.25f),
    onPrimaryContainer = Color(0xFFFFFFFF).copy(alpha = 0.9f),
    secondary = Color(0xFF26A69A).copy(alpha = 0.8f),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFF26A69A).copy(alpha = 0.25f),
    onSecondaryContainer = Color(0xFFFFFFFF).copy(alpha = 0.9f),
    background = Color(0xFF121212),
    onBackground = Color(0xFFFFFFFF).copy(alpha = 0.87f),
    surface = Color(0xFF1E1E1E),
    onSurface = Color(0xFFFFFFFF).copy(alpha = 0.87f),
    surfaceVariant = Color(0xFF424242).copy(alpha = 0.7f),
    onSurfaceVariant = Color(0xFFFFFFFF).copy(alpha = 0.7f),
    error = Color(0xFFF48FB1).copy(alpha = 0.9f),
    onError = Color(0xFF0A1014),
    outline = Color(0xFF42A5F5).copy(alpha = 0.5f)
)

private val SunsetLightColors = lightColorScheme(
    primary = Color(0xFF9C4146),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDAD9),
    onPrimaryContainer = Color(0xFF40000A),
    secondary = Color(0xFF775656),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFDAD9),
    onSecondaryContainer = Color(0xFF2C1516),
    tertiary = Color(0xFF755A2F),
    onTertiary = Color.White,
    background = Color(0xFFFFF8F6),
    surface = Color(0xFFFFF8F6),
    onSurface = Color(0xFF231919)
)

private val SunsetDarkColors = darkColorScheme(
    primary = Color(0xFFFFB3B4),
    onPrimary = Color(0xFF5F131C),
    primaryContainer = Color(0xFF7E2A30),
    onPrimaryContainer = Color(0xFFFFDAD9),
    secondary = Color(0xFFE6BDBC),
    onSecondary = Color(0xFF44292A),
    secondaryContainer = Color(0xFF5D3F3F),
    onSecondaryContainer = Color(0xFFFFDAD9),
    tertiary = Color(0xFFE5C18D),
    onTertiary = Color(0xFF422C05),
    background = Color(0xFF201A1A),
    surface = Color(0xFF201A1A),
    onSurface = Color(0xFFEDE0DE)
)


enum class AppThemeMode {
    SYSTEM, LIGHT, DARK
}

enum class ThemePalette {
    NATURE, OCEAN, SUNSET
}

@Composable
fun AppTheme(
    themeMode: AppThemeMode = AppThemeMode.DARK,
    themePalette: ThemePalette = ThemePalette.OCEAN,
    content: @Composable () -> Unit
) {
    val isDark = when (themeMode) {
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
    }

    val colorScheme = when (themePalette) {
        ThemePalette.OCEAN -> if (isDark) OceanDarkColors else OceanLightColors
        ThemePalette.NATURE -> if (isDark) NatureDarkColors else NatureLightColors
        ThemePalette.SUNSET -> if (isDark) SunsetDarkColors else SunsetLightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = PoppinsTypography(),
        content = content
    )
}
