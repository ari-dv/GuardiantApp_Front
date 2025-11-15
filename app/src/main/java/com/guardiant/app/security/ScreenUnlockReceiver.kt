package com.guardiant.app.security

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
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
 * Receiver que se activa cuando el usuario desbloquea el dispositivo
 * Lanza la pantalla de bloqueo de Guardiant si el setup está completo
 */
class ScreenUnlockReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "ScreenUnlockReceiver"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        when (intent.action) {
            Intent.ACTION_USER_PRESENT -> {
                Log.d(TAG, "📱 Dispositivo desbloqueado - Verificando estado del usuario")
                handleDeviceUnlock(context)
            }
            Intent.ACTION_SCREEN_ON -> {
                Log.d(TAG, "🔆 Pantalla encendida")
                // Podríamos mostrar lock screen también aquí si lo deseamos
            }
        }
    }

    private fun handleDeviceUnlock(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val auth = FirebaseAuth.getInstance()
                val currentUser = auth.currentUser

                if (currentUser == null) {
                    Log.d(TAG, "Usuario no autenticado - No se muestra lock screen")
                    return@launch
                }

                // Verificar si el setup está completo
                val db = Firebase.firestore
                val userDoc = db.collection("users").document(currentUser.uid).get().await()

                if (!userDoc.exists()) {
                    Log.d(TAG, "Documento de usuario no existe - No se muestra lock screen")
                    return@launch
                }

                val setupData = userDoc.get("setup") as? Map<*, *>
                val setupCompleted = setupData?.get("completed") as? Boolean ?: false

                if (setupCompleted) {
                    Log.i(TAG, "✅ Setup completo - Lanzando UnlockActivity")
                    launchUnlockActivity(context)
                } else {
                    Log.d(TAG, "Setup incompleto - No se muestra lock screen")
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error verificando estado del usuario: ${e.message}", e)
            }
        }
    }

    private fun launchUnlockActivity(context: Context) {
        val intent = Intent(context, UnlockActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
        }
        context.startActivity(intent)
    }
}
