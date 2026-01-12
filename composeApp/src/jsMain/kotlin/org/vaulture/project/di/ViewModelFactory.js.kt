package org.vaulture.project.di

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.russhwolf.settings.Settings
import org.vaulture.project.presentation.viewmodels.SettingsViewModel

/**
 * The 'actual' implementation for Web.
 * It creates a context-free SettingsFactory for localStorage.
 */
@Composable
actual fun getSettingsViewModel(): SettingsViewModel {
    return remember {
        val settingsFactory = SettingsFactory()
        val settings: Settings = settingsFactory.createSettings()
        SettingsViewModel(settings)
    }
}
