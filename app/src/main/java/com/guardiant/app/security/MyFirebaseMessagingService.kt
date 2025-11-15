package com.guardiant.app.security

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val TAG = "MyFirebaseMsgService"
    private lateinit var coercionManager: CoercionStateManager

    /**
     * Se llama cuando un nuevo token FCM es generado (al instalar la app
     * o al limpiar datos).
     * Debes enviar este token a tu backend para el bloqueo remoto.
     */
    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")

        // TODO: Enviar este token a tu backend (a la función 'registerDeviceToken')
        // (Esto debe hacerse después de que el usuario inicie sesión)
        // Ejemplo:
        // CoroutineScope(Dispatchers.IO).launch {
        //     val api = GuardiantApi.create()
        //     val userToken = "AQUI_VA_EL_TOKEN_DE_AUTH_DEL_USUARIO"
        //     api.registerDeviceToken("Bearer $userToken", OnCallRequest(FcmTokenRequest(token)))
        // }
    }

    /**
     * Se llama cuando la app recibe un mensaje (comando) de tu backend,
     * (solo si la app está en primer plano).
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "From: ${remoteMessage.from}")

        // Inicializar CoercionStateManager
        coercionManager = CoercionStateManager.getInstance(this)

        // Revisa si el mensaje tiene datos (el comando de bloqueo)
        remoteMessage.data.isNotEmpty().let {
            Log.d(TAG, "Message data payload: " + remoteMessage.data)

            // Esta es la lógica que implementamos en 'alerts.ts'
            when (remoteMessage.data["command"]) {
                "unlock_coercion_mode" -> {
                    Log.i(TAG, "✅ Comando de DESBLOQUEO DE COERCIÓN recibido")
                    handleUnlockCoercionMode()
                }
                "BLOCK" -> {
                    Log.w(TAG, "¡¡Comando de BLOQUEO REMOTO recibido!!")
                    // TODO: Aquí debes iniciar tu Servicio de Accesibilidad
                    // o la lógica de bloqueo de la app.
                    // Ejemplo:
                    // val intent = Intent(this, AccessibilityLockService::class.java)
                    // startService(intent)
                }
                "WIPE" -> {
                    Log.e(TAG, "¡¡Comando de BORRADO REMOTO recibido!!")
                    // TODO: Aquí debes activar el Administrador de Dispositivo
                    // para borrar los datos.
                }
            }
        }

        // Revisa si el mensaje tiene una notificación (no lo usamos, pero es bueno saberlo)
        remoteMessage.notification?.let {
            Log.d(TAG, "Message Notification Body: ${it.body}")
        }
    }

    /**
     * Maneja el comando de desbloqueo de coerción
     * Desactiva el modo de coerción para que las apps vuelvan a ser accesibles
     */
    private fun handleUnlockCoercionMode() {
        try {
            coercionManager.disableCoercionMode()
            Log.i(TAG, "✅ Modo de coerción desactivado exitosamente")
            
            // Aquí podrías mostrar una notificación al usuario
            // o enviar confirmación al backend
        } catch (e: Exception) {
            Log.e(TAG, "Error desactivando modo de coerción: ${e.message}", e)
        }
    }
}