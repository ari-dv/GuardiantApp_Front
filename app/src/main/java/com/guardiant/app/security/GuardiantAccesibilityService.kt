/*
package com.guardiant.app.security

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.util.Log
import android.content.Intent
import com.guardiant.app.auth.UnlockActivity

class GuardiantAccessibilityService : AccessibilityService() {

    private val TAG = "GuardiantAccessibilityService"

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Detecta cuando el usuario intenta desbloquear o cuando hay cambios de ventana
        if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            Log.d(TAG, "Cambio de ventana detectado: ${event.packageName}")
            
            // Aquí puedes lanzar tu pantalla de PIN si lo necesitas
            // Por ahora solo monitoreamos
        }
    }

    override fun onInterrupt() {
        Log.d(TAG, "Servicio de Accesibilidad interrumpido")
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d(TAG, "✅ Servicio de Accesibilidad conectado")
    }
}
*/