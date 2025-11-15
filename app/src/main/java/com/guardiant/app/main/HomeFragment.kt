package com.guardiant.app.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.guardiant.app.R
import com.guardiant.app.auth.LoginActivity
import com.guardiant.app.network.AppConfig
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

        // Cargar apps protegidas
        loadProtectedApps(view)
    }

    private fun loadProtectedApps(view: View) {
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBarApps)
        val appsContainer = view.findViewById<LinearLayout>(R.id.appsContainer)
        val textAppsCount = view.findViewById<TextView>(R.id.textAppsCount)

        progressBar.visibility = View.VISIBLE
        
        lifecycleScope.launch {
            try {
                val user = auth.currentUser
                if (user == null) {
                    showError("Usuario no autenticado")
                    progressBar.visibility = View.GONE
                    return@launch
                }

                val token = user.getIdToken(false).await().token
                if (token == null) {
                    showError("No se pudo obtener el token")
                    progressBar.visibility = View.GONE
                    return@launch
                }

                val response = api.getProtectedApps("Bearer $token")
                
                progressBar.visibility = View.GONE

                if (response.isSuccessful && response.body() != null) {
                    val apps = response.body()!!.result.apps
                    displayProtectedApps(apps, appsContainer, textAppsCount)
                } else {
                    showError("Error al cargar apps: ${response.code()}")
                    displayProtectedApps(emptyList(), appsContainer, textAppsCount)
                }
            } catch (e: Exception) {
                progressBar.visibility = View.GONE
                Log.e("HomeFragment", "Error cargando apps", e)
                displayProtectedApps(emptyList(), appsContainer, textAppsCount)
            }
        }
    }

    private fun displayProtectedApps(apps: List<AppConfig>, container: LinearLayout, countText: TextView) {
        container.removeAllViews()
        
        if (apps.isEmpty()) {
            countText.text = "Apps protegidas: 0"
            val noAppsText = TextView(requireContext()).apply {
                text = "No tienes apps configuradas aún"
                textSize = 14f
                setPadding(16, 16, 16, 16)
                setTextColor(resources.getColor(android.R.color.darker_gray, null))
            }
            container.addView(noAppsText)
        } else {
            countText.text = "Apps protegidas: ${apps.size}"
            
            apps.forEach { app ->
                val appView = TextView(requireContext()).apply {
                    text = "🔒 ${app.appName}"
                    textSize = 16f
                    setPadding(24, 16, 24, 16)
                    setBackgroundResource(android.R.drawable.dialog_holo_light_frame)
                    val params = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    params.setMargins(0, 8, 0, 8)
                    layoutParams = params
                }
                container.addView(appView)
            }
        }
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
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