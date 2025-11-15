package com.guardiant.app.security

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.guardiant.app.auth.UnlockActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Servicio que escucha cuando la pantalla se enciende
 * y muestra UnlockActivity sobre la pantalla de bloqueo del sistema
 */
class LockScreenService : Service() {

    companion object {
        private const val TAG = "LockScreenService"
    }

    private val screenReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (context == null || intent == null) return

            when (intent.action) {
                Intent.ACTION_SCREEN_ON -> {
                    Log.d(TAG, "📱 Pantalla encendida - Verificando si mostrar lock screen")
                    handleScreenOn(context)
                }
                Intent.ACTION_USER_PRESENT -> {
                    Log.d(TAG, "🔓 Usuario desbloqueó pantalla nativa")
                    handleUserPresent(context)
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "✅ LockScreenService iniciado")

        // Registrar receiver dinámicamente para escuchar eventos de pantalla
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_USER_PRESENT)
        }
        registerReceiver(screenReceiver, filter)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand llamado")
        return START_STICKY // Reiniciar si el sistema lo mata
    }

    private fun handleScreenOn(context: Context) {
        // Cuando la pantalla se enciende, aún está bloqueada
        Log.d(TAG, "Pantalla encendida, esperando desbloqueo del usuario")
    }

    private fun handleUserPresent(context: Context) {
        // Usuario acaba de desbloquear la pantalla nativa
        // Ahora mostramos nuestra pantalla de bloqueo
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val auth = FirebaseAuth.getInstance()
                val currentUser = auth.currentUser

                if (currentUser == null) {
                    Log.d(TAG, "Usuario no autenticado - No mostrar lock screen")
                    return@launch
                }

                // Verificar si el setup está completo
                val db = Firebase.firestore
                val userDoc = db.collection("users").document(currentUser.uid).get().await()

                if (!userDoc.exists()) {
                    Log.d(TAG, "Documento de usuario no existe")
                    return@launch
                }

                val setupData = userDoc.get("setup") as? Map<*, *>
                val setupCompleted = setupData?.get("completed") as? Boolean ?: false

                if (setupCompleted) {
                    Log.i(TAG, "✅ Setup completo - Mostrando UnlockActivity sobre pantalla de bloqueo")
                    launchUnlockActivity(context)
                } else {
                    Log.d(TAG, "Setup incompleto - No mostrar lock screen")
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error verificando usuario: ${e.message}", e)
            }
        }
    }

    private fun launchUnlockActivity(context: Context) {
        val intent = Intent(context, UnlockActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS or
                    Intent.FLAG_ACTIVITY_NO_HISTORY
        }
        context.startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(screenReceiver)
            Log.w(TAG, "❌ LockScreenService destruido")
        } catch (e: Exception) {
            Log.e(TAG, "Error al destruir servicio: ${e.message}")
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
