package com.guardiant.app.setup // <-- ¡PAQUETE CORREGIDO!

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
// --- ¡IMPORTACIONES CORREGIDAS! ---
// Estas líneas resuelven 'Unresolved reference 'R'' y 'ActivitySetupAppsBinding'
import com.guardiant.app.R
import com.guardiant.app.databinding.ActivitySetupAppsBinding
import com.guardiant.app.main.HomeActivity
import com.guardiant.app.network.AppConfig
// ---------------------------------

class SetupAppsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySetupAppsBinding
    private val setupViewModel: SetupViewModel by viewModels()

    // Lista Falsa para la Demo
    // ¡CORRECCIÓN! Usamos los IDs reales de 'R'
    private val fakeAppsList by lazy {
        mapOf(
            binding.checkApp1.id to AppConfig("BCP (Demo)", "com.bcp.demo", null),
            binding.checkApp2.id to AppConfig("Yape (Demo)", "com.yape.demo", null),
            binding.checkApp3.id to AppConfig("Interbank (Demo)", "com.interbank.demo", null),
            binding.checkApp4.id to AppConfig("Galería (Demo)", "com.gallery.demo", null)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySetupAppsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Ya no necesitamos refrescar el token aquí, el ViewModel lo hace
        // setupViewModel.refreshIdToken()
        setupObservers()

        binding.buttonSaveApps.setOnClickListener {
            saveSelectedApps()
        }
    }

    private fun saveSelectedApps() {
        binding.progressBar.visibility = View.VISIBLE

        val selectedApps = mutableListOf<AppConfig>()

        // Recolectar apps de la lista falsa
        fakeAppsList.forEach { (checkboxId, appConfig) ->
            // Usamos binding para encontrar las vistas, es más seguro
            val checkBox = findViewById<CheckBox>(checkboxId)
            if (checkBox != null && checkBox.isChecked) {
                selectedApps.add(appConfig)
            }
        }

        if (selectedApps.isEmpty()) {
            Toast.makeText(this, "Selecciona al menos una app", Toast.LENGTH_SHORT).show()
            binding.progressBar.visibility = View.GONE
            return
        }

        // Llamar al ViewModel para guardar
        setupViewModel.saveProtectedApps(selectedApps)
    }

    private fun setupObservers() {
        setupViewModel.errorMessage.observe(this) { message ->
            if (message != null) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this, "Error: $message", Toast.LENGTH_LONG).show()
                setupViewModel.clearErrorMessage()
            }
        }

        setupViewModel.saveAppsSuccess.observe(this) { success ->
            if (success) {
                // Apps guardadas, ahora llamamos a finalizar el setup
                // (En un flujo real, aquí iría la pantalla de Permisos)
                // Para la hackatón, finalizamos de frente.
                Toast.makeText(this, "Apps guardadas, finalizando...", Toast.LENGTH_SHORT).show()
                setupViewModel.completeSetup()
            }
        }

        setupViewModel.setupCompleteSuccess.observe(this) { success ->
            if (success) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this, "¡Configuración Completa!", Toast.LENGTH_SHORT).show()

                // ¡Setup terminado! Navegamos a Home
                val intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)
                finishAffinity() // Cierra todas las actividades anteriores
            }
        }
    }
}