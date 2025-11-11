package com.guardiant.app.auth // ¡Corregido el nombre del paquete!

import android.app.Activity
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val MAGIC_VERIFICATION_CODE = "123456" // Código que la app aceptará

    // --- LiveData ---
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _registrationSuccess = MutableLiveData<Boolean>()
    val registrationSuccess: LiveData<Boolean> = _registrationSuccess

    private val _loginSuccess = MutableLiveData<Boolean>()
    val loginSuccess: LiveData<Boolean> = _loginSuccess

    // Usado para la simulación
    private val _smsCodeSent = MutableLiveData<String?>()
    val smsCodeSent: LiveData<String?> = _smsCodeSent

    private val _smsVerificationSuccess = MutableLiveData<Boolean>()
    val smsVerificationSuccess: LiveData<Boolean> = _smsVerificationSuccess


    /**
     * 1. Registro de Usuario (Crea el usuario en Firebase)
     */
    fun registerUser(email: String, password: String) {
        viewModelScope.launch {
            try {
                val passwordError = validatePassword(password)
                if (passwordError != null) {
                    _errorMessage.value = passwordError
                    return@launch
                }
                val authResult = auth.createUserWithEmailAndPassword(email, password).await()
                _registrationSuccess.value = authResult.user != null
            } catch (e: Exception) {
                val message = when (e) {
                    is FirebaseAuthUserCollisionException -> "El correo electrónico ya está en uso."
                    is FirebaseAuthWeakPasswordException -> "La contraseña es demasiado débil."
                    is FirebaseAuthInvalidCredentialsException -> "El formato del correo es inválido."
                    else -> "Error en el registro: ${e.message}"
                }
                _errorMessage.value = message
            }
        }
    }

    /**
     * 2. Login de Usuario
     */
    fun loginUser(email: String, password: String) {
        viewModelScope.launch {
            try {
                val authResult = auth.signInWithEmailAndPassword(email, password).await()
                _loginSuccess.value = authResult.user != null
            } catch (e: Exception) {
                val message = when (e) {
                    is FirebaseAuthInvalidCredentialsException -> "Credenciales incorrectas."
                    else -> "Error al iniciar sesión: ${e.message}"
                }
                _errorMessage.value = message
            }
        }
    }

    /**
     * 3. Envío de SMS (¡SIMULADO!)
     * Simplemente notifica a la UI que el código fue "enviado".
     */
    fun sendVerificationSms(phoneNumber: String, activity: Activity) {
        Log.d("AuthViewModel", "Simulando envío de SMS. Usa el código: $MAGIC_VERIFICATION_CODE")
        _smsCodeSent.value = "fake-verification-id" // ID falso para permitir el siguiente paso
    }

    /**
     * 4. Verificación de Código SMS (¡SIMULADO!)
     * Compara el código ingresado con el código mágico.
     */
    fun verifySmsCode(verificationId: String, code: String) {
        viewModelScope.launch {
            if (code == MAGIC_VERIFICATION_CODE) {
                _smsVerificationSuccess.value = true
            } else {
                _errorMessage.value = "Código de verificación inválido. (Prueba 123456)"
            }
        }
    }

    private fun validatePassword(password: String): String? {
        val passwordPattern = Regex("^(?=.*[A-Z])(?=.*[!@#\$%^&*()_+=\\[\\]{};':\"\\\\|,.<>\\/?]).{8,}\$")
        if (!passwordPattern.matches(password)) {
            return "La contraseña debe tener 8+ caracteres, 1 mayúscula y 1 símbolo."
        }
        return null
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }
}