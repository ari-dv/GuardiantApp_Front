package com.guardiant.app.security

import android.app.Service
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlin.math.sqrt

class SensorService : Service(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    val abnormalMovement = MutableLiveData<Boolean>()

    // Umbral de sensibilidad (m/s²)
    // Valor normal: ~9.8 (gravedad)
    // Caída/golpe: > 25
    private val ABNORMAL_THRESHOLD = 25f

    private val TAG = "SensorService"

    // Callback listener (se asigna desde HomeActivity)
    private var abnormalMovementListener: ((Float, Location?) -> Unit)? = null

    fun setAbnormalMovementListener(listener: (Float, Location?) -> Unit) {
        abnormalMovementListener = listener
    }

    override fun onCreate() {
        super.onCreate()
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME)
            Log.d(TAG, "✓ Acelerómetro registrado")
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event != null && event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            // Obtener valores X, Y, Z
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            // Calcular magnitud (sqrt(x² + y² + z²))
            val magnitude = sqrt(x * x + y * y + z * z)

            // Restar gravedad (~9.8) para obtener aceleración actual
            val acceleration = magnitude - 9.8f

            if (acceleration > ABNORMAL_THRESHOLD) {
                Log.w(TAG, "⚠️ MOVIMIENTO ANORMAL DETECTADO: ${acceleration}m/s²")
                abnormalMovement.value = true

                // Obtener ubicación actual y notificar
                try {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                        if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)
                            == android.content.pm.PackageManager.PERMISSION_GRANTED) {

                            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                                // Notificar al listener (SecurityViewModel)
                                abnormalMovementListener?.invoke(acceleration, location)
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error obteniendo ubicación: ${e.message}")
                    // Enviar sin ubicación si hay error
                    abnormalMovementListener?.invoke(acceleration, null)
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
        Log.d(TAG, "Sensor Service destruido")
    }
}