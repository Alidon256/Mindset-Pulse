/*package org.vaulture.project.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key.Companion.R
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.Font
import vaulture.composeapp.generated.resources.Res
import vaulture.composeapp.generated.resources.poppins_bold
import vaulture.composeapp.generated.resources.poppins_medium
import vaulture.composeapp.generated.resources.poppins_regular

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
private val LightColors = lightColorScheme(
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

private val DarkColors = darkColorScheme(
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

@Composable
fun AppTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (!useDarkTheme) {
        LightColors
    } else {
        DarkColors
    }

    MaterialTheme(
        colorScheme = colors,
        content = content
    )
}*/
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
import org.jetbrains.compose.resources.Font
import vaulture.composeapp.generated.resources.Res
import vaulture.composeapp.generated.resources.poppins_bold
import vaulture.composeapp.generated.resources.poppins_medium
import vaulture.composeapp.generated.resources.poppins_regular

// --- 1. FONTS (Existing) ---
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

// --- 2. COLOR PALETTES ---

// Palette 1: Nature's Embrace (Greens - Growth & Balance) - DEFAULT
private val NatureLightColors = lightColorScheme(
    primary = Color(0xFF2E7D32),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFA5D6A7),
    onPrimaryContainer = Color(0xFF003300),
    secondary = Color(0xFF558B2F),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDCEDC8),
    onSecondaryContainer = Color(0xFF1B3304),
    tertiary = Color(0xFF00695C),
    onTertiary = Color.White,
    background = Color(0xFFF1F8E9),
    surface = Color(0xFFFDFDFD),
    onSurface = Color(0xFF1B1C18),
    error = Color(0xFFBA1A1A)
)

private val NatureDarkColors = darkColorScheme(
    primary = Color(0xFF81C784),
    onPrimary = Color(0xFF003300),
    primaryContainer = Color(0xFF2E7D32),
    onPrimaryContainer = Color(0xFFE8F5E9),
    secondary = Color(0xFFAED581),
    onSecondary = Color(0xFF1B3304),
    secondaryContainer = Color(0xFF558B2F),
    onSecondaryContainer = Color(0xFFF1F8E9),
    tertiary = Color(0xFF80CBC4),
    onTertiary = Color(0xFF003300),
    background = Color(0xFF121212), // Pure black for OLED savings or dark grey
    surface = Color(0xFF1E1E1E),
    onSurface = Color(0xFFE2E2E6),
    error = Color(0xFFFFB4AB)
)

// Palette 2: Calm Ocean (Blues/Teals - Focus & Clarity)
private val OceanLightColors = lightColorScheme(
    primary = Color(0xFF006495),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFCBE6FF),
    onPrimaryContainer = Color(0xFF001E30),
    secondary = Color(0xFF50606E),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD3E4F5),
    onSecondaryContainer = Color(0xFF0C1D29),
    tertiary = Color(0xFF65587B),
    onTertiary = Color.White,
    background = Color(0xFFF8FDFF),
    surface = Color(0xFFF8FDFF),
    onSurface = Color(0xFF001F25)
)

private val OceanDarkColors = darkColorScheme(
    primary = Color(0xFF8DCDFF),
    onPrimary = Color(0xFF00344F),
    primaryContainer = Color(0xFF004B71),
    onPrimaryContainer = Color(0xFFCBE6FF),
    secondary = Color(0xFFB7C8D9),
    onSecondary = Color(0xFF22323F),
    secondaryContainer = Color(0xFF394956),
    onSecondaryContainer = Color(0xFFD3E4F5),
    tertiary = Color(0xFFD0C0E8),
    onTertiary = Color(0xFF362B4A),
    background = Color(0xFF111318),
    surface = Color(0xFF111318),
    onSurface = Color(0xFFE1E2E8)
)

// Palette 3: Serene Sunset (Warm Oranges/Purples - Comfort & Creativity)
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

// --- 3. THEME ENUMS & HELPER ---

enum class AppThemeMode {
    SYSTEM, LIGHT, DARK
}

enum class ThemePalette {
    NATURE, OCEAN, SUNSET
}

@Composable
fun AppTheme(
    themeMode: AppThemeMode = AppThemeMode.DARK, // Default to Dark
    themePalette: ThemePalette = ThemePalette.NATURE, // Default to Nature
    content: @Composable () -> Unit,
    useDarkTheme: Boolean
) {
    val isDark = when (themeMode) {
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
        AppThemeMode.LIGHT -> useDarkTheme
        AppThemeMode.DARK -> true
    }

    val colorScheme = when (themePalette) {
        ThemePalette.NATURE -> if (isDark) NatureDarkColors else NatureLightColors
        ThemePalette.OCEAN -> if (isDark) OceanDarkColors else OceanLightColors
        ThemePalette.SUNSET -> if (isDark) SunsetDarkColors else SunsetLightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = PoppinsTypography(),
        content = content
    )
}
