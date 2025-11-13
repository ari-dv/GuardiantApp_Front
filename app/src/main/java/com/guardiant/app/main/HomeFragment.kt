package com.guardiant.app.main

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.guardiant.app.R
import com.guardiant.app.auth.LoginActivity
import com.guardiant.app.network.GuardiantApi
import com.guardiant.app.network.OnCallRequest
import com.guardiant.app.network.PanicButtonRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class HomeFragment : Fragment() {

    private val auth = FirebaseAuth.getInstance()
    private val api = GuardiantApi.create()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentUser = auth.currentUser

        // Mostrar bienvenida
        val textViewWelcome = view.findViewById<TextView>(R.id.textViewWelcome)
        textViewWelcome.text = "👋 ${currentUser?.email ?: "Usuario"}"

        // Botón Cerrar Sesión (arriba a la derecha)
        val buttonLogout = view.findViewById<Button>(R.id.buttonLogout)
        buttonLogout.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Cerrar Sesión")
                .setMessage("¿Estás seguro de que deseas cerrar sesión?")
                .setPositiveButton("Sí") { _, _ ->
                    auth.signOut()
                    Toast.makeText(requireContext(), "Sesión cerrada", Toast.LENGTH_SHORT).show()
                    val intent = Intent(requireContext(), LoginActivity::class.java)
                    startActivity(intent)
                    requireActivity().finishAffinity()
                }
                .setNegativeButton("No", null)
                .show()
        }

        // Botón Pánico Rápido
        val buttonQuickPanic = view.findViewById<Button>(R.id.buttonQuickPanic)
        buttonQuickPanic.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("🚨 BOTÓN DE PÁNICO")
                .setMessage("¿Activar alerta de emergencia AHORA?")
                .setPositiveButton("SÍ") { _, _ ->
                    triggerPanic()
                }
                .setNegativeButton("No", null)
                .show()
        }
    }

    private fun triggerPanic() {
        Toast.makeText(requireContext(), "🚨 Activando pánico...", Toast.LENGTH_LONG).show()

        CoroutineScope(Dispatchers.IO).launch {
            val user = auth.currentUser ?: return@launch

            try {
                val token = user.getIdToken(true).await().token ?: return@launch

                val request = OnCallRequest(
                    data = PanicButtonRequest(
                        latitude = 0.0,
                        longitude = 0.0,
                        reason = "Usuario presionó botón de pánico desde HomeFragment"
                    )
                )

                val response = api.triggerPanicButton("Bearer $token", request)

                if (response.isSuccessful) {
                    requireActivity().runOnUiThread {
                        Toast.makeText(
                            requireContext(),
                            "✅ Pánico activado - Contactos notificados",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}