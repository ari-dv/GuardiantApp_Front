package com.guardiant.app.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
// ¡ASEGÚRATE DE QUE ESTA LÍNEA DE IMPORT ESTÉ PRESENTE!
import com.guardiant.app.databinding.ActivityVerificationBinding
// Importamos el onboarding de permisos
import com.guardiant.app.permissions.OnboardingActivity

class VerificationActivity : AppCompatActivity() {

    // Esta línea usa el import de ...databinding...
    private lateinit var binding: ActivityVerificationBinding

    // Esta línea usa el import de androidx.activity.viewModels
    private val authViewModel: AuthViewModel by viewModels()

    private var currentVerificationId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Esta línea usa la variable 'binding'
        binding = ActivityVerificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupObservers()

        // Botón "Enviar SMS" (Simulado)
        binding.buttonSendSms.setOnClickListener {
            val phoneNumber = binding.editTextPhone.text.toString().trim()
            if (phoneNumber.length < 9) {
                Toast.makeText(this, "Número inválido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            binding.progressBar.visibility = View.VISIBLE
            // Llamamos a la función simulada
            authViewModel.sendVerificationSms(phoneNumber, this)
        }

        // Botón "Verificar Código"
        binding.buttonVerifyCode.setOnClickListener {
            val code = binding.editTextCode.text.toString().trim()
            if (code.length < 6) {
                Toast.makeText(this, "Código debe tener 6 dígitos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (currentVerificationId != null) {
                binding.progressBar.visibility = View.VISIBLE
                // Llamamos a la función simulada de verificación
                authViewModel.verifySmsCode(currentVerificationId!!, code)
            } else {
                Toast.makeText(this, "Presiona 'Enviar Código' primero", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupObservers() {
        // Observador de errores
        authViewModel.errorMessage.observe(this) { message ->
            if (message != null) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                authViewModel.clearErrorMessage()
            }
        }

        // Se disparará por la simulación
        authViewModel.smsCodeSent.observe(this) { verificationId ->
            if (verificationId != null) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this, "¡Código Mágico 'Enviado'! (Usa 123456)", Toast.LENGTH_LONG).show()
                currentVerificationId = verificationId
                // Mostrar los campos para el código
                binding.codeEntryGroup.visibility = View.VISIBLE // <-- ESTA LÍNEA DA EL ERROR
            }
        }

        // Se disparará por la simulación
        authViewModel.smsVerificationSuccess.observe(this) { success ->
            if (success) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this, "¡Verificación Mágica Exitosa!", Toast.LENGTH_SHORT).show()

                // En el método de verificación exitosa, cambia:
                onVerificationSuccess()
            }
        }
    }

    private fun onVerificationSuccess() {
        Toast.makeText(this, "✅ Verificación exitosa", Toast.LENGTH_SHORT).show()

        // 🆕 NAVEGAR AL ONBOARDING DE PERMISOS
        val intent = Intent(this, com.guardiant.app.permissions.OnboardingActivity::class.java)
        startActivity(intent)
        finish()
    }
}