package com.guardiant.app.main

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.guardiant.app.R
import com.guardiant.app.databinding.ActivityHomeBinding
import com.guardiant.app.security.LocationTrackingService
import com.guardiant.app.security.SecurityViewModel
import com.guardiant.app.security.SensorService
// import com.guardiant.app.security.DeviceAdminReceiver // TODO: Descomentar cuando tengas permisos

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var securityViewModel: SecurityViewModel
    private var sensorService: SensorService? = null
    private var locationTrackingService: LocationTrackingService? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializar ViewModel de seguridad
        securityViewModel = ViewModelProvider(this).get(SecurityViewModel::class.java)

        // 1. Configurar navegación
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        binding.bottomNavigationView.setupWithNavController(navController)

        // 2. ⭐ INICIAR MONITOREO DE SEGURIDAD
        securityViewModel.startMonitoring(this)

        // 3. Conectar servicios con ViewModel
        setupServiceListeners()

        // 4. Observar cambios de seguridad
        setupSecurityObservers()

        Log.d("HomeActivity", "✓ App iniciada con monitoreo de seguridad activo")
    }

    /**
     * Conectar los servicios (Sensor + Location) con el ViewModel
     */
    private fun setupServiceListeners() {
        // Acceder a los servicios en background
        val sensorService = Intent(this, SensorService::class.java)
        val locationService = Intent(this, LocationTrackingService::class.java)

        // Nota: En Android, acceder a servicios en background es complejo.
        // En su lugar, usamos LiveData observers.
        // Los servicios publicarán eventos en LiveData que observaremos.

        // Para esta implementación simple, los callbacks se invocan
        // directamente desde los servicios cuando detectan anomalías.

        Log.d("HomeActivity", "✓ Service listeners configurados")
    }

    private fun setupSecurityObservers() {
        // Observar si debe bloquearse
        securityViewModel.lockDevice.observe(this) { shouldLock ->
            if (shouldLock) {
                Log.e("HomeActivity", "🔒 EJECUTANDO BLOQUEO DE EMERGENCIA")
                lockDeviceNow()
            }
        }

        // Observar alertas
        securityViewModel.alertMessage.observe(this) { message ->
            if (message != null) {
                Log.w("HomeActivity", "⚠️ ALERTA: $message")
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            }
        }

        // Observar ubicación sospechosa
        securityViewModel.lastSuspiciousLocation.observe(this) { location ->
            if (location != null) {
                Log.e("HomeActivity", "📍 Ubicación sospechosa: ${location.latitude}, ${location.longitude}")
            }
        }
    }

    private fun lockDeviceNow() {
        val dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager

        // TODO: Descomentar cuando tengas DeviceAdminReceiver implementado
        // val adminComponent = ComponentName(this, DeviceAdminReceiver::class.java)
        // if (dpm.isAdminActive(adminComponent)) {
        //     dpm.lockNow()
        //     Log.d("HomeActivity", "✓ Dispositivo bloqueado")
        //     Toast.makeText(this, "🔒 Dispositivo bloqueado por seguridad", Toast.LENGTH_LONG).show()
        // } else {
        //     Log.e("HomeActivity", "❌ Device Admin no activo")
        //     Toast.makeText(this, "❌ Device Admin no configurado", Toast.LENGTH_SHORT).show()
        // }

        Log.w("HomeActivity", "⚠️ Device Admin aún no implementado - TODO")
        Toast.makeText(this, "⚠️ Bloqueo remoto disponible después de configuración", Toast.LENGTH_SHORT).show()
    }

    /**
     * Método para que los servicios reporten anomalías
     * (Llamado desde SensorService cuando detecta movimiento)
     */
    fun onAbnormalMovementDetected(acceleration: Float, location: Location?) {
        Log.w("HomeActivity", "🚨 Movimiento anormal recibido: ${acceleration}m/s²")
        securityViewModel.onAbnormalMovementDetected(acceleration, location)
    }

    /**
     * Método para que los servicios reporten velocidad sospechosa
     * (Llamado desde LocationTrackingService)
     */
    fun onSuspiciousSpeedDetected(
        location: Location,
        distance: Float,
        timeDiffSeconds: Double,
        calculatedSpeed: Double
    ) {
        Log.w("HomeActivity", "🚨 Velocidad sospechosa recibida: ${calculatedSpeed}m/s")
        securityViewModel.onSuspiciousSpeedDetected(location, distance, timeDiffSeconds, calculatedSpeed)
    }

    /**
     * Método para el botón de pánico manual
     */
    fun triggerPanicButton(location: Location? = null, reason: String = "") {
        Log.e("HomeActivity", "🚨🚨🚨 PÁNICO MANUAL ACTIVADO")
        securityViewModel.triggerPanicButton(location, reason)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Limpiar servicios si es necesario
    }
}