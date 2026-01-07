package org.vaulture.project.presentation.viewmodels

import dev.icerock.moko.mvvm.viewmodel.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.vaulture.project.presentation.theme.AppThemeMode
import org.vaulture.project.presentation.theme.ThemePalette

data class SettingsUiState(
    val themeMode: AppThemeMode = AppThemeMode.DARK,
    val themePalette: ThemePalette = ThemePalette.NATURE,
    val notificationsEnabled: Boolean = true,
    val dataSaverEnabled: Boolean = false,
    val appVersion: String = "1.0.0"
)

class SettingsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState = _uiState.asStateFlow()

    fun setThemeMode(mode: AppThemeMode) {
        _uiState.update { it.copy(themeMode = mode) }
    }

    fun setThemePalette(palette: ThemePalette) {
        _uiState.update { it.copy(themePalette = palette) }
    }

    fun toggleNotifications(enabled: Boolean) {
        _uiState.update { it.copy(notificationsEnabled = enabled) }
    }

    fun toggleDataSaver(enabled: Boolean) {
        _uiState.update { it.copy(dataSaverEnabled = enabled) }
    }
}
