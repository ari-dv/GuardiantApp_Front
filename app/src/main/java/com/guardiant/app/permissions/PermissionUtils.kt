package com.guardiant.app.permissions

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.appcompat.app.AlertDialog

/**
 * Utilidades y funciones helper para trabajar con permisos
 * Métodos estáticos para uso en cualquier parte de la app
 */
object PermissionUtils {

    /**
     * Muestra un diálogo explicando por qué se necesita un permiso
     */
    fun showPermissionRationaleDialog(
        context: Context,
        permission: PermissionItem,
        onAccept: () -> Unit,
        onCancel: (() -> Unit)? = null
    ) {
        AlertDialog.Builder(context)
            .setTitle("${permission.icon} ${permission.title}")
            .setMessage("${permission.longDescription}\n\n📝 ${permission.whyNeeded}")
            .setPositiveButton("Entendido, continuar") { dialog, _ ->
                dialog.dismiss()
                onAccept()
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
                onCancel?.invoke()
            }
            .setCancelable(false)
            .show()
    }

    /**
     * Muestra un resumen de todos los permisos faltantes
     */
    fun showMissingPermissionsDialog(
        context: Context,
        permissionManager: PermissionManager,
        onGoToSettings: () -> Unit
    ) {
        val status = permissionManager.getAllPermissionsStatus()
        val missing = status.getCriticalMissingPermissions()

        if (missing.isEmpty()) {
            Toast.makeText(context, "✅ Todos los permisos otorgados", Toast.LENGTH_SHORT).show()
            return
        }

        val message = buildString {
            append("⚠️ Permisos críticos faltantes:\n\n")
            missing.forEach { perm ->
                append("• $perm\n")
            }
            append("\nGuardiant no podrá proteger completamente tu dispositivo sin estos permisos.")
        }

        AlertDialog.Builder(context)
            .setTitle("Configuración Incompleta")
            .setMessage(message)
            .setPositiveButton("Configurar ahora") { dialog, _ ->
                dialog.dismiss()
                onGoToSettings()
            }
            .setNegativeButton("Más tarde") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    /**
     * Verifica si la app puede funcionar con los permisos actuales
     */
    fun canAppFunction(permissionManager: PermissionManager): Boolean {
        val status = permissionManager.getAllPermissionsStatus()
        
        // Mínimo necesario para funcionar:
        // - Device Admin (crítico)
        // - Location (crítico)
        return status.deviceAdmin && status.location
    }

    /**
     * Muestra toast con el estado de un permiso
     */
    fun showPermissionStatus(context: Context, permissionName: String, isGranted: Boolean) {
        val emoji = if (isGranted) "✅" else "❌"
        val status = if (isGranted) "otorgado" else "denegado"
        Toast.makeText(context, "$emoji $permissionName $status", Toast.LENGTH_SHORT).show()
    }

    /**
     * Crea un Intent para abrir OnboardingActivity
     */
    fun createOnboardingIntent(context: Context, startFromStep: Int = 0): Intent {
        return Intent(context, OnboardingActivity::class.java).apply {
            putExtra("start_step", startFromStep)
        }
    }

    /**
     * Verifica permisos y muestra onboarding si es necesario
     * Útil para llamar desde HomeActivity o SettingsFragment
     */
    fun checkAndShowOnboardingIfNeeded(
        activity: Activity,
        permissionManager: PermissionManager,
        onAllGranted: (() -> Unit)? = null
    ) {
        if (permissionManager.areAllCriticalPermissionsGranted()) {
            // Todos los permisos otorgados
            onAllGranted?.invoke()
        } else {
            // Faltan permisos, mostrar onboarding
            showMissingPermissionsDialog(activity, permissionManager) {
                activity.startActivity(createOnboardingIntent(activity))
            }
        }
    }

    /**
     * Obtiene un texto descriptivo del progreso
     */
    fun getProgressText(progress: Int): String {
        return when {
            progress == 0 -> "No has configurado ningún permiso"
            progress < 30 -> "Configuración inicial: $progress%"
            progress < 60 -> "Vas bien: $progress% completado"
            progress < 90 -> "¡Casi listo!: $progress%"
            progress < 100 -> "Solo falta un poco: $progress%"
            else -> "¡Configuración completa!"
        }
    }

    /**
     * Obtiene el color de la barra de progreso según el porcentaje
     */
    fun getProgressColor(context: Context, progress: Int): Int {
        return when {
            progress < 30 -> context.getColor(android.R.color.holo_red_light)
            progress < 60 -> context.getColor(android.R.color.holo_orange_light)
            progress < 90 -> context.getColor(android.R.color.holo_blue_light)
            else -> context.getColor(android.R.color.holo_green_light)
        }
    }

    /**
     * Genera reporte detallado del estado de permisos (para debugging)
     */
    fun generatePermissionsReport(permissionManager: PermissionManager): String {
        val status = permissionManager.getAllPermissionsStatus()
        val progress = permissionManager.getPermissionsProgress()

        return buildString {
            appendLine("=== REPORTE DE PERMISOS ===")
            appendLine()
            appendLine("Progreso general: $progress%")
            appendLine()
            appendLine("🛡️ Device Admin: ${if (status.deviceAdmin) "✅ Otorgado" else "❌ Faltante"}")
            appendLine("👁️ Accessibility: ${if (status.accessibility) "✅ Otorgado" else "❌ Faltante"}")
            appendLine("📍 Location: ${if (status.location) "✅ Otorgado" else "❌ Faltante"}")
            appendLine("🌐 Background Location: ${if (status.backgroundLocation) "✅ Otorgado" else "❌ Faltante"}")
            appendLine("📡 GPS Enabled: ${if (status.locationEnabled) "✅ Activado" else "❌ Desactivado"}")
            appendLine("🔔 Notifications: ${if (status.notifications) "✅ Otorgado" else "❌ Faltante"}")
            appendLine("🔝 Draw Overlay: ${if (status.drawOverlay) "✅ Otorgado" else "❌ Faltante"}")
            appendLine()
            appendLine("¿Puede funcionar la app?: ${if (canAppFunction(permissionManager)) "SÍ" else "NO"}")
            appendLine("¿Todos los permisos críticos?: ${if (permissionManager.areAllCriticalPermissionsGranted()) "SÍ" else "NO"}")
        }
    }
}
