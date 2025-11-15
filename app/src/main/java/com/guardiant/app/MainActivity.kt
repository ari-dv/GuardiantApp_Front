package com.guardiant.app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.guardiant.app.auth.AuthViewModel
import com.guardiant.app.auth.LoginActivity
import com.guardiant.app.auth.UnlockActivity
import com.guardiant.app.auth.VerificationActivity
import com.guardiant.app.databinding.ActivityMainBinding
import com.guardiant.app.security.LockScreenService
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Verificar si ya hay usuario autenticado con setup completo
        checkExistingUser()

        setupObservers()

        // Botón de Registrar
        binding.buttonRegister.setOnClickListener {
            val email = binding.editTextEmail.text.toString().trim()
            val password = binding.editTextPassword.text.toString().trim()
            val confirmPassword = binding.editTextConfirmPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            binding.progressBar.visibility = View.VISIBLE
            authViewModel.registerUser(email, password)
        }

        // Texto para ir a Login
        binding.textViewGoToLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun setupObservers() {
        authViewModel.errorMessage.observe(this) { message ->
            binding.progressBar.visibility = View.GONE
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        }

        authViewModel.registrationSuccess.observe(this) { success ->
            if (success) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this, "Registro exitoso. Siguiente paso: verificar teléfono.", Toast.LENGTH_LONG).show()
                startActivity(Intent(this, VerificationActivity::class.java))
                finish()
            }
        }
    }

    /**
     * Verifica si ya hay un usuario autenticado con setup completo
     * Si es así, redirige a UnlockActivity
     */
    private fun checkExistingUser() {
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        if (currentUser != null) {
            // Usuario ya autenticado, verificar setup
            authViewModel.checkSetupStatus()
            
            authViewModel.isSetupComplete.observe(this) { isComplete ->
                if (isComplete) {
                    // Iniciar servicio de lock screen
                    startService(Intent(this, LockScreenService::class.java))
                    
                    // Redirigir a pantalla de bloqueo
                    val intent = Intent(this, UnlockActivity::class.java)
                    startActivity(intent)
                    finishAffinity()
                }
            }
        }
    }
}