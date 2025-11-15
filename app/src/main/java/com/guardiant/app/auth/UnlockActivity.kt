package com.guardiant.app.auth

import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.location.LocationServices
import com.guardiant.app.databinding.ActivityUnlockBinding
import com.guardiant.app.main.HomeActivity
import com.guardiant.app.network.GuardiantApi
import com.guardiant.app.network.OnCallRequest
import com.guardiant.app.network.PanicButtonRequest
import com.guardiant.app.network.VerifyPinRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.android.gms.location.FusedLocationProviderClient
import com.guardiant.app.ui.PinKeypadComponent
import com.guardiant.app.security.CoercionStateManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class UnlockActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUnlockBinding
    private val authViewModel: AuthViewModel by viewModels()
    private val api = GuardiantApi.create()
    private val auth = FirebaseAuth.getInstance()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var coercionManager: CoercionStateManager

    private var pinBuilder = StringBuilder()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUnlockBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        coercionManager = CoercionStateManager.getInstance(this)

        setupKeypad()
        setupObservers()
        updateUIForStep()
    }

    /**
     * Conecta los listeners para el teclado numérico
     */
    private fun setupKeypad() {
        val keypadGrid = binding.keypad
        for (i in 0 until keypadGrid.childCount) {
            val view = keypadGrid.getChildAt(i)
            if (view is Button) {
                view.setOnClickListener { onKeypadClick(it) }
            }
        }

        binding.buttonUnlock.setOnClickListener { handleUnlockClick() }
    }

    /**
     * Actualiza la UI
     */
    private fun updateUIForStep() {
        binding.textViewTitle.text = "🔒 Desbloquear Dispositivo"
        binding.textViewDesc.text = "Ingresa tu PIN para desbloquear"
        binding.buttonUnlock.text = "Desbloquear"
        pinBuilder.clear()
        updatePinDisplay()
    }

    /**
     * Maneja el clic en los botones del 0-9 y borrar
     */
    private fun onKeypadClick(view: View) {
        val tag = view.tag.toString()

        if (pinBuilder.length >= 6 && tag != "del") {
            return
        }

        when (tag) {
            "del" -> {
                if (pinBuilder.isNotEmpty()) {
                    pinBuilder.deleteCharAt(pinBuilder.length - 1)
                }
            }
            else -> {
                pinBuilder.append(tag)
            }
        }
        updatePinDisplay()
    }

    /**
     * Muestra los puntos (●) en el visor
     */
    private fun updatePinDisplay() {
        val pinText = "● ".repeat(pinBuilder.length)
        binding.textPinDisplay.text = pinText.trim()
    }

    /**
     * Lógica principal al presionar "Desbloquear"
     */
    private fun handleUnlockClick() {
        val pin = pinBuilder.toString()

        // Validación de longitud
        if (pin.length < 4) {
            Toast.makeText(this, "El PIN debe tener al menos 4 dígitos", Toast.LENGTH_SHORT).show()
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        binding.buttonUnlock.isEnabled = false

        // Verificar PIN
        authViewModel.verifyPin(pin)
    }

    /**
     * Escucha las respuestas del ViewModel
     */
    private fun setupObservers() {
        authViewModel.errorMessage.observe(this) { message ->
            if (message != null) {
                binding.progressBar.visibility = View.GONE
                binding.buttonUnlock.isEnabled = true
                Toast.makeText(this, "❌ $message", Toast.LENGTH_LONG).show()
                authViewModel.clearErrorMessage()
                pinBuilder.clear()
                updatePinDisplay()
            }
        }

        // Observar resultado de verificación de PIN
        authViewModel.loginSuccess.observe(this) { success ->
            if (success) {
                checkPinMode(pinBuilder.toString())
            }
        }
    }

    /**
     * Determinar si fue PIN normal o de seguridad
     */
    private fun checkPinMode(pin: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val user = auth.currentUser ?: return@launch

            try {
                val token = user.getIdToken(true).await().token ?: return@launch

                val request = OnCallRequest(
                    data = VerifyPinRequest(pin = pin)
                )

                val response = api.verifyPin("Bearer $token", request)

                if (response.isSuccessful) {
                    val result = response.body()?.result
                    val mode = result?.mode

                    if (mode == "security") {
                        handleSecurityPinUsed()
                    } else {
                        runOnUiThread {
                            handleUnlockSuccess()
                        }
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@UnlockActivity, "Error verificando PIN", Toast.LENGTH_SHORT).show()
                        binding.progressBar.visibility = View.GONE
                        binding.buttonUnlock.isEnabled = true
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    binding.progressBar.visibility = View.GONE
                    binding.buttonUnlock.isEnabled = true
                }
            }
        }
    }

    /**
     * Maneja PIN de seguridad (COERCIÓN)
     */
    private fun handleSecurityPinUsed() {
        runOnUiThread {
            Toast.makeText(this, "✅ Desbloqueo exitoso", Toast.LENGTH_SHORT).show()
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 1. Activar modo de coerción
                coercionManager.enableCoercionMode()

                // 2. Obtener apps protegidas del backend
                fetchAndSaveProtectedApps()

                // 3. Capturar y enviar ubicación
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)
                        == android.content.pm.PackageManager.PERMISSION_GRANTED) {

                        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                            sendCoercionAlert(location)
                        }
                    } else {
                        // Enviar sin ubicación si no hay permiso
                        sendCoercionAlert(null)
                    }
                } else {
                    sendCoercionAlert(null)
                }

                // 4. Salir al home launcher (NO a HomeActivity)
                runOnUiThread {
                    exitToHomeLauncher()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    exitToHomeLauncher()
                }
            }
        }
    }

    /**
     * Obtiene las apps protegidas del backend y las guarda localmente
     */
    private suspend fun fetchAndSaveProtectedApps() {
        try {
            val user = auth.currentUser ?: return
            val token = user.getIdToken(true).await().token ?: return

            val response = api.getProtectedApps("Bearer $token")

            if (response.isSuccessful && response.body() != null) {
                val apps = response.body()!!.result.apps
                coercionManager.saveProtectedApps(apps)
                Log.d("UnlockActivity", "Apps protegidas guardadas: ${apps.size}")
            } else {
                Log.e("UnlockActivity", "Error al obtener apps: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e("UnlockActivity", "Error fetchAndSaveProtectedApps: ${e.message}", e)
        }
    }

    /**
     * Sale al home launcher del dispositivo (no a HomeActivity)
     */
    private fun exitToHomeLauncher() {
        binding.progressBar.visibility = View.GONE
        
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
        finishAffinity()
    }

    /**
     * Enviar alerta de coerción al backend
     */
    private fun sendCoercionAlert(location: Location?) {
        CoroutineScope(Dispatchers.IO).launch {
            val user = auth.currentUser ?: return@launch

            try {
                val token = user.getIdToken(true).await().token ?: return@launch

                val request = OnCallRequest(
                    data = PanicButtonRequest(
                        latitude = location?.latitude ?: 0.0,
                        longitude = location?.longitude ?: 0.0,
                        reason = "PIN de seguridad utilizado - COERCIÓN DETECTADA"
                    )
                )

                api.triggerPanicButton("Bearer $token", request)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Desbloquear exitosamente
     */
    private fun handleUnlockSuccess() {
        binding.progressBar.visibility = View.GONE
        Toast.makeText(this, "✅ Desbloqueo exitoso", Toast.LENGTH_SHORT).show()

        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finishAffinity()
    }
}