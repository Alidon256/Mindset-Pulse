package org.vaulture.project.data.local


object OnboardingManager {
    var hasCompletedOnboarding: Boolean = false
        private set

    fun completeOnboarding() {
        hasCompletedOnboarding = true
    }
}