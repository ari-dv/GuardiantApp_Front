package com.guardiant.app.auth

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
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.guardiant.app.network.GuardiantApi
import com.guardiant.app.network.OnCallRequest
import com.guardiant.app.network.VerifyPinRequest
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db = Firebase.firestore
    private val api = GuardiantApi.create()

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

    /**
     * Registrar usuario con email y contraseña
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
     * Login con email y contraseña
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
     * Enviar SMS de verificación (simulado)
     */
    fun sendVerificationSms(phoneNumber: String, activity: Activity) {
        Log.d("AuthViewModel", "Simulando envío de SMS. Usa el código: $MAGIC_VERIFICATION_CODE")
        _smsCodeSent.value = "fake-verification-id"
    }

    /**
     * Verificar código SMS
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

    /**
     * Validar contraseña (8+ chars, 1 mayúscula, 1 símbolo)
     */
    private fun validatePassword(password: String): String? {
        val passwordPattern = Regex("^(?=.*[A-Z])(?=.*[!@#\$%^&*()_+=\\[\\]{};':\"\\\\|,.<>\\/?]).{8,}\$")
        if (!passwordPattern.matches(password)) {
            return "La contraseña debe tener 8+ caracteres, 1 mayúscula y 1 símbolo."
        }
        return null
    }

    /**
     * Verificar estado del setup del usuario
     */
    fun checkSetupStatus() {
        viewModelScope.launch {
            val user = auth.currentUser
            if (user == null) {
                _errorMessage.value = "Usuario no encontrado."
                _isSetupComplete.value = false
                return@launch
            }

            try {
                val userDoc = db.collection("users").document(user.uid).get().await()

                if (userDoc.exists()) {
                    val setupData = userDoc.get("setup") as? Map<*, *>
                    val setupCompleted = setupData?.get("completed") as? Boolean ?: false
                    _isSetupComplete.value = setupCompleted
                } else {
                    _errorMessage.value = "Documento de usuario aún no existe, reintentando..."
                    _isSetupComplete.value = false
                }
            } catch (e: Exception) {
                _errorMessage.value = "Excepción al verificar setup: ${e.message}"
                _isSetupComplete.value = false
            }
        }
    }

    /**
     * Verificar PIN (para desbloqueo)
     */
    fun verifyPin(pin: String) {
        viewModelScope.launch {
            try {
                val user = auth.currentUser ?: return@launch
                val token = user.getIdToken(true).await().token ?: return@launch

                val request = OnCallRequest(data = VerifyPinRequest(pin = pin))
                val response = api.verifyPin("Bearer $token", request)

                if (response.isSuccessful && response.body()?.result?.success == true) {
                    _loginSuccess.value = true
                } else {
                    _errorMessage.value = response.body()?.result?.message ?: "PIN incorrecto"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error verificando PIN: ${e.message}"
            }
        }
    }

    /**
     * Limpiar mensaje de error
     */
    fun clearErrorMessage() {
        _errorMessage.value = null
    }
}