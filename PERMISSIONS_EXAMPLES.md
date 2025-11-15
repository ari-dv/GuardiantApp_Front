# 📚 Ejemplos de Uso - Sistema de Permisos

## 1. Integrar en SettingsFragment

```kotlin
// En SettingsFragment.kt
package com.guardiant.app.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.guardiant.app.databinding.FragmentSettingsBinding
import com.guardiant.app.utils.PermissionsWidgetHelper

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var permissionWidget: View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Agregar widget de permisos al inicio
        permissionWidget = PermissionsWidgetHelper.inflateWidget(
            layoutInflater,
            binding.containerSettings  // Tu LinearLayout o contenedor principal
        )

        // Actualizar estado inicial
        updatePermissionsWidget()
    }

    override fun onResume() {
        super.onResume()
        // Re-actualizar cuando el usuario vuelve (por si activó permisos)
        updatePermissionsWidget()
    }

    private fun updatePermissionsWidget() {
        PermissionsWidgetHelper.updateWidget(
            requireActivity() as AppCompatActivity,
            permissionWidget
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
```

---

## 2. Verificar Permisos en HomeActivity

```kotlin
// En HomeActivity.kt
package com.guardiant.app.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.guardiant.app.databinding.ActivityHomeBinding
import com.guardiant.app.permissions.PermissionManager
import com.guardiant.app.permissions.PermissionUtils

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var permissionManager: PermissionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        permissionManager = PermissionManager(this)

        // Verificar permisos al inicio
        checkCriticalPermissions()

        // Setup navigation, etc...
        setupNavigation()
    }

    override fun onResume() {
        super.onResume()
        // Re-verificar permisos cuando el usuario vuelve
        checkCriticalPermissions()
    }

    private fun checkCriticalPermissions() {
        if (!permissionManager.areAllCriticalPermissionsGranted()) {
            // Mostrar diálogo informativo
            PermissionUtils.checkAndShowOnboardingIfNeeded(
                this,
                permissionManager,
                onAllGranted = {
                    // Todos los permisos OK, continuar normalmente
                    Toast.makeText(this, "✅ Dispositivo protegido", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    private fun setupNavigation() {
        // Tu código de navegación...
    }
}
```

---

## 3. Forzar Onboarding después del Registro

```kotlin
// En VerificationActivity.kt
package com.guardiant.app.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.guardiant.app.permissions.OnboardingActivity

class VerificationActivity : AppCompatActivity() {

    // ... tu código existente ...

    private fun onVerificationSuccess() {
        // Después de verificar SMS exitosamente
        Toast.makeText(this, "✅ Verificación exitosa", Toast.LENGTH_SHORT).show()

        // Ir directamente al onboarding de permisos
        val intent = Intent(this, OnboardingActivity::class.java)
        startActivity(intent)
        finishAffinity() // Cierra todas las activities anteriores
    }
}
```

---

## 4. Mostrar Estado de Permisos en Logs

```kotlin
// En cualquier Activity o Fragment
import android.util.Log
import com.guardiant.app.permissions.PermissionManager
import com.guardiant.app.permissions.PermissionUtils

fun debugPermissions() {
    val permissionManager = PermissionManager(this)
    val report = PermissionUtils.generatePermissionsReport(permissionManager)
    
    Log.d("Permissions", report)
    
    // Salida:
    // === REPORTE DE PERMISOS ===
    // 
    // Progreso general: 83%
    // 
    // 🛡️ Device Admin: ✅ Otorgado
    // 👁️ Accessibility: ✅ Otorgado
    // 📍 Location: ✅ Otorgado
    // 🌐 Background Location: ✅ Otorgado
    // 📡 GPS Enabled: ✅ Activado
    // 🔔 Notifications: ❌ Faltante
    // 🔝 Draw Overlay: ❌ Faltante
    // 
    // ¿Puede funcionar la app?: SÍ
    // ¿Todos los permisos críticos?: NO
}
```

---

## 5. Crear Recordatorio Periódico de Permisos

```kotlin
// En HomeActivity.kt o algún WorkManager
import com.guardiant.app.permissions.OnboardingHelper
import com.guardiant.app.permissions.PermissionManager

fun checkAndRemindPermissions() {
    val onboardingHelper = OnboardingHelper(this)
    val permissionManager = PermissionManager(this)

    // Si han pasado 7 días desde la última verificación
    if (onboardingHelper.shouldRemindPermissions(7)) {
        if (!permissionManager.areAllCriticalPermissionsGranted()) {
            // Mostrar notificación o diálogo
            showPermissionReminder()
        }
    }
}

private fun showPermissionReminder() {
    AlertDialog.Builder(this)
        .setTitle("⚠️ Permisos Incompletos")
        .setMessage("Hace tiempo que no verificamos los permisos de seguridad. " +
                    "¿Quieres configurarlos ahora para proteger tu dispositivo?")
        .setPositiveButton("Sí, configurar") { _, _ ->
            startActivity(Intent(this, OnboardingActivity::class.java))
        }
        .setNegativeButton("Recordarme en 7 días", null)
        .show()
}
```

---

## 6. Verificar Permisos Específicos

```kotlin
// Verificar solo Device Admin
val permissionManager = PermissionManager(this)

if (permissionManager.isDeviceAdminEnabled()) {
    Log.d("Permissions", "✅ Device Admin activado")
} else {
    Log.d("Permissions", "❌ Device Admin NO activado")
    // Solicitar específicamente
    permissionManager.requestDeviceAdminPermission(this)
}

// Verificar solo Accessibility
if (permissionManager.isAccessibilityServiceEnabled()) {
    Log.d("Permissions", "✅ Accessibility activado")
} else {
    Log.d("Permissions", "❌ Accessibility NO activado")
    permissionManager.requestAccessibilityPermission(this)
}

// Verificar solo Location
if (permissionManager.isLocationPermissionGranted()) {
    if (permissionManager.isLocationEnabled()) {
        Log.d("Permissions", "✅ Location activado y GPS encendido")
    } else {
        Log.d("Permissions", "⚠️ Permiso OK pero GPS apagado")
        permissionManager.openLocationSettings(this)
    }
} else {
    Log.d("Permissions", "❌ Location NO otorgado")
    permissionManager.requestLocationPermission(this)
}
```

---

## 7. Mostrar Diálogo de Explicación Antes de Solicitar

```kotlin
import com.guardiant.app.permissions.PermissionItem
import com.guardiant.app.permissions.PermissionUtils

fun requestDeviceAdminWithExplanation() {
    val permission = PermissionItem.getPermissionById("device_admin")!!
    
    PermissionUtils.showPermissionRationaleDialog(
        context = this,
        permission = permission,
        onAccept = {
            // Usuario entendió, proceder
            permissionManager.requestDeviceAdminPermission(this)
        },
        onCancel = {
            // Usuario canceló
            Toast.makeText(this, "Permiso necesario para proteger tu dispositivo", Toast.LENGTH_LONG).show()
        }
    )
}
```

---

## 8. Manejo de Resultados de Permisos

```kotlin
class MyActivity : AppCompatActivity() {

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            PermissionManager.REQUEST_DEVICE_ADMIN -> {
                if (permissionManager.isDeviceAdminEnabled()) {
                    PermissionUtils.showPermissionStatus(this, "Device Admin", true)
                } else {
                    PermissionUtils.showPermissionStatus(this, "Device Admin", false)
                }
            }
            
            PermissionManager.REQUEST_ACCESSIBILITY -> {
                if (permissionManager.isAccessibilityServiceEnabled()) {
                    PermissionUtils.showPermissionStatus(this, "Accessibility", true)
                } else {
                    PermissionUtils.showPermissionStatus(this, "Accessibility", false)
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            PermissionManager.REQUEST_LOCATION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "✅ Ubicación otorgada", Toast.LENGTH_SHORT).show()
                    // Ahora solicitar ubicación en segundo plano
                    permissionManager.requestBackgroundLocationPermission(this)
                } else {
                    Toast.makeText(this, "❌ Ubicación denegada", Toast.LENGTH_SHORT).show()
                }
            }
            
            PermissionManager.REQUEST_NOTIFICATION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "✅ Notificaciones activadas", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
```

---

## 9. Resetear Onboarding (Para Testing)

```kotlin
// Útil durante desarrollo para probar el flujo nuevamente
import com.guardiant.app.permissions.OnboardingHelper

fun resetOnboardingForTesting() {
    val helper = OnboardingHelper(this)
    helper.resetOnboarding()
    
    Toast.makeText(this, "Onboarding reseteado. Vuelve a abrir la app.", Toast.LENGTH_LONG).show()
    
    // Ir al onboarding
    startActivity(Intent(this, OnboardingActivity::class.java))
    finish()
}
```

---

## 10. Verificar si la App Puede Funcionar

```kotlin
import com.guardiant.app.permissions.PermissionUtils

fun canProceedWithApp(): Boolean {
    val permissionManager = PermissionManager(this)
    
    if (!PermissionUtils.canAppFunction(permissionManager)) {
        // App NO puede funcionar, faltan permisos críticos
        AlertDialog.Builder(this)
            .setTitle("⚠️ Configuración Necesaria")
            .setMessage("Guardiant necesita permisos críticos para funcionar. " +
                        "Sin ellos, no podremos proteger tu dispositivo.")
            .setPositiveButton("Configurar ahora") { _, _ ->
                startActivity(Intent(this, OnboardingActivity::class.java))
            }
            .setNegativeButton("Salir") { _, _ ->
                finish()
            }
            .setCancelable(false)
            .show()
        
        return false
    }
    
    return true
}
```

---

## 11. Actualizar Widget Manualmente

```kotlin
// Si agregaste el widget en fragment_settings.xml
// Puedes actualizarlo manualmente cuando sea necesario

class SettingsFragment : Fragment() {
    
    private lateinit var permissionWidget: View
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Obtener referencia al widget incluido en el XML
        permissionWidget = binding.includePermissionsWidget.root
        
        // Actualizar
        updateWidget()
        
        // Agregar botón para refrescar manualmente
        binding.buttonRefreshPermissions.setOnClickListener {
            updateWidget()
            Toast.makeText(requireContext(), "🔄 Permisos actualizados", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun updateWidget() {
        PermissionsWidgetHelper.updateWidget(
            requireActivity() as AppCompatActivity,
            permissionWidget
        )
    }
}
```

---

## 12. Integrar con Firebase Analytics

```kotlin
import com.google.firebase.analytics.FirebaseAnalytics

fun logPermissionEvent(permissionName: String, granted: Boolean) {
    val analytics = FirebaseAnalytics.getInstance(this)
    
    val bundle = Bundle().apply {
        putString("permission_name", permissionName)
        putBoolean("granted", granted)
    }
    
    analytics.logEvent("permission_status", bundle)
}

// Uso:
val permissionManager = PermissionManager(this)
val status = permissionManager.getAllPermissionsStatus()

logPermissionEvent("device_admin", status.deviceAdmin)
logPermissionEvent("accessibility", status.accessibility)
logPermissionEvent("location", status.location)
logPermissionEvent("notifications", status.notifications)
```

---

## 📝 Notas Importantes

1. **Siempre verifica permisos en `onResume()`**
   - El usuario puede desactivarlos en cualquier momento desde Configuración

2. **Usa diálogos explicativos**
   - Antes de solicitar, explica el "por qué"
   - Mejora significativamente la tasa de conversión

3. **No forces al usuario**
   - Permite continuar con funcionalidad limitada
   - Recuerda periódicamente si faltan permisos críticos

4. **Testing**
   - Prueba en dispositivos físicos, no solo emuladores
   - Algunos permisos se comportan diferente en emuladores

5. **Logs**
   - Mantén logs detallados durante desarrollo
   - Ayudan a debuggear problemas de permisos

---

## ✅ Checklist de Integración

- [ ] Agregar widget en SettingsFragment
- [ ] Verificar permisos en HomeActivity.onResume()
- [ ] Integrar onboarding después de VerificationActivity
- [ ] Implementar recordatorios periódicos
- [ ] Agregar logs de analytics
- [ ] Probar en dispositivo físico
- [ ] Documentar para el equipo

---

¡Con estos ejemplos deberías poder integrar el sistema de permisos en toda la app! 🚀
