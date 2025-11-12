package com.guardiant.app.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.guardiant.app.MainActivity
import com.guardiant.app.databinding.ActivityLoginBinding
// Importamos los dos posibles destinos
import com.guardiant.app.setup.SetupPinsActivity
import com.guardiant.app.main.HomeActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupObservers()

        binding.buttonLogin.setOnClickListener {
            val email = binding.editTextEmail.text.toString().trim()
            val password = binding.editTextPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Complete todos loscampos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            binding.progressBar.visibility = View.VISIBLE
            authViewModel.loginUser(email, password)
        }

        binding.textViewGoToRegister.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun setupObservers() {
        authViewModel.errorMessage.observe(this) { message ->
            if (message != null) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                authViewModel.clearErrorMessage() // Limpiar después de mostrar
            }
        }

        authViewModel.loginSuccess.observe(this) { success ->
            if (success) {
                // --- ¡MEJORA DE UX! ---
                // Informamos al usuario que estamos revisando el estado
                Toast.makeText(this, "Inicio de sesión exitoso. Verificando estado...", Toast.LENGTH_SHORT).show()

                // 1. El Login fue exitoso. AHORA, revisamos el estado del setup.
                authViewModel.checkSetupStatus()
            }
        }

        authViewModel.isSetupComplete.observe(this) { isComplete ->
            binding.progressBar.visibility = View.GONE

            // 2. Navegamos basado en el resultado
            if (isComplete) {
                // Ir al Dashboard Principal
                Toast.makeText(this, "¡Bienvenido de vuelta!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)
                finishAffinity()
            } else {
                // Ir al inicio del Flujo de Configuración
                Toast.makeText(this, "Completando configuración...", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, SetupPinsActivity::class.java)
                startActivity(intent)
                finishAffinity()
            }
        }
    }
}