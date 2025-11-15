package com.guardiant.app.security

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

/**
 * Receiver para manejar eventos de Device Admin
 */
class DeviceAdminReceiver : DeviceAdminReceiver() {

    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
        Toast.makeText(
            context,
            "✅ Guardiant Device Admin activado",
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onDisabled(context: Context, intent: Intent) {
        super.onDisabled(context, intent)
        Toast.makeText(
            context,
            "⚠️ Guardiant Device Admin desactivado",
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onPasswordChanged(context: Context, intent: Intent) {
        super.onPasswordChanged(context, intent)
        // Manejar cambios de contraseña
    }

    override fun onPasswordFailed(context: Context, intent: Intent) {
        super.onPasswordFailed(context, intent)
        // Manejar intentos fallidos de contraseña
    }

    override fun onPasswordSucceeded(context: Context, intent: Intent) {
        super.onPasswordSucceeded(context, intent)
        // Manejar desbloqueos exitosos
    }

    override fun onPasswordExpiring(context: Context, intent: Intent) {
        super.onPasswordExpiring(context, intent)
        // Manejar expiración de contraseña
    }
}