package com.guardiant.app.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.guardiant.app.databinding.FragmentSettingsBinding
import com.guardiant.app.security.SecurityViewModel
import com.google.firebase.auth.FirebaseAuth
import com.guardiant.app.network.GuardiantApi
import com.guardiant.app.network.OnCallRequest
import com.guardiant.app.network.PanicButtonRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SettingsFragment : Fragment() {

    private lateinit var binding: FragmentSettingsBinding
    private val api = GuardiantApi.create()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // ✅ AQUÍ ESTÁ LA CLAVE: Inflar el binding correctamente
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ⚠️ BOTÓN CRÍTICO: Pánico Manual
        binding.buttonEmergencyLock.setOnClickListener {
            handlePanicButton()
        }

        // Botón Cambiar PIN
        binding.buttonChangePins.setOnClickListener {
            Toast.makeText(requireContext(), "Cambiar PIN - TODO", Toast.LENGTH_SHORT).show()
        }

        // ⭐ BOTÓN DE PRUEBA: Ver pantalla de desbloqueo
        binding.textViewMonitoringStatus.setOnClickListener {
            val intent = android.content.Intent(requireContext(), com.guardiant.app.auth.UnlockActivity::class.java)
            startActivity(intent)
        }

        // Contactos de emergencia
        binding.buttonEmergencyContacts.setOnClickListener {
            Toast.makeText(requireContext(), "Contactos de emergencia - TODO", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handlePanicButton() {
        AlertDialog.Builder(requireContext())
            .setTitle("🚨 BOTÓN DE PÁNICO")
            .setMessage("¿Activar alerta de emergencia AHORA?\n\nSe notificará a todos tus contactos de emergencia.")
            .setPositiveButton("SÍ, ACTIVAR") { _, _ ->
                triggerPanic()
            }
            .setNegativeButton("Cancelar", null)
            .setCancelable(false)
            .show()
    }

    private fun triggerPanic() {
        Toast.makeText(requireContext(), "🚨 Activando alerta de pánico...", Toast.LENGTH_LONG).show()

        lifecycleScope.launch(Dispatchers.IO) {
            val user = auth.currentUser ?: return@launch

            try {
                val token = user.getIdToken(true).await().token ?: return@launch

                val request = OnCallRequest(
                    data = PanicButtonRequest(
                        latitude = 0.0, // TODO: Obtener GPS
                        longitude = 0.0,
                        reason = "Usuario presionó botón de pánico desde SettingsFragment"
                    )
                )

                val response = api.triggerPanicButton(
                    "Bearer $token",
                    request
                )

                if (response.isSuccessful) {
                    requireActivity().runOnUiThread {
                        Toast.makeText(
                            requireContext(),
                            "✅ Pánico activado - Contactos notificados",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    requireActivity().runOnUiThread {
                        Toast.makeText(
                            requireContext(),
                            "❌ Error: ${response.code()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

            } catch (e: Exception) {
                requireActivity().runOnUiThread {
                    Toast.makeText(
                        requireContext(),
                        "❌ Error: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}