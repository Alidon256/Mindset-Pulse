package org.vaulture.project.utils

// In a full production app, you would replace this with a multiplatform settings library// like 'multiplatform-settings' to persist this flag on the user's device.
// For this example, we'll use a simple in-memory flag that resets on each app start.
object OnboardingManager {
    var hasCompletedOnboarding: Boolean = false
        private set

    fun completeOnboarding() {
        hasCompletedOnboarding = true
    }
}