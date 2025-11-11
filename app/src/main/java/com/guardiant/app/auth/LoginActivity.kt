package com.guardiant.app.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.guardiant.app.MainActivity
import com.guardiant.app.databinding.ActivityLoginBinding
// Importamos la nueva actividad de Setup
import com.guardiant.app.setup.SetupPinsActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupObservers()

        // Botón de Login
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

        // Texto para ir a Registro
        binding.textViewGoToRegister.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun setupObservers() {
        // Observador de errores
        authViewModel.errorMessage.observe(this) { message ->
            if (message != null) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            }
        }

        // Observador de éxito en login
        authViewModel.loginSuccess.observe(this) { success ->
            if (success) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show()

                // ---- LÓGICA ACTUALIZADA ----
                // ¡Navegamos a la configuración de PINs!
                val intent = Intent(this, SetupPinsActivity::class.java)
                startActivity(intent)
                finishAffinity() // Cierra todas las actividades de auth
            }
        }
    }
}