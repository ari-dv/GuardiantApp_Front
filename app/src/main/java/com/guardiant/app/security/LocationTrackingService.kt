package com.guardiant.app.security

import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import kotlin.math.pow
import kotlin.math.sqrt

class LocationTrackingService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    val suspiciousSpeed = MutableLiveData<Boolean>()
    val currentLocation = MutableLiveData<Location>()

    private var lastLocation: Location? = null
    private var lastTimestamp: Long = 0

    // Umbral: >100m en 5 segundos = velocidad imposible
    private val SUSPICIOUS_DISTANCE_M = 100f
    private val SUSPICIOUS_TIME_S = 5

    private val TAG = "LocationTrackingService"

    // Callback listener (se asigna desde HomeActivity)
    private var suspiciousSpeedListener: ((Location, Float, Double, Double) -> Unit)? = null

    fun setSuspiciousSpeedListener(listener: (Location, Float, Double, Double) -> Unit) {
        suspiciousSpeedListener = listener
    }

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        setupLocationUpdates()
    }

    private fun setupLocationUpdates() {
        val locationRequest = LocationRequest.create().apply {
            interval = 10000 // Cada 10 segundos
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    currentLocation.value = location
                    checkSuspiciousMovement(location)
                }
            }
        }

        try {
            // ✅ AQUÍ: Verificar permisos en runtime
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)
                    != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    Log.w(TAG, "⚠️ Permiso ACCESS_FINE_LOCATION denegado")
                    return
                }
            }

            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                null
            )
            Log.d(TAG, "✓ GPS iniciado")
        } catch (e: SecurityException) {
            Log.e(TAG, "Permiso de GPS denegado: ${e.message}")
        }
    }

    private fun checkSuspiciousMovement(currentLoc: Location) {
        if (lastLocation == null) {
            lastLocation = currentLoc
            lastTimestamp = System.currentTimeMillis()
            return
        }

        // Calcular distancia en metros
        val distance = lastLocation!!.distanceTo(currentLoc)

        // Calcular tiempo en segundos
        val currentTime = System.currentTimeMillis()
        val timeDiffSeconds = (currentTime - lastTimestamp) / 1000.0

        // Calcular velocidad en m/s
        val velocityMs = distance / timeDiffSeconds

        Log.d(TAG, "Distancia: ${distance}m, Tiempo: ${timeDiffSeconds}s, Velocidad: ${velocityMs}m/s")

        // Si viajó >100m en <5 segundos = IMPOSIBLE (excepto en avión)
        if (distance > SUSPICIOUS_DISTANCE_M && timeDiffSeconds < SUSPICIOUS_TIME_S) {
            Log.e(TAG, "🚨 VELOCIDAD IMPOSIBLE: ${velocityMs}m/s")
            suspiciousSpeed.value = true

            // Notificar al listener (SecurityViewModel)
            suspiciousSpeedListener?.invoke(currentLoc, distance, timeDiffSeconds, velocityMs)
        }

        lastLocation = currentLoc
        lastTimestamp = currentTime
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
        Log.d(TAG, "LocationTrackingService destruido")
    }
}