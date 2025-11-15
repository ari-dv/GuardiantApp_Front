package com.guardiant.app.security

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent

/**
 * Servicio de Accesibilidad de Guardiant
 * Detecta intentos de desinstalación y otras acciones críticas
 * En modo de coerción, bloquea silenciosamente apps protegidas
 */
class GuardiantAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "GuardiantAccService"
        private const val GUARDIANT_PACKAGE = "com.guardiant.app"

        // Paquetes críticos a monitorear
        private val CRITICAL_PACKAGES = setOf(
            "com.android.settings",
            "com.google.android.packageinstaller",
            "com.android.packageinstaller"
        )
    }

    private lateinit var coercionManager: CoercionStateManager

    override fun onServiceConnected() {
        super.onServiceConnected()

        Log.i(TAG, "Servicio de Accesibilidad conectado")

        // Inicializar CoercionStateManager
        coercionManager = CoercionStateManager.getInstance(this)

        // Configurar el servicio
        val info = AccessibilityServiceInfo().apply {
            // Tipos de eventos que queremos capturar
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or
                    AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED or
                    AccessibilityEvent.TYPE_VIEW_CLICKED

            // Tipos de feedback
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC

            // Flags
            flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or
                    AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS

            // Delay de notificación
            notificationTimeout = 100

            // Paquetes a monitorear (todas las apps)
            packageNames = null // null = todas las apps
        }

        serviceInfo = info

        Log.d(TAG, "Configuración del servicio completada")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        try {
            val packageName = event.packageName?.toString() ?: return

            // PRIORIDAD 1: Verificar modo de coerción y bloquear apps
            if (coercionManager.isCoercionModeActive()) {
                handleCoercionMode(event, packageName)
                return // No procesar otros eventos en modo coerción
            }

            // Log de eventos (solo para debugging)
            if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
                Log.d(TAG, "Window cambiada: $packageName")
            }

            // Detectar si se está intentando desinstalar nuestra app
            if (isUninstallAttempt(event, packageName)) {
                handleUninstallAttempt(event)
            }

            // Detectar acceso a configuración de administrador de dispositivos
            if (isDeviceAdminSettings(event, packageName)) {
                handleDeviceAdminAccess(event)
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error procesando evento: ${e.message}", e)
        }
    }

    /**
     * Maneja el modo de coerción: bloquea apps protegidas silenciosamente
     */
    private fun handleCoercionMode(event: AccessibilityEvent, packageName: String) {
        // Solo actuar en cambios de ventana (cuando se abre una app)
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            return
        }

        // Bloquear la propia app de Guardiant
        if (packageName == GUARDIANT_PACKAGE) {
            Log.w(TAG, "🚫 Bloqueando acceso a Guardiant (modo coerción)")
            blockCurrentApp()
            return
        }

        // Bloquear apps protegidas
        if (coercionManager.isPackageProtected(packageName)) {
            Log.w(TAG, "🚫 Bloqueando app protegida: $packageName")
            blockCurrentApp()
        }
    }

    /**
     * Bloquea la app actual retornando al home launcher (silenciosamente)
     */
    private fun blockCurrentApp() {
        try {
            performGlobalAction(GLOBAL_ACTION_HOME)
        } catch (e: Exception) {
            Log.e(TAG, "Error bloqueando app: ${e.message}", e)
        }
    }

    /**
     * Detecta intentos de desinstalación
     */
    private fun isUninstallAttempt(event: AccessibilityEvent, packageName: String): Boolean {
        if (!CRITICAL_PACKAGES.contains(packageName)) return false

        val className = event.className?.toString() ?: return false

        // Detectar pantallas de desinstalación
        return className.contains("UninstallAppProgress") ||
                className.contains("UninstallAlertActivity") ||
                event.text?.any { it.toString().contains("desinstalar", ignoreCase = true) } == true
    }

    /**
     * Maneja intentos de desinstalación
     */
    private fun handleUninstallAttempt(event: AccessibilityEvent) {
        Log.w(TAG, "Intento de desinstalación detectado!")

        // TODO: Implementar acciones de seguridad
        // - Enviar alerta al servidor
        // - Tomar foto con cámara frontal
        // - Registrar ubicación
        // - Notificar al usuario legítimo

        // Por ahora, solo registrar
        Log.w(TAG, "Alerta: Intento de desinstalación registrado")
    }

    /**
     * Detecta acceso a configuración de administrador de dispositivos
     */
    private fun isDeviceAdminSettings(event: AccessibilityEvent, packageName: String): Boolean {
        if (packageName != "com.android.settings") return false

        val className = event.className?.toString() ?: return false

        return className.contains("DeviceAdminSettings") ||
                className.contains("DeviceAdminAdd")
    }

    /**
     * Maneja acceso a configuración de Device Admin
     */
    private fun handleDeviceAdminAccess(event: AccessibilityEvent) {
        Log.i(TAG, "ℹ️ Usuario accediendo a configuración de administrador")

        // TODO: Implementar monitoreo
        // - Verificar si se está desactivando el admin
        // - Alertar si es necesario
    }

    override fun onInterrupt() {
        Log.w(TAG, "Servicio interrumpido")
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.w(TAG, "Servicio desvinculado")
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        Log.e(TAG, "Servicio destruido")
        super.onDestroy()
    }
}