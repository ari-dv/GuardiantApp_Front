package com.guardiant.app.setup

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.guardiant.app.databinding.ActivitySetupAppsBinding
import com.guardiant.app.main.HomeActivity
import com.guardiant.app.network.AppConfig

class SetupAppsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySetupAppsBinding
    private val setupViewModel: SetupViewModel by viewModels()

    // --- ¡NUEVO! ---
    // Almacena las apps encontradas para enviarlas
    private var foundAppsMap = mutableMapOf<Int, AppConfig>() // <CheckboxId, AppConfig>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySetupAppsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Refrescar el token (necesario para las llamadas a API

        // 2. Configurar los observadores
        setupObservers()

        // 3. ¡Ejecutar el escaneo real!
        binding.progressBar.visibility = View.VISIBLE
        // Le pasamos el 'packageManager' de Android al ViewModel
        setupViewModel.loadInstalledApps(packageManager)

        // 4. Configurar el botón de guardar
        binding.buttonSaveApps.setOnClickListener {
            saveSelectedApps()
        }
    }

    private fun setupObservers() {
        setupViewModel.errorMessage.observe(this) { message ->
            if (message != null) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this, "Error: $message", Toast.LENGTH_LONG).show()
                setupViewModel.clearErrorMessage()
            }
        }

        // --- ¡NUEVO! ---
        // Observador para la lista de apps encontradas
        setupViewModel.foundApps.observe(this) { apps ->
            binding.progressBar.visibility = View.GONE
            binding.appsListContainer.removeAllViews()
            foundAppsMap.clear()

            if (apps.isNotEmpty()) {
                apps.forEach { appInfo ->
                    val checkBox = CheckBox(this).apply {
                        text = appInfo.appName  // ✅ SOLO EL NOMBRE, SIN PACKAGE
                        isChecked = true
                        id = View.generateViewId()
                        textSize = 16f
                        setPadding(8, 16, 8, 16)
                    }
                    foundAppsMap[checkBox.id] = AppConfig(appInfo.appName, appInfo.packageName, null)
                    binding.appsListContainer.addView(checkBox)
                }
            } else {
                val textView = android.widget.TextView(this).apply {
                    text = "No se encontraron apps bancarias instaladas.\nPuedes continuar sin proteger apps."
                    textSize = 16f
                    setPadding(16, 16, 16, 16)
                }
                binding.appsListContainer.addView(textView)
            }
        }

        // Observador para guardar apps
        setupViewModel.saveAppsSuccess.observe(this) { success ->
            if (success) {
                // Apps guardadas, ahora llamamos a finalizar el setup
                // (Para la hackatón, finalizamos de frente)
                setupViewModel.completeSetup()
            }
        }

        // Observador para setup completo
        setupViewModel.setupCompleteSuccess.observe(this) { success ->
            if (success) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this, "¡Apps guardadas!", Toast.LENGTH_SHORT).show()

                // NAVEGAR A PERMISOS (OnboardingActivity)
                val intent = Intent(this, com.guardiant.app.permissions.OnboardingActivity::class.java)
                startActivity(intent)
                finishAffinity()
            }
        }
    }

    private fun saveSelectedApps() {
        binding.progressBar.visibility = View.VISIBLE

        val selectedApps = mutableListOf<AppConfig>()

        // Recolectar apps de los CheckBoxes dinámicos
        foundAppsMap.forEach { (checkboxId, appConfig) ->
            findViewById<CheckBox>(checkboxId)?.let {
                if (it.isChecked) {
                    selectedApps.add(appConfig)
                }
            }
        }

        if (selectedApps.isEmpty()) {
            Toast.makeText(this, "Debes seleccionar al menos una app para proteger", Toast.LENGTH_SHORT).show()
            binding.progressBar.visibility = View.GONE
            return
        }

        // Llamar al ViewModel para guardar
        setupViewModel.saveProtectedApps(selectedApps)
    }
}