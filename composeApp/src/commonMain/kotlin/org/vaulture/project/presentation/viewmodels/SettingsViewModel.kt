package org.vaulture.project.presentation.viewmodels

import com.russhwolf.settings.Settings
import com.russhwolf.settings.get
import com.russhwolf.settings.set
import dev.icerock.moko.mvvm.viewmodel.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.vaulture.project.presentation.theme.AppThemeMode
import org.vaulture.project.presentation.theme.ThemePalette

data class SettingsUiState(
    val themeMode: AppThemeMode = AppThemeMode.DARK,
    val themePalette: ThemePalette = ThemePalette.OCEAN,
    val notificationsEnabled: Boolean = true,
    val dataSaverEnabled: Boolean = false,
    val appVersion: String = "1.0.0"
)

/**
 * Mindset Pulse Settings Engine.
 * Responsible for preserving user comfort preferences across app restarts.
 */
class SettingsViewModel(
    private val settings: Settings
) : ViewModel() {

    companion object {
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_THEME_PALETTE = "theme_palette"
        private const val KEY_NOTIFS = "notifications_enabled"
        private const val KEY_DATA_SAVER = "data_saver_enabled"
    }

    private val _uiState = MutableStateFlow(
        SettingsUiState(
            themeMode = AppThemeMode.entries.find {
                it.name == settings.getString(KEY_THEME_MODE, AppThemeMode.DARK.name)
            } ?: AppThemeMode.DARK,

            themePalette = ThemePalette.entries.find {
                it.name == settings.getString(KEY_THEME_PALETTE, ThemePalette.OCEAN.name)
            } ?: ThemePalette.OCEAN,

            notificationsEnabled = settings.getBoolean(KEY_NOTIFS, true),
            dataSaverEnabled = settings.getBoolean(KEY_DATA_SAVER, false)
        )
    )
    val uiState = _uiState.asStateFlow()

    fun setThemeMode(mode: AppThemeMode) {
        settings.putString(KEY_THEME_MODE, mode.name) // Explicit type call for clarity
        _uiState.update { it.copy(themeMode = mode) }
    }

    fun setThemePalette(palette: ThemePalette) {
        settings.putString(KEY_THEME_PALETTE, palette.name)
        _uiState.update { it.copy(themePalette = palette) }
    }

    fun toggleNotifications(enabled: Boolean) {
        settings.putBoolean(KEY_NOTIFS, enabled)
        _uiState.update { it.copy(notificationsEnabled = enabled) }
    }

    fun toggleDataSaver(enabled: Boolean) {
        settings.putBoolean(KEY_DATA_SAVER, enabled)
        _uiState.update { it.copy(dataSaverEnabled = enabled) }
    }
}
