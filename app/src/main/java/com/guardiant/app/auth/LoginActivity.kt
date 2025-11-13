package com.guardiant.app.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.guardiant.app.MainActivity
import com.guardiant.app.databinding.ActivityLoginBinding
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
                Toast.makeText(this, "Complete todos los campos", Toast.LENGTH_SHORT).show()
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
                authViewModel.clearErrorMessage()
            }
        }

        authViewModel.loginSuccess.observe(this) { success ->
            if (success) {
                Toast.makeText(this, "Inicio de sesión exitoso. Verificando estado...", Toast.LENGTH_SHORT).show()
                authViewModel.checkSetupStatus()
            }
        }

        authViewModel.isSetupComplete.observe(this) { isComplete ->
            binding.progressBar.visibility = View.GONE

            if (isComplete) {
                Toast.makeText(this, "¡Bienvenido de vuelta!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)
                finishAffinity()
            } else {
                Toast.makeText(this, "Completando configuración...", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, SetupPinsActivity::class.java)
                startActivity(intent)
                finishAffinity()
            }
        }
    }
}