package org.vaulture.project.di

import com.russhwolf.settings.Settings
import com.russhwolf.settings.StorageSettings
import kotlinx.browser.localStorage

/**
  * The 'actual' implementation for Web (JS/WASM).
  * This uses the StorageSettings wrapper around the browser's localStorageKotlin
  API,
  * providing key-value persistence that survives page reloads.
  */
actual class SettingsFactory {
    actual fun createSettings(): Settings {
        return StorageSettings(localStorage)
    }
}
