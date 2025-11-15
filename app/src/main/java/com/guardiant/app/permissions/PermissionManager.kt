package com.guardiant.app.permissions

import android.Manifest
import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.guardiant.app.security.DeviceAdminReceiver

/**
 * Data class para el estado de todos los permisos
 */
data class PermissionsStatus(
    val deviceAdmin: Boolean,
    val accessibility: Boolean,
    val location: Boolean,
    val backgroundLocation: Boolean,
    val locationEnabled: Boolean,
    val notifications: Boolean,
    val drawOverlay: Boolean
) {
    fun getCriticalMissingPermissions(): List<String> {
        val missing = mutableListOf<String>()

        if (!deviceAdmin) missing.add("Administrador de Dispositivo")
        if (!accessibility) missing.add("Servicio de Accesibilidad")
        if (!location) missing.add("Ubicación GPS")
        if (!backgroundLocation) missing.add("Ubicación en Segundo Plano")
        if (!notifications) missing.add("Notificaciones")

        return missing
    }

    fun getProgress(): Int {
        val total = 7
        var granted = 0

        if (deviceAdmin) granted++
        if (accessibility) granted++
        if (location) granted++
        if (backgroundLocation) granted++
        if (locationEnabled) granted++
        if (notifications) granted++
        if (drawOverlay) granted++

        return ((granted.toFloat() / total) * 100).toInt()
    }
}

class PermissionManager(private val context: Context) {

    private val devicePolicyManager = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    private val adminComponent = ComponentName(context, DeviceAdminReceiver::class.java)
    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    companion object {
        const val REQUEST_DEVICE_ADMIN = 1001
        const val REQUEST_ACCESSIBILITY = 1002
        const val REQUEST_LOCATION = 1003
        const val REQUEST_BACKGROUND_LOCATION = 1004
        const val REQUEST_NOTIFICATION = 1005
        const val REQUEST_DRAW_OVERLAY = 1006
    }

    /**
     * Verifica si un permiso específico está otorgado
     */
    fun isPermissionGranted(permissionId: String): Boolean {
        return when (permissionId) {
            "device_admin" -> isDeviceAdminActive()
            "accessibility" -> isAccessibilityServiceEnabled()
            "location" -> isLocationPermissionGranted()
            "background_location" -> isBackgroundLocationPermissionGranted()
            "notifications" -> isNotificationPermissionGranted()
            "draw_overlay" -> isDrawOverlayPermissionGranted()
            else -> false
        }
    }

    /**
     * Obtiene el estado completo de todos los permisos
     */
    fun getAllPermissionsStatus(): PermissionsStatus {
        return PermissionsStatus(
            deviceAdmin = isDeviceAdminActive(),
            accessibility = isAccessibilityServiceEnabled(),
            location = isLocationPermissionGranted(),
            backgroundLocation = isBackgroundLocationPermissionGranted(),
            locationEnabled = isLocationEnabled(),
            notifications = isNotificationPermissionGranted(),
            drawOverlay = isDrawOverlayPermissionGranted()
        )
    }

    /**
     * Verifica si Device Admin está activo
     */
    private fun isDeviceAdminActive(): Boolean {
        return devicePolicyManager.isAdminActive(adminComponent)
    }

    /**
     * Verifica si el Servicio de Accesibilidad está habilitado
     */
    private fun isAccessibilityServiceEnabled(): Boolean {
        val serviceName = "${context.packageName}/com.guardiant.app.security.GuardiantAccessibilityService"

        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false

        return enabledServices.contains(serviceName)
    }

    /**
     * Verifica si el permiso de ubicación está otorgado
     */
    private fun isLocationPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Verifica si el permiso de ubicación en segundo plano está otorgado
     */
    private fun isBackgroundLocationPermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // No existe en versiones anteriores a Android 10
        }
    }

    /**
     * Verifica si el GPS está activado
     */
    private fun isLocationEnabled(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            locationManager.isLocationEnabled
        } else {
            @Suppress("DEPRECATION")
            locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        }
    }

    /**
     * Verifica si el permiso de notificaciones está otorgado
     */
    private fun isNotificationPermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Permitido por defecto en versiones anteriores a Android 13
        }
    }

    /**
     * Verifica si el permiso de superposición está otorgado
     */
    private fun isDrawOverlayPermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else {
            true
        }
    }

    /**
     * Solicita el permiso de Device Admin
     */
    fun requestDeviceAdminPermission(activity: Activity) {
        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
            putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent)
            putExtra(
                DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                "Guardiant necesita ser administrador del dispositivo para protegerlo en caso de robo."
            )
        }
        activity.startActivityForResult(intent, REQUEST_DEVICE_ADMIN)
    }

    /**
     * Solicita el permiso de Accesibilidad
     */
    fun requestAccessibilityPermission(activity: Activity) {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        activity.startActivityForResult(intent, REQUEST_ACCESSIBILITY)
    }

    /**
     * Solicita el permiso de Ubicación
     */
    fun requestLocationPermission(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION
            )
        }
    }

    /**
     * Solicita el permiso de Ubicación en Segundo Plano
     */
    fun requestBackgroundLocationPermission(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                REQUEST_BACKGROUND_LOCATION
            )
        }
    }

    /**
     * Solicita el permiso de Notificaciones
     */
    fun requestNotificationPermission(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                REQUEST_NOTIFICATION
            )
        }
    }

    /**
     * Solicita el permiso de Superposición
     */
    fun requestDrawOverlayPermission(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${context.packageName}")
            )
            activity.startActivityForResult(intent, REQUEST_DRAW_OVERLAY)
        }
    }

    /**
     * Obtiene el progreso de permisos otorgados (0-100)
     */
    fun getPermissionsProgress(): Int {
        return getAllPermissionsStatus().getProgress()
    }

    /**
     * Verifica si todos los permisos críticos están otorgados
     */
    fun areAllCriticalPermissionsGranted(): Boolean {
        val status = getAllPermissionsStatus()
        return status.deviceAdmin &&
                status.accessibility &&
                status.location &&
                status.notifications
    }

    /**
     * Obtiene la cantidad de permisos otorgados
     */
    fun getGrantedPermissionsCount(): Int {
        return PermissionItem.getAllPermissions().count { isPermissionGranted(it.id) }
    }

    /**
     * Obtiene la cantidad total de permisos
     */
    fun getTotalPermissionsCount(): Int {
        return PermissionItem.getAllPermissions().size
    }
}