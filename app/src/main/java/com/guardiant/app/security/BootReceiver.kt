package com.guardiant.app.security

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.firebase.auth.FirebaseAuth

/**
 * Receiver que se activa cuando el dispositivo se reinicia
 * Reinicia servicios de seguridad si es necesario
 */
class BootReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "BootReceiver"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.i(TAG, "🔄 Dispositivo reiniciado")

            val auth = FirebaseAuth.getInstance()
            val currentUser = auth.currentUser

            if (currentUser != null) {
                Log.d(TAG, "Usuario autenticado detectado en boot")
                
                // Verificar si hay modo de coerción activo
                val coercionManager = CoercionStateManager.getInstance(context)
                if (coercionManager.isCoercionModeActive()) {
                    Log.w(TAG, "⚠️ Modo de coerción activo después de reinicio")
                }

                // Iniciar el servicio de lock screen
                val serviceIntent = Intent(context, LockScreenService::class.java)
                context.startService(serviceIntent)
                Log.d(TAG, "✅ LockScreenService iniciado después de reinicio")
            } else {
                Log.d(TAG, "No hay usuario autenticado en boot")
            }
        }
    }
}
