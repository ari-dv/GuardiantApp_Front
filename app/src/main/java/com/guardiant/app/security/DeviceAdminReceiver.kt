/*
package com.guardiant.app.security

import android.app.admin.DeviceAdminReceiver
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log

class DeviceAdminReceiver : DeviceAdminReceiver() {
    
    companion object {
        private const val TAG = "DeviceAdminReceiver"
        
        fun lockDevice(context: Context) {
            try {
                val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
                val adminComponent = ComponentName(context, DeviceAdminReceiver::class.java)
                
                if (dpm.isAdminActive(adminComponent)) {
                    dpm.lockNow()
                    Log.d(TAG, "✓ Dispositivo bloqueado correctamente")
                } else {
                    Log.e(TAG, "❌ Device Admin no está activo")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error bloqueando dispositivo: ${e.message}")
            }
        }

        fun isAdminActive(context: Context): Boolean {
            val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            val adminComponent = ComponentName(context, DeviceAdminReceiver::class.java)
            return dpm.isAdminActive(adminComponent)
        }

        fun wipeData(context: Context) {
            try {
                val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
                val adminComponent = ComponentName(context, DeviceAdminReceiver::class.java)
                
                if (dpm.isAdminActive(adminComponent)) {
                    dpm.wipeData(0)
                    Log.d(TAG, "🔥 Datos borrados correctamente")
                } else {
                    Log.e(TAG, "❌ Device Admin no está activo")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error borrando datos: ${e.message}")
            }
        }
    }

    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
        Log.d(TAG, "✅ Device Admin habilitado")
    }

    override fun onDisabled(context: Context, intent: Intent) {
        super.onDisabled(context, intent)
        Log.d(TAG, "❌ Device Admin deshabilitado")
    }
}
*/