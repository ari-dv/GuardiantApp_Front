package com.guardiant.app.permissions

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.guardiant.app.databinding.ActivityOnboardingBinding

/**
 * Activity de Onboarding que guía al usuario paso a paso
 * para otorgar todos los permisos necesarios
 */
class OnboardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingBinding
    private lateinit var permissionManager: PermissionManager
    private lateinit var onboardingHelper: OnboardingHelper
    
    private val permissions = PermissionItem.getAllPermissions()
    private var currentStep = 0

    companion object {
        private const val TAG = "OnboardingActivity"
        private const val STATE_CURRENT_STEP = "current_step"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        Log.d(TAG, "onCreate llamado")
        
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        permissionManager = PermissionManager(this)
        onboardingHelper = OnboardingHelper(this)

        // Restaurar estado si existe
        currentStep = if (savedInstanceState != null) {
            Log.d(TAG, "Restaurando estado desde savedInstanceState")
            savedInstanceState.getInt(STATE_CURRENT_STEP, 0)
        } else {
            Log.d(TAG, "Restaurando estado desde SharedPreferences")
            onboardingHelper.getCurrentStep()
        }

        Log.d(TAG, "Paso actual: $currentStep")

        setupUI()
        showCurrentStep()
    }

    /**
     * Guardar el estado antes de que la Activity se destruya
     */
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Log.d(TAG, "Guardando estado: paso $currentStep")
        outState.putInt(STATE_CURRENT_STEP, currentStep)
        onboardingHelper.saveCurrentStep(currentStep)
    }

    /**
     * Cuando la activity vuelve a estar visible
     */
    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume - Actualizando estado de permisos")
        
        // Pequeño delay para dar tiempo al sistema de actualizar permisos
        binding.root.postDelayed({
            updateProgressBar()
            
            // Auto-verificar si estamos esperando un permiso
            val permission = permissions.getOrNull(currentStep)
            if (permission != null) {
                val isGranted = permissionManager.isPermissionGranted(permission.id)
                Log.d(TAG, "Permiso '${permission.id}' está: ${if (isGranted) "OTORGADO" else "PENDIENTE"}")
                
                if (isGranted) {
                    // Solo auto-avanzar si acabamos de otorgar el permiso
                    Toast.makeText(this, "✅ Permiso verificado automáticamente", Toast.LENGTH_SHORT).show()
                }
            }
        }, 500)
    }

    private fun setupUI() {
        // Botón principal de acción
        binding.btnPrimaryAction.setOnClickListener {
            requestCurrentPermission()
        }

        // Botón de "Más información"
        binding.btnMoreInfo.setOnClickListener {
            showDetailedInfo()
        }

        // Botón secundario (verificar/siguiente)
        binding.btnSecondaryAction.setOnClickListener {
            checkAndMoveNext()
        }
    }

    /**
     * Muestra el paso actual
     */
    private fun showCurrentStep() {
        if (currentStep >= permissions.size) {
            showCompletionScreen()
            return
        }

        val permission = permissions[currentStep]

        Log.d(TAG, "Mostrando paso ${currentStep + 1}: ${permission.title}")

        binding.apply {
            // Actualizar contenido
            tvStepIcon.text = permission.icon
            tvStepTitle.text = permission.title
            tvStepDescription.text = permission.description
            tvStepNumber.text = "Paso ${currentStep + 1} de ${permissions.size}"

            // Actualizar texto del botón según el permiso
            btnPrimaryAction.text = when (permission.id) {
                "device_admin" -> "Activar Administrador"
                "accessibility" -> "Activar Servicio"
                "location" -> "Permitir Ubicación"
                "background_location" -> "Permitir Siempre"
                "notifications" -> "Permitir Notificaciones"
                "draw_overlay" -> "Permitir Superposición"
                else -> "Activar Permiso"
            }

            // Botón secundario
            btnSecondaryAction.text = if (permission.isCritical) {
                "Ya lo activé, verificar"
            } else {
                "Omitir (opcional)"
            }

            // Actualizar información adicional en los cards
            cardInfo.visibility = View.VISIBLE
        }

        updateProgressBar()
        onboardingHelper.saveCurrentStep(currentStep)
    }

    /**
     * Solicita el permiso actual
     */
    private fun requestCurrentPermission() {
        val permission = permissions[currentStep]

        Log.d(TAG, "Solicitando permiso: ${permission.id}")

        try {
            when (permission.id) {
                "device_admin" -> {
                    permissionManager.requestDeviceAdminPermission(this)
                    showInfoToast("Activa el permiso de Administrador y presiona 'Atrás'")
                }
                "accessibility" -> {
                    permissionManager.requestAccessibilityPermission(this)
                    showInfoToast("Activa el Servicio de Accesibilidad 'Guardiant' y presiona 'Atrás'")
                }
                "location" -> {
                    permissionManager.requestLocationPermission(this)
                }
                "background_location" -> {
                    permissionManager.requestBackgroundLocationPermission(this)
                }
                "notifications" -> {
                    permissionManager.requestNotificationPermission(this)
                }
                "draw_overlay" -> {
                    permissionManager.requestDrawOverlayPermission(this)
                    showInfoToast("Activa el permiso de Superposición y presiona 'Atrás'")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error solicitando permiso: ${e.message}", e)
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Verifica el permiso actual y avanza
     */
    private fun checkAndMoveNext() {
        val permission = permissions[currentStep]
        val isGranted = permissionManager.isPermissionGranted(permission.id)

        Log.d(TAG, "Verificando permiso '${permission.id}': ${if (isGranted) "OTORGADO" else "DENEGADO"}")

        if (isGranted) {
            Toast.makeText(this, "✅ Permiso verificado", Toast.LENGTH_SHORT).show()
            moveToNextStep()
        } else {
            if (!permission.isCritical) {
                // Permitir saltar permisos opcionales
                Toast.makeText(this, "Permiso omitido", Toast.LENGTH_SHORT).show()
                moveToNextStep()
            } else {
                Toast.makeText(
                    this, 
                    "⚠️ Este permiso es necesario. Por favor actívalo primero.", 
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    /**
     * Avanza al siguiente paso
     */
    private fun moveToNextStep() {
        currentStep++
        Log.d(TAG, "Avanzando al paso: $currentStep")
        
        if (currentStep < permissions.size) {
            showCurrentStep()
        } else {
            showCompletionScreen()
        }
    }

    /**
     * Muestra información detallada del permiso
     */
    private fun showDetailedInfo() {
        val permission = permissions[currentStep]

        AlertDialog.Builder(this)
            .setTitle("¿Por qué necesitamos esto?")
            .setMessage(permission.whyNeeded)
            .setPositiveButton("Entendido", null)
            .show()
    }

    /**
     * Actualiza la barra de progreso
     */
    private fun updateProgressBar() {
        val progress = permissionManager.getPermissionsProgress()
        binding.progressBar.progress = progress
        binding.tvProgress.text = "$progress% completado"
        
        Log.d(TAG, "Progreso actualizado: $progress%")
    }

    /**
     * Muestra la pantalla de completado
     */
    private fun showCompletionScreen() {
        Log.d(TAG, "¡Onboarding completado!")
        
        Toast.makeText(this, "¡Configuración de permisos completada!", Toast.LENGTH_LONG).show()
        
        // Marcar onboarding como completado
        onboardingHelper.markOnboardingCompleted()
        
        // Iniciar servicio de lock screen
        val serviceIntent = Intent(this, com.guardiant.app.security.LockScreenService::class.java)
        startService(serviceIntent)
        
        // NAVEGAR A HOME (no solo finish())
        val intent = Intent(this, com.guardiant.app.main.HomeActivity::class.java)
        startActivity(intent)
        finishAffinity() // Cierra todas las actividades anteriores (registro, verificación, setup, etc.)
    }

    /**
     * Muestra un Toast informativo
     */
    private fun showInfoToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    /**
     * Maneja el resultado de solicitudes de permisos
     */
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        Log.d(TAG, "onActivityResult - requestCode: $requestCode, resultCode: $resultCode")

        when (requestCode) {
            PermissionManager.REQUEST_DEVICE_ADMIN,
            PermissionManager.REQUEST_ACCESSIBILITY,
            PermissionManager.REQUEST_DRAW_OVERLAY -> {
                // Esperar un poco para que el sistema actualice los permisos
                binding.root.postDelayed({
                    val permission = permissions.getOrNull(currentStep)
                    if (permission != null) {
                        val isGranted = permissionManager.isPermissionGranted(permission.id)
                        if (isGranted) {
                            Toast.makeText(this, "Permiso otorgado", Toast.LENGTH_SHORT).show()
                            moveToNextStep()
                        } else {
                            Toast.makeText(this, "Permiso no otorgado. Intenta de nuevo.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }, 800)
            }
        }
    }

    /**
     * Maneja el resultado de permisos en tiempo de ejecución
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        Log.d(TAG, "onRequestPermissionsResult - requestCode: $requestCode")

        when (requestCode) {
            PermissionManager.REQUEST_LOCATION,
            PermissionManager.REQUEST_BACKGROUND_LOCATION,
            PermissionManager.REQUEST_NOTIFICATION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "✅ Permiso otorgado", Toast.LENGTH_SHORT).show()
                    moveToNextStep()
                } else {
                    Toast.makeText(this, "❌ Permiso denegado", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /**
     * Confirmar antes de salir
     */
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        AlertDialog.Builder(this)
            .setTitle("¿Salir de la configuración?")
            .setMessage("Si sales ahora, algunos permisos no estarán configurados y tendrás que volver a hacer login.")
            .setPositiveButton("Continuar configuración", null)
            .setNegativeButton("Salir") { _, _ ->
                super.onBackPressed()
            }
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy llamado")
    }
}