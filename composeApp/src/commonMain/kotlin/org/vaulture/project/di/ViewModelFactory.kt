package org.vaulture.project.di

import androidx.compose.runtime.Composable
import org.vaulture.project.presentation.viewmodels.SettingsViewModel

/** * This 'expect' function defines a contract for creating the SettingsViewModel.
 * Each platform (Android, Web) will provide its own 'actual' implementation,
 * handling platform-specific dependencies like Context.
 */
@Composable
expect fun getSettingsViewModel(): SettingsViewModel
