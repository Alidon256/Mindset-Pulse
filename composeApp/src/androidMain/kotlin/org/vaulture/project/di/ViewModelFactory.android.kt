package org.vaulture.project.di

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.russhwolf.settings.Settings
import org.vaulture.project.presentation.viewmodels.SettingsViewModel

/**
 * The 'actual' implementation for Android.
 * It uses LocalContext.current to get the required context for SharedPreferences.
 */
@Composable
actual fun getSettingsViewModel(): SettingsViewModel {
    val context = LocalContext.current.applicationContext
    return remember {
        val settingsFactory = SettingsFactory(context)
        val settings: Settings = settingsFactory.createSettings()
        SettingsViewModel(settings)
    }
}
