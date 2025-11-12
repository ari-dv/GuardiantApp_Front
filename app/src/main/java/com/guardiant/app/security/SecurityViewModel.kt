package com.guardiant.app.security

import android.app.Activity
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.guardiant.app.network.GuardiantApi
import com.guardiant.app.network.OnCallRequest
import com.guardiant.app.network.AbnormalMovementRequest
import com.guardiant.app.network.SuspiciousSpeedRequest
import com.guardiant.app.network.PanicButtonRequest
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SecurityViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val api = GuardiantApi.create()
    private val TAG = "SecurityViewModel"

    private val _lockDevice = MutableLiveData<Boolean>()
    val lockDevice: LiveData<Boolean> = _lockDevice

    private val _alertMessage = MutableLiveData<String?>()
    val alertMessage: LiveData<String?> = _alertMessage

    private val _lastSuspiciousLocation = MutableLiveData<Location?>()
    val lastSuspiciousLocation: LiveData<Location?> = _lastSuspiciousLocation

    // Observadores internos
    fun startMonitoring(context: Context) {
        Log.d(TAG, "🛡️ Iniciando monitoreo de seguridad...")

        // 1. Iniciar Sensor Service
        val sensorIntent = Intent(context, SensorService::class.java)
        context.startService(sensorIntent)

        // 2. Iniciar Location Tracking Service
        val locationIntent = Intent(context, LocationTrackingService::class.java)
        context.startService(locationIntent)
    }

    /**
     * Se llama cuando el acelerómetro detecta movimiento anormal
     */
    fun onAbnormalMovementDetected(
        accelerationValue: Float,
        location: Location?
    ) {
        Log.w(TAG, "⚠️ MOVIMIENTO ANORMAL DETECTADO: ${accelerationValue}m/s²")

        _alertMessage.value = "⚠️ Movimiento anormal detectado"

        reportAbnormalMovementToBackend(accelerationValue, location)
    }

    /**
     * Se llama cuando el GPS detecta velocidad imposible
     */
    fun onSuspiciousSpeedDetected(
        location: Location,
        distance: Float,
        timeDiffSeconds: Double,
        calculatedSpeed: Double
    ) {
        Log.e(TAG, "🚨 VELOCIDAD IMPOSIBLE DETECTADA: ${calculatedSpeed}m/s")

        _lastSuspiciousLocation.value = location
        _alertMessage.value = "🚨 Velocidad imposible: ${calculatedSpeed}m/s"

        reportSuspiciousSpeedToBackend(location, distance, timeDiffSeconds, calculatedSpeed)
    }

    /**
     * Usuario presiona botón de pánico manualmente
     */
    fun triggerPanicButton(location: Location?, reason: String = "") {
        Log.e(TAG, "🚨🚨🚨 BOTÓN DE PÁNICO PRESIONADO")

        _alertMessage.value = "🚨 Alerta de pánico activada"

        reportPanicButtonToBackend(location, reason)
    }

    // ============================================
    // REPORTES AL BACKEND
    // ============================================

    /**
     * Enviar movimiento anormal al backend
     */
    private fun reportAbnormalMovementToBackend(
        accelerationValue: Float,
        location: Location?
    ) {
        viewModelScope.launch {
            val user = auth.currentUser ?: return@launch

            try {
                val token = user.getIdToken(true).await().token ?: return@launch

                val request = OnCallRequest(
                    data = AbnormalMovementRequest(
                        latitude = location?.latitude ?: 0.0,
                        longitude = location?.longitude ?: 0.0,
                        accelerationValue = accelerationValue
                    )
                )

                val response = api.reportAbnormalMovement(
                    "Bearer $token",
                    request
                )

                if (response.isSuccessful && response.body()?.result?.success == true) {
                    Log.d(TAG, "✓ Movimiento anormal reportado al backend")
                } else {
                    Log.e(TAG, "❌ Error reportando movimiento: ${response.code()}")
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error enviando movimiento anormal: ${e.message}")
            }
        }
    }

    /**
     * Enviar velocidad sospechosa al backend
     */
    private fun reportSuspiciousSpeedToBackend(
        location: Location,
        distance: Float,
        timeDiffSeconds: Double,
        calculatedSpeed: Double
    ) {
        viewModelScope.launch {
            val user = auth.currentUser ?: return@launch

            try {
                val token = user.getIdToken(true).await().token ?: return@launch

                val request = OnCallRequest(
                    data = SuspiciousSpeedRequest(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        calculatedSpeed = calculatedSpeed,
                        distance = distance.toDouble(),
                        timeDiff = timeDiffSeconds
                    )
                )

                val response = api.reportSuspiciousSpeed(
                    "Bearer $token",
                    request
                )

                if (response.isSuccessful && response.body()?.result?.success == true) {
                    Log.d(TAG, "✓ Velocidad sospechosa reportada al backend")
                } else {
                    Log.e(TAG, "❌ Error reportando velocidad: ${response.code()}")
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error enviando velocidad sospechosa: ${e.message}")
            }
        }
    }

    /**
     * Enviar pánico al backend
     */
    private fun reportPanicButtonToBackend(location: Location?, reason: String) {
        viewModelScope.launch {
            val user = auth.currentUser ?: return@launch

            try {
                val token = user.getIdToken(true).await().token ?: return@launch

                val request = OnCallRequest(
                    data = PanicButtonRequest(
                        latitude = location?.latitude ?: 0.0,
                        longitude = location?.longitude ?: 0.0,
                        reason = reason
                    )
                )

                val response = api.triggerPanicButton(
                    "Bearer $token",
                    request
                )

                if (response.isSuccessful && response.body()?.result?.success == true) {
                    Log.d(TAG, "✓ Pánico reportado al backend - Contactos notificados")
                } else {
                    Log.e(TAG, "❌ Error reportando pánico: ${response.code()}")
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error enviando pánico: ${e.message}")
            }
        }
    }

    fun clearAlert() {
        _alertMessage.value = null
    }
}