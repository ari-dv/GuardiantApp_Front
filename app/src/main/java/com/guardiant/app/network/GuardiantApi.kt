package com.guardiant.app.network

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

// ---------------------------------------------------------------------------------
// 1. ESTRUCTURAS DE DATOS (REQUEST/RESPONSE)
// ---------------------------------------------------------------------------------

/**
 * Envoltura genérica para peticiones onCall: {"data": {...}}
 */
data class OnCallRequest<T>(@SerializedName("data") val data: T)

/**
 * ¡LA CORRECCIÓN!
 * Envoltura genérica para respuestas onCall: {"result": {...}}
 */
data class OnCallResultWrapper<T>(@SerializedName("result") val result: T)

// --- Clases de Respuesta Específicas (lo que va DENTRO de "result") ---

data class GenericResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String
)

data class VerifyPinResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("mode") val mode: String? = null
)

// (Podríamos añadir data classes para getUserConfig, etc., pero GenericResponse funciona)


// --- Clases de Petición Específicas (lo que va DENTRO de "data") ---

data class RegisterRequest(val email: String, val password: String)
data class SavePinsData(val normalPin: String, val securityPin: String)
data class VerifyPinRequest(val pin: String)
data class ChangePinsRequest(val currentPin: String, val newNormalPin: String, val newSecurityPin: String)
data class AppConfig(val appName: String, val packageName: String, val icon: String? = null)
data class ProtectedAppsRequest(val apps: List<AppConfig>)
data class ProtectedAppsResponse(
    @SerializedName("apps") val apps: List<AppConfig>
)
data class ProtectionLevelRequest(val level: String)
data class Contact(val name: String, val phone: String)
data class UpdateProfileRequest(
    val displayName: String? = null,
    val phoneNumber: String? = null,
    val emergencyContacts: List<Contact>? = null
)
data class SecurityEventRequest(val eventType: String, val details: Map<String, Any>)
data class SuddenMovementRequest(val details: Map<String, Any>)
data class FcmTokenRequest(val token: String)
object EmptyRequest

data class SetupStatusData(
    @SerializedName("completed") val completed: Boolean = false,
    @SerializedName("pinsConfigured") val pinsConfigured: Boolean = false,
    @SerializedName("appsConfigured") val appsConfigured: Boolean = false
)
data class SetupStatusResponseData(
    @SerializedName("setup") val setup: SetupStatusData = SetupStatusData()
    // (puedes añadir currentMode, security, etc. si los necesitas aquí)
)
// Esta es la envoltura de respuesta para getSetupStatus
data class GetSetupStatusResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: SetupStatusResponseData
)

// Requests para Security Events

// Requests para Security Events

data class AbnormalMovementRequest(
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("accelerationValue") val accelerationValue: Float
)

data class SuspiciousSpeedRequest(
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("calculatedSpeed") val calculatedSpeed: Double,
    @SerializedName("distance") val distance: Double? = null,
    @SerializedName("timeDiff") val timeDiff: Double? = null
)

data class PanicButtonRequest(
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("reason") val reason: String? = null
)

data class LockDeviceRequest(
    @SerializedName("alertId") val alertId: String
)

data class WipeDeviceRequest(
    @SerializedName("alertId") val alertId: String
)

data class DeactivateSecurityRequest(
    @SerializedName("alertId") val alertId: String
)

data class SecurityAlert(
    @SerializedName("id") val id: String,
    @SerializedName("type") val type: String,
    @SerializedName("timestamp") val timestamp: Long,
    @SerializedName("status") val status: String,
    @SerializedName("details") val details: Map<String, Any>
)

data class SecurityAlertsResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: AlertsData
)

data class AlertsData(
    @SerializedName("alerts") val alerts: List<SecurityAlert>
)

// ---------------------------------------------------------------------------------
// 2. INTERFAZ DE LA API (Definición de Endpoints)
// ¡TODAS las respuestas ahora usan OnCallResultWrapper!
// ---------------------------------------------------------------------------------
interface GuardiantApi {

    companion object {
        private const val BASE_URL = "https://us-central1-guardiant-ea59f.cloudfunctions.net/"
        fun create(): GuardiantApi {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(GuardiantApi::class.java)
        }
    }

    // --- FUNCIONES PÚBLICAS ---
    @POST("registerUser")
    suspend fun registerUser(
        @Body request: OnCallRequest<RegisterRequest>
    ): Response<OnCallResultWrapper<GenericResponse>> // <- Corregido


    // --- FUNCIONES PROTEGIDAS ---
    @POST("savePins")
    suspend fun savePins(
        @Header("Authorization") token: String,
        @Body request: OnCallRequest<SavePinsData>
    ): Response<OnCallResultWrapper<GenericResponse>> // <- Corregido

    @POST("verifyPin")
    suspend fun verifyPin(
        @Header("Authorization") token: String,
        @Body request: OnCallRequest<VerifyPinRequest>
    ): Response<OnCallResultWrapper<VerifyPinResponse>> // <- Corregido

    @POST("saveProtectedApps")
    suspend fun saveProtectedApps(
        @Header("Authorization") token: String,
        @Body request: OnCallRequest<ProtectedAppsRequest>
    ): Response<OnCallResultWrapper<GenericResponse>> // <- Corregido

    @POST("getProtectedApps")
    suspend fun getProtectedApps(
        @Header("Authorization") token: String,
        @Body request: OnCallRequest<EmptyRequest> = OnCallRequest(EmptyRequest)
    ): Response<OnCallResultWrapper<ProtectedAppsResponse>>

    @POST("setProtectionLevel")
    suspend fun setProtectionLevel(
        @Header("Authorization") token: String,
        @Body request: OnCallRequest<ProtectionLevelRequest>
    ): Response<OnCallResultWrapper<GenericResponse>> // <- Corregido

    // ... (El resto de funciones también deben usar OnCallResultWrapper) ...

    // Ejemplo para funciones sin datos de respuesta específicos
    @POST("getUserConfig")
    suspend fun getUserConfig(
        @Header("Authorization") token: String,
        @Body request: OnCallRequest<EmptyRequest> = OnCallRequest(EmptyRequest)
    ): Response<OnCallResultWrapper<Any>> // <- 'Any' para data flexible

    @POST("getSetupStatus")
    suspend fun getSetupStatus(
        @Header("Authorization") token: String,
        @Body request: OnCallRequest<EmptyRequest> = OnCallRequest(EmptyRequest)
    ): Response<GetSetupStatusResponse> // <-- Ahora usa la clase de respuesta específica

    @POST("reportAbnormalMovement")
    suspend fun reportAbnormalMovement(
        @Header("Authorization") token: String,
        @Body request: OnCallRequest<AbnormalMovementRequest>
    ): Response<OnCallResultWrapper<GenericResponse>>

    @POST("reportSuspiciousSpeed")
    suspend fun reportSuspiciousSpeed(
        @Header("Authorization") token: String,
        @Body request: OnCallRequest<SuspiciousSpeedRequest>
    ): Response<OnCallResultWrapper<GenericResponse>>

    @POST("triggerPanicButton")
    suspend fun triggerPanicButton(
        @Header("Authorization") token: String,
        @Body request: OnCallRequest<PanicButtonRequest>
    ): Response<OnCallResultWrapper<GenericResponse>>

// ENDPOINTS DE CONTROL REMOTO

    @POST("lockDeviceRemotely")
    suspend fun lockDeviceRemotely(
        @Header("Authorization") token: String,
        @Body request: OnCallRequest<LockDeviceRequest>
    ): Response<OnCallResultWrapper<GenericResponse>>

    @POST("wipeDeviceRemotely")
    suspend fun wipeDeviceRemotely(
        @Header("Authorization") token: String,
        @Body request: OnCallRequest<WipeDeviceRequest>
    ): Response<OnCallResultWrapper<GenericResponse>>

    @POST("deactivateSecurityMode")
    suspend fun deactivateSecurityMode(
        @Header("Authorization") token: String,
        @Body request: OnCallRequest<DeactivateSecurityRequest>
    ): Response<OnCallResultWrapper<GenericResponse>>

    @POST("getSecurityAlerts")
    suspend fun getSecurityAlerts(
        @Header("Authorization") token: String,
        @Body request: OnCallRequest<EmptyRequest> = OnCallRequest(EmptyRequest)
    ): Response<OnCallResultWrapper<SecurityAlertsResponse>>

}