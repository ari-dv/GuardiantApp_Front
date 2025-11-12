package com.guardiant.app.auth

import android.app.Activity
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
// ¡NUEVAS IMPORTACIONES!
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
// (El resto de tus importaciones)
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    // ¡NUEVO! Conexión directa a Firestore
    private val db = Firebase.firestore

    private val MAGIC_VERIFICATION_CODE = "123456"

    // --- LiveData ---
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _registrationSuccess = MutableLiveData<Boolean>()
    val registrationSuccess: LiveData<Boolean> = _registrationSuccess

    private val _loginSuccess = MutableLiveData<Boolean>()
    val loginSuccess: LiveData<Boolean> = _loginSuccess

    private val _smsCodeSent = MutableLiveData<String?>()
    val smsCodeSent: LiveData<String?> = _smsCodeSent

    private val _smsVerificationSuccess = MutableLiveData<Boolean>()
    val smsVerificationSuccess: LiveData<Boolean> = _smsVerificationSuccess

    private val _isSetupComplete = MutableLiveData<Boolean>()
    val isSetupComplete: LiveData<Boolean> = _isSetupComplete


    // --- (registerUser, loginUser, sendSms, verifySms, validatePassword... sin cambios) ---
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

    fun sendVerificationSms(phoneNumber: String, activity: Activity) {
        Log.d("AuthViewModel", "Simulando envío de SMS. Usa el código: $MAGIC_VERIFICATION_CODE")
        _smsCodeSent.value = "fake-verification-id"
    }
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

    // --- ¡FUNCIÓN ACTUALIZADA! ---
    /**
     * Llama directamente a Firestore (rápido) para saber a dónde navegar.
     */
    fun checkSetupStatus() {
        viewModelScope.launch {
            val user = auth.currentUser
            if (user == null) {
                _errorMessage.value = "Usuario no encontrado."
                _isSetupComplete.value = false // Asumir incompleto si no hay usuario
                return@launch
            }

            try {
                // 1. Llamada directa a Firestore
                val userDoc = db.collection("users").document(user.uid).get().await()

                if (userDoc.exists()) {
                    // 2. Leemos el campo 'completed' del 'setup'
                    val setupData = userDoc.get("setup") as? Map<*, *>
                    val setupCompleted = setupData?.get("completed") as? Boolean ?: false
                    _isSetupComplete.value = setupCompleted
                } else {
                    // El trigger 'onUserCreate' aún no ha creado el documento
                    _errorMessage.value = "Documento de usuario aún no existe, reintentando..."
                    _isSetupComplete.value = false
                }
            } catch (e: Exception) {
                _errorMessage.value = "Excepción al verificar setup: ${e.message}"
                _isSetupComplete.value = false
            }
        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }
}