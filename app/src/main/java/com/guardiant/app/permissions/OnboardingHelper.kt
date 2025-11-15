package com.guardiant.app.permissions

import android.content.Context
import android.content.SharedPreferences

class OnboardingHelper(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "onboarding_prefs"
        private const val KEY_CURRENT_STEP = "current_step"
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
    }

    /**
     * Guarda el paso actual
     */
    fun saveCurrentStep(step: Int) {
        prefs.edit().putInt(KEY_CURRENT_STEP, step).apply()
    }

    /**
     * Obtiene el paso actual guardado
     */
    fun getCurrentStep(): Int {
        return prefs.getInt(KEY_CURRENT_STEP, 0)
    }

    /**
     * Marca el onboarding como completado
     */
    fun markOnboardingCompleted() {
        prefs.edit()
            .putBoolean(KEY_ONBOARDING_COMPLETED, true)
            .putInt(KEY_CURRENT_STEP, 0) // Reset step
            .apply()
    }

    /**
     * Verifica si el onboarding fue completado
     */
    fun isOnboardingCompleted(): Boolean {
        return prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)
    }

    /**
     * Resetea el progreso del onboarding
     */
    fun resetOnboarding() {
        prefs.edit()
            .putInt(KEY_CURRENT_STEP, 0)
            .putBoolean(KEY_ONBOARDING_COMPLETED, false)
            .apply()
    }
}