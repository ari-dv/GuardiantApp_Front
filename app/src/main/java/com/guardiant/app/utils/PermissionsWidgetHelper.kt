package com.guardiant.app.utils

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.guardiant.app.R
import com.guardiant.app.permissions.OnboardingActivity
import com.guardiant.app.permissions.PermissionManager

/**
 * Helper para integrar el widget de permisos en cualquier Fragment
 * 
 * USO EN SETTINGSFRAGMENT:
 * 
 * override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
 *     super.onViewCreated(view, savedInstanceState)
 *     
 *     // Inflar y agregar el widget
 *     val permissionWidget = PermissionsWidgetHelper.inflateWidget(
 *         layoutInflater,
 *         binding.containerSettings  // Tu contenedor principal
 *     )
 *     
 *     // Actualizar estado
 *     PermissionsWidgetHelper.updateWidget(requireContext(), permissionWidget)
 * }
 */
object PermissionsWidgetHelper {

    /**
     * Infla el widget de permisos y lo agrega al contenedor
     */
    fun inflateWidget(inflater: LayoutInflater, container: ViewGroup): View {
        val widget = inflater.inflate(R.layout.widget_permissions_status, container, false)
        container.addView(widget, 0) // Agregar al inicio
        return widget
    }

    /**
     * Actualiza el estado del widget con los permisos actuales
     */
    fun updateWidget(activity: AppCompatActivity, widget: View) {
        val permissionManager = PermissionManager(activity)
        val status = permissionManager.getAllPermissionsStatus()
        val progress = permissionManager.getPermissionsProgress()

        // Actualizar barra de progreso
        widget.findViewById<android.widget.ProgressBar>(R.id.progressBarPermissions)?.apply {
            setProgress(progress, true) // Con animación
        }

        widget.findViewById<TextView>(R.id.textViewProgressPercent)?.text = "$progress%"

        // Actualizar Device Admin
        val deviceAdminCheck = widget.findViewById<TextView>(R.id.checkDeviceAdmin)
        val deviceAdminStatus = widget.findViewById<TextView>(R.id.statusDeviceAdmin)
        if (status.deviceAdmin) {
            deviceAdminCheck?.text = "✅"
            deviceAdminStatus?.text = "Configurado correctamente"
            deviceAdminStatus?.setTextColor(activity.getColor(R.color.green))
        } else {
            deviceAdminCheck?.text = "❌"
            deviceAdminStatus?.text = "No configurado - CRÍTICO"
            deviceAdminStatus?.setTextColor(activity.getColor(R.color.red))
        }

        // Actualizar Accessibility
        val accessibilityCheck = widget.findViewById<TextView>(R.id.checkAccessibility)
        val accessibilityStatus = widget.findViewById<TextView>(R.id.statusAccessibility)
        if (status.accessibility) {
            accessibilityCheck?.text = "✅"
            accessibilityStatus?.text = "Configurado correctamente"
            accessibilityStatus?.setTextColor(activity.getColor(R.color.green))
        } else {
            accessibilityCheck?.text = "❌"
            accessibilityStatus?.text = "No configurado - CRÍTICO"
            accessibilityStatus?.setTextColor(activity.getColor(R.color.red))
        }

        // Actualizar Location
        val locationCheck = widget.findViewById<TextView>(R.id.checkLocation)
        val locationStatus = widget.findViewById<TextView>(R.id.statusLocation)
        if (status.location && status.backgroundLocation && status.locationEnabled) {
            locationCheck?.text = "✅"
            locationStatus?.text = "GPS activo y permisos otorgados"
            locationStatus?.setTextColor(activity.getColor(R.color.green))
        } else if (status.location && !status.backgroundLocation) {
            locationCheck?.text = "⚠️"
            locationStatus?.text = "Solo ubicación en primer plano"
            locationStatus?.setTextColor(activity.getColor(R.color.orange))
        } else {
            locationCheck?.text = "❌"
            locationStatus?.text = "No configurado - CRÍTICO"
            locationStatus?.setTextColor(activity.getColor(R.color.red))
        }

        // Actualizar Notifications
        val notificationsCheck = widget.findViewById<TextView>(R.id.checkNotifications)
        val notificationsStatus = widget.findViewById<TextView>(R.id.statusNotifications)
        if (status.notifications) {
            notificationsCheck?.text = "✅"
            notificationsStatus?.text = "Notificaciones activas"
            notificationsStatus?.setTextColor(activity.getColor(R.color.green))
        } else {
            notificationsCheck?.text = "❌"
            notificationsStatus?.text = "No configurado"
            notificationsStatus?.setTextColor(activity.getColor(R.color.red))
        }

        // Configurar botón
        val button = widget.findViewById<MaterialButton>(R.id.buttonConfigurePermissions)
        if (permissionManager.areAllCriticalPermissionsGranted()) {
            button?.text = "✅ Todos los permisos configurados"
            button?.isEnabled = false
        } else {
            button?.text = "⚠️ Configurar Permisos Faltantes"
            button?.isEnabled = true
            button?.setOnClickListener {
                activity.startActivity(Intent(activity, OnboardingActivity::class.java))
            }
        }
    }
}
