package com.guardiant.app.setup

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
// Importaciones corregidas
import com.guardiant.app.network.AppConfig
import com.guardiant.app.network.GuardiantApi
import com.guardiant.app.network.OnCallRequest
import com.guardiant.app.network.ProtectionLevelRequest
import com.guardiant.app.network.ProtectedAppsRequest
import com.guardiant.app.network.SavePinsData
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SetupViewModel : ViewModel() {

    private val api = GuardiantApi.create()
    private val auth = FirebaseAuth.getInstance()

    private val _savePinsSuccess = MutableLiveData<Boolean>()
    val savePinsSuccess: LiveData<Boolean> = _savePinsSuccess

    private val _saveAppsSuccess = MutableLiveData<Boolean>()
    val saveAppsSuccess: LiveData<Boolean> = _saveAppsSuccess

    private val _setupCompleteSuccess = MutableLiveData<Boolean>()
    val setupCompleteSuccess: LiveData<Boolean> = _setupCompleteSuccess

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private suspend fun getFreshAuthHeader(): String? {
        val user = auth.currentUser
        if (user == null) {
            _errorMessage.postValue("Usuario no autenticado. Inicie sesión de nuevo.")
            return null
        }
        try {
            val idToken = user.getIdToken(true).await().token
            if (idToken == null) throw Exception("Token de ID nulo después de obtenerlo.")
            return "Bearer $idToken"
        } catch(e: Exception) {
            _errorMessage.postValue("Error al obtener token de seguridad: ${e.message}")
            return null
        }
    }

    /**
     * Paso 1: Guardar PINs
     */
    fun savePins(normalPin: String, securityPin: String) {
        viewModelScope.launch {
            val authHeader = getFreshAuthHeader() ?: return@launch
            try {
                val data = SavePinsData(normalPin, securityPin)
                val request = OnCallRequest(data = data)
                val response = api.savePins(authHeader, request)

                // --- ¡LA CORRECCIÓN! ---
                // Accedemos al objeto "result" interno
                val result = response.body()?.result

                if (response.isSuccessful && result?.success == true) {
                    _savePinsSuccess.value = true
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Error desconocido"
                    Log.e("SetupViewModel", "API Error: $errorBody")
                    // El mensaje de error ahora está en 'result'
                    _errorMessage.value = result?.message ?: "Error al guardar PINs (Código: ${response.code()})"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Excepción: ${e.message}"
            }
        }
    }

    /**
     * Paso 2: Guardar Apps
     */
    fun saveProtectedApps(apps: List<AppConfig>) {
        viewModelScope.launch {
            val authHeader = getFreshAuthHeader() ?: return@launch
            try {
                val data = ProtectedAppsRequest(apps = apps)
                val request = OnCallRequest(data = data)
                val response = api.saveProtectedApps(authHeader, request)

                // --- ¡LA CORRECCIÓN! ---
                val result = response.body()?.result

                if (response.isSuccessful && result?.success == true) {
                    _saveAppsSuccess.value = true
                } else {
                    _errorMessage.value = result?.message ?: "Error al guardar Apps"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Excepción: ${e.message}"
            }
        }
    }

    /**
     * Paso 3: Finalizar Setup
     */
    fun completeSetup(level: String = "extreme") {
        viewModelScope.launch {
            val authHeader = getFreshAuthHeader() ?: return@launch
            try {
                val data = ProtectionLevelRequest(level = level)
                val request = OnCallRequest(data = data)
                val response = api.setProtectionLevel(authHeader, request)

                // --- ¡LA CORRECCIÓN! ---
                val result = response.body()?.result

                if (response.isSuccessful && result?.success == true) {
                    _setupCompleteSuccess.value = true
                } else {
                    _errorMessage.value = result?.message ?: "Error al finalizar setup"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Excepción: ${e.message}"
            }
        }
    }


    fun clearErrorMessage() {
        _errorMessage.value = null
    }
}