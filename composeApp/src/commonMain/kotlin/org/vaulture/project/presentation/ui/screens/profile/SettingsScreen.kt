package org.vaulture.project.presentation.ui.screens.profile

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.vaulture.project.presentation.theme.AppThemeMode
import org.vaulture.project.presentation.theme.PoppinsTypography
import org.vaulture.project.presentation.theme.ThemePalette
import org.vaulture.project.presentation.viewmodels.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit,
    onSignOut: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Settings",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            val contentModifier = if (maxWidth > 800.dp) {
                Modifier
                    .width(600.dp)
                    .align(Alignment.TopCenter)
            } else {
                Modifier.fillMaxWidth()
            }

            LazyColumn(
                modifier = contentModifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {

                item {
                    SettingsSectionTitle("Appearance")

                    ThemeModeSelectorCard(
                        currentMode = state.themeMode,
                        onModeSelected = { viewModel.setThemeMode(it) }
                    )

                    Spacer(Modifier.height(16.dp))

                    ColorPaletteSelectorCard(
                        currentPalette = state.themePalette,
                        onPaletteSelected = { viewModel.setThemePalette(it) }
                    )
                }

                item {
                    SettingsSectionTitle("Preferences")
                    SettingsCard {
                        SettingsSwitchItem(
                            icon = Icons.Default.Notifications,
                            title = "Notifications",
                            subtitle = "Daily reminders and insights",
                            checked = state.notificationsEnabled,
                            onCheckedChange = { viewModel.toggleNotifications(it) }
                        )
                        HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                        SettingsSwitchItem(
                            icon = Icons.Default.Wifi,
                            title = "Data Saver",
                            subtitle = "Reduce data usage on mobile networks",
                            checked = state.dataSaverEnabled,
                            onCheckedChange = { viewModel.toggleDataSaver(it) }
                        )
                    }
                }

                item {
                    Spacer(Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = onSignOut,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.error)
                        )
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Logout,
                            null
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Sign Out",
                            style = PoppinsTypography().titleMedium
                        )
                    }
                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun ThemeModeSelectorCard(
    currentMode: AppThemeMode,
    onModeSelected: (AppThemeMode) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Brightness4,
                    null,
                    tint = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    "Theme Mode",
                    style = PoppinsTypography().titleMedium,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ThemeOptionItem(
                    modifier = Modifier.weight(1f),
                    isSelected = currentMode == AppThemeMode.LIGHT,
                    icon = Icons.Default.LightMode,
                    label = "Light",
                    onClick = { onModeSelected(AppThemeMode.LIGHT) }
                )
                ThemeOptionItem(
                    modifier = Modifier.weight(1f),
                    isSelected = currentMode == AppThemeMode.DARK,
                    icon = Icons.Default.DarkMode,
                    label = "Dark",
                    onClick = { onModeSelected(AppThemeMode.DARK) }
                )
                ThemeOptionItem(
                    modifier = Modifier.weight(1f),
                    isSelected = currentMode == AppThemeMode.SYSTEM,
                    icon = Icons.Default.SettingsBrightness,
                    label = "System",
                    onClick = { onModeSelected(AppThemeMode.SYSTEM) }
                )
            }
        }
    }
}

@Composable
fun ColorPaletteSelectorCard(
    currentPalette: ThemePalette,
    onPaletteSelected: (ThemePalette) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Palette,
                    null,
                    tint = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    "Color Palette",
                    style = PoppinsTypography().titleMedium,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(Modifier.height(16.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PaletteOptionRow(
                    palette = ThemePalette.NATURE,
                    currentPalette = currentPalette,
                    label = "Nature's Embrace",
                    colorPreview = Color(0xFF2E7D32), // Green
                    onClick = onPaletteSelected
                )
                PaletteOptionRow(
                    palette = ThemePalette.OCEAN,
                    currentPalette = currentPalette,
                    label = "Calm Ocean",
                    colorPreview = Color(0xFF006495), // Blue
                    onClick = onPaletteSelected
                )
                PaletteOptionRow(
                    palette = ThemePalette.SUNSET,
                    currentPalette = currentPalette,
                    label = "Serene Sunset",
                    colorPreview = Color(0xFF9C4146), // Red/Orange
                    onClick = onPaletteSelected
                )
            }
        }
    }
}

@Composable
fun PaletteOptionRow(
    palette: ThemePalette,
    currentPalette: ThemePalette,
    label: String,
    colorPreview: Color,
    onClick: (ThemePalette) -> Unit
) {
    val isSelected = palette == currentPalette
    val backgroundColor by animateColorAsState(
        if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .clickable { onClick(palette) }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(colorPreview)
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), CircleShape)
        )

        Spacer(Modifier.width(16.dp))

        Text(
            text = label,
            style = PoppinsTypography().bodyMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(Modifier.weight(1f))

        if (isSelected) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Selected",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun SettingsSectionTitle(text: String) {
    Text(
        text = text,
        style = PoppinsTypography().titleMedium,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
    )
}

@Composable
fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            content()
        }
    }
}

@Composable
fun ThemeOptionItem(
    modifier: Modifier = Modifier,
    isSelected: Boolean,
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
        label = "ThemeBg"
    )
    val contentColor by animateColorAsState(
        if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
        label = "ThemeContent"
    )
    val scale by animateFloatAsState(if (isSelected) 1.05f else 1f, label = "ThemeScale")

    Column(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .border(
                width = if (isSelected) 0.dp else 1.dp,
                color = if (isSelected) Color.Transparent else MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = contentColor
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = contentColor, fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun SettingsSwitchItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(16.dp))
        Column(
            modifier = Modifier
                .weight(1f)
        ) {
            Text(
                text = title,
                style = PoppinsTypography().bodyLarge
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = PoppinsTypography().bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
