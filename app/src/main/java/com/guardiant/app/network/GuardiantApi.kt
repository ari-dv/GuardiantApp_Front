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
    ): Response<OnCallResultWrapper<Any>> // <- 'Any' para data flexible

    // ... (actualiza el resto si las usas) ...

}