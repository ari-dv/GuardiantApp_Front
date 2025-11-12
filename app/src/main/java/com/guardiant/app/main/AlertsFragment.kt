package com.guardiant.app.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.guardiant.app.databinding.FragmentAlertsBinding
import com.guardiant.app.setup.SetupViewModel

class AlertsFragment : Fragment() {

    private lateinit var binding: FragmentAlertsBinding
    private val viewModel: SetupViewModel by viewModels()
    private lateinit var alertsAdapter: AlertsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAlertsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        loadSecurityAlerts()
    }

    private fun setupRecyclerView() {
        binding.recyclerViewAlerts.layoutManager = LinearLayoutManager(context)
        alertsAdapter = AlertsAdapter(mutableListOf())
        binding.recyclerViewAlerts.adapter = alertsAdapter
    }

    private fun loadSecurityAlerts() {
        binding.progressBar.visibility = View.VISIBLE

        binding.recyclerViewAlerts.postDelayed({
            binding.progressBar.visibility = View.GONE

            // Datos de ejemplo con SecurityAlert LOCAL
            val dummyAlerts = listOf(
                SecurityAlert(
                    id = "alert1",
                    type = "COERCION",
                    timestamp = System.currentTimeMillis(),
                    status = "active",
                    details = mapOf(
                        "latitude" to -6.5,
                        "longitude" to -76.1,
                        "reason" to "PIN de seguridad usado"
                    )
                )
            )

            alertsAdapter.updateAlerts(dummyAlerts)
        }, 1000)
    }
}