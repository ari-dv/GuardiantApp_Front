package com.guardiant.app.setup

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.GridLayout
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.guardiant.app.databinding.ActivitySetupPinsBinding
import com.guardiant.app.setup.SetupAppsActivity

class SetupPinsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySetupPinsBinding
    private val setupViewModel: SetupViewModel by viewModels()

    private enum class SetupStep {
        SET_NORMAL_PIN,
        CONFIRM_NORMAL_PIN,
        SET_SECURITY_PIN
    }
    private var currentStep = SetupStep.SET_NORMAL_PIN
    private var tempNormalPin = ""
    private var pinBuilder = StringBuilder()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySetupPinsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupKeypad()
        setupObservers()
        updateUIForStep()
    }

    /**
     * Conecta los listeners para el teclado numérico
     */
    private fun setupKeypad() {
        val keypadGrid = binding.keypad // <-- CORREGIDO: Usamos el ID 'keypad' del XML
        for (i in 0 until keypadGrid.childCount) {
            val view = keypadGrid.getChildAt(i)
            if (view is Button) {
                // Usamos el 'tag' del botón (0-9, del)
                view.setOnClickListener { onKeypadClick(it) }
            }
        }

        binding.buttonContinue.setOnClickListener { handleContinueClick() }
    }

    /**
     * Actualiza la UI (títulos y texto) según el paso actual
     */
    private fun updateUIForStep() {
        when (currentStep) {
            SetupStep.SET_NORMAL_PIN -> {
                binding.textViewTitle.text = "Paso 1: PIN Normal"
                binding.textViewDesc.text = "Crea tu PIN normal (4-6 dígitos)."
                binding.buttonContinue.text = "Continuar"
            }
            SetupStep.CONFIRM_NORMAL_PIN -> {
                binding.textViewTitle.text = "Confirma tu PIN Normal"
                binding.textViewDesc.text = "Vuelve a ingresar tu PIN normal."
                binding.buttonContinue.text = "Confirmar"
            }
            SetupStep.SET_SECURITY_PIN -> {
                binding.textViewTitle.text = "Paso 2: PIN de Seguridad"
                binding.textViewDesc.text = "Crea un PIN de emergencia (diferente al normal)."
                binding.buttonContinue.text = "Finalizar Configuración de PINs"
            }
        }
        pinBuilder.clear()
        updatePinDisplay()
    }

    /**
     * Maneja el clic en los botones del 0-9 y borrar
     */
    private fun onKeypadClick(view: View) {
        // Obtenemos el tag (el número o "del")
        val tag = view.tag.toString()

        // Límite de 6 dígitos
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
        binding.textPinDisplay.text = pinText.trim() // <-- CORREGIDO: Usamos el ID 'textPinDisplay'
    }

    /**
     * Lógica principal al presionar "Continuar/Confirmar/Finalizar"
     */
    private fun handleContinueClick() {
        val pin = pinBuilder.toString()

        // Validación de longitud
        if (pin.length < 4) {
            Toast.makeText(this, "El PIN debe tener al menos 4 dígitos", Toast.LENGTH_SHORT).show()
            return
        }

        when (currentStep) {
            SetupStep.SET_NORMAL_PIN -> {
                tempNormalPin = pin
                currentStep = SetupStep.CONFIRM_NORMAL_PIN
                updateUIForStep()
            }
            SetupStep.CONFIRM_NORMAL_PIN -> {
                if (pin == tempNormalPin) {
                    currentStep = SetupStep.SET_SECURITY_PIN
                    updateUIForStep()
                } else {
                    Toast.makeText(this, "Los PINs no coinciden", Toast.LENGTH_SHORT).show()
                    pinBuilder.clear()
                    updatePinDisplay()
                }
            }
            SetupStep.SET_SECURITY_PIN -> {
                if (pin == tempNormalPin) {
                    Toast.makeText(this, "El PIN de seguridad debe ser DIFERENTE al normal", Toast.LENGTH_SHORT).show()
                    pinBuilder.clear()
                    updatePinDisplay()
                    return
                }

                binding.progressBar.visibility = View.VISIBLE
                setupViewModel.savePins(tempNormalPin, pin)
            }
        }
    }

    /**
     * Escucha las respuestas del ViewModel
     */
    private fun setupObservers() {
        setupViewModel.errorMessage.observe(this) { message ->
            if (message != null) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this, "Error: $message", Toast.LENGTH_LONG).show()
                setupViewModel.clearErrorMessage()
            }
        }

        setupViewModel.savePinsSuccess.observe(this) { success ->
            if (success) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this, "PINs guardados exitosamente", Toast.LENGTH_SHORT).show()

                // Navegamos al Paso 2: Configurar Apps
                val intent = Intent(this, SetupAppsActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }
}