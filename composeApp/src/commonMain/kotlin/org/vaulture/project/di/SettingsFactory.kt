package org.vaulture.project.di

import com.russhwolf.settings.Settings

/**
 * An 'expect' declaration defines a contract that platform-specific
 * code MUST implement. This allows our shared ViewModel to request a 'Settings'
 * instance without knowing if it's SharedPreferences or localStorage.
 */
expect class SettingsFactory {
    fun createSettings(): Settings
}
