package com.guardiant.app.security

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.guardiant.app.network.AppConfig

/**
 * Gestiona el estado de coerción (modo de seguridad activado)
 * y la lista de apps protegidas usando SharedPreferences
 */
class CoercionStateManager(context: Context) {

    companion object {
        private const val TAG = "CoercionStateManager"
        private const val PREFS_NAME = "guardiant_coercion_prefs"
        private const val KEY_COERCION_MODE = "is_coercion_mode"
        private const val KEY_PROTECTED_APPS = "protected_apps_json"
        
        @Volatile
        private var instance: CoercionStateManager? = null
        
        fun getInstance(context: Context): CoercionStateManager {
            return instance ?: synchronized(this) {
                instance ?: CoercionStateManager(context.applicationContext).also { instance = it }
            }
        }
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    /**
     * Activa el modo de coerción
     */
    fun enableCoercionMode() {
        prefs.edit().putBoolean(KEY_COERCION_MODE, true).apply()
        Log.w(TAG, "🚨 Modo de coerción ACTIVADO")
    }

    /**
     * Desactiva el modo de coerción
     */
    fun disableCoercionMode() {
        prefs.edit()
            .putBoolean(KEY_COERCION_MODE, false)
            .remove(KEY_PROTECTED_APPS)
            .apply()
        Log.i(TAG, "✅ Modo de coerción DESACTIVADO")
    }

    /**
     * Verifica si el modo de coerción está activo
     */
    fun isCoercionModeActive(): Boolean {
        return prefs.getBoolean(KEY_COERCION_MODE, false)
    }

    /**
     * Guarda la lista de apps protegidas
     */
    fun saveProtectedApps(apps: List<AppConfig>) {
        val json = gson.toJson(apps)
        prefs.edit().putString(KEY_PROTECTED_APPS, json).apply()
        Log.d(TAG, "Apps protegidas guardadas: ${apps.size} apps")
    }

    /**
     * Obtiene la lista de apps protegidas
     */
    fun getProtectedApps(): List<AppConfig> {
        val json = prefs.getString(KEY_PROTECTED_APPS, null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<AppConfig>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Error parseando apps protegidas: ${e.message}")
            emptyList()
        }
    }

    /**
     * Obtiene los package names de las apps protegidas
     */
    fun getProtectedPackageNames(): Set<String> {
        return getProtectedApps().map { it.packageName }.toSet()
    }

    /**
     * Verifica si un paquete está protegido
     */
    fun isPackageProtected(packageName: String): Boolean {
        return getProtectedPackageNames().contains(packageName)
    }

    /**
     * Resetea todo el estado (para testing o desbloqueo externo)
     */
    fun reset() {
        prefs.edit().clear().apply()
        Log.i(TAG, "Estado de coerción reseteado completamente")
    }

    /**
     * Genera un reporte del estado actual
     */
    fun generateReport(): String {
        val isActive = isCoercionModeActive()
        val apps = getProtectedApps()
        return """
            === ESTADO DE COERCIÓN ===
            Modo activo: $isActive
            Apps protegidas: ${apps.size}
            ${apps.joinToString("\n") { "  - ${it.appName} (${it.packageName})" }}
        """.trimIndent()
    }
}
