package com.guardiant.app.main

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.guardiant.app.databinding.ItemAlertBinding
import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Data class (si no lo tienes ya en GuardiantApi.kt)
data class SecurityAlert(
    val id: String,
    val type: String,
    val timestamp: Long,
    val status: String,
    val details: Map<String, Any>
)

class AlertsAdapter(private var alerts: MutableList<SecurityAlert> = mutableListOf()) :
    RecyclerView.Adapter<AlertsAdapter.AlertViewHolder>() {

    inner class AlertViewHolder(private val binding: ItemAlertBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(alert: SecurityAlert) {
            binding.textViewEventType.text = alert.type
            binding.textViewStatus.text = "Status: ${alert.status}"
            binding.textViewTimestamp.text = formatTime(alert.timestamp)

            // Detalles
            val details = alert.details
            val lat = details["latitude"] as? Double ?: 0.0
            val lng = details["longitude"] as? Double ?: 0.0
            binding.textViewLocation.text = "📍 $lat, $lng"

            // Color según estado
            val color = when (alert.status) {
                "active" -> Color.RED
                "resolved" -> Color.GREEN
                else -> Color.GRAY
            }
            binding.cardView.setCardBackgroundColor(color)
            binding.cardView.alpha = 0.7f
        }

        private fun formatTime(timestamp: Long): String {
            val sdf = SimpleDateFormat("HH:mm:ss dd/MM", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlertViewHolder {
        val binding = ItemAlertBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AlertViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AlertViewHolder, position: Int) {
        holder.bind(alerts[position])
    }

    override fun getItemCount() = alerts.size

    fun updateAlerts(newAlerts: List<SecurityAlert>) {
        alerts.clear()
        alerts.addAll(newAlerts)
        notifyDataSetChanged()
    }
}