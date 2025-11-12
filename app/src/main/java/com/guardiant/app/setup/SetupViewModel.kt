package com.guardiant.app.setup

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.guardiant.app.network.AppConfig
import com.guardiant.app.network.GuardiantApi
import com.guardiant.app.network.OnCallRequest
import com.guardiant.app.network.ProtectionLevelRequest
import com.guardiant.app.network.ProtectedAppsRequest
import com.guardiant.app.network.SavePinsData
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

// --- ¡NUEVA CLASE DE DATOS! ---
// Modelo simple para la app instalada
data class InstalledAppInfo(
    val appName: String,
    val packageName: String,
    // val icon: Drawable // El ícono es más complejo, lo omitimos por ahora
)

class SetupViewModel : ViewModel() {

    private val api = GuardiantApi.create()
    private val auth = FirebaseAuth.getInstance()

    private val _savePinsSuccess = MutableLiveData<Boolean>()
    val savePinsSuccess: LiveData<Boolean> = _savePinsSuccess

    private val _saveAppsSuccess = MutableLiveData<Boolean>()
    val saveAppsSuccess: LiveData<Boolean> = _saveAppsSuccess

    private val _setupCompleteSuccess = MutableLiveData<Boolean>()
    val setupCompleteSuccess: LiveData<Boolean> = _setupCompleteSuccess

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    // --- ¡NUEVO! ---
    // LiveData para las apps que SÍ encontramos instaladas
    private val _foundApps = MutableLiveData<List<InstalledAppInfo>>()
    val foundApps: LiveData<List<InstalledAppInfo>> = _foundApps

    // --- Lógica de Escaneo ---

    /**
     * Escanea el dispositivo, filtra por la TARGET_LIST,
     * y publica los resultados en el LiveData 'foundApps'.
     */
    // Lista de apps BANCARIAS (Ejemplos de Perú)
    private val BANKING_PACKAGES = setOf(
        "com.bcp.bank.bcp", // BCP
        "pe.com.bn.app.bancodelanacion", //BANCO DE LA NACION
        "com.google.android.apps.walletnfcrel", //WALLET
        "com.google.commerce.tapandpay.android.wallet.WalletActivity",
        "com.bcp.innovacxion.yapeapp",       // Yape
        "pe.com.interbank.mobilebanking",   // Interbank
        "com.bbva.nxt_peru", // BBVA
        "pe.com.scotiabank.blpm.android.client",// Scotiabank
        "pe.com.scotiabank.blpm.android.client.host.shared.HostActivity",
        "com.paysafe.pagoefectivo",
        "com.paysafe.pagoefectivo.MainActivity",
        "pe.com.banbif.bancamovil", // BanBif
        "com.pichincha.pe.bancamovil" // Banco Pichincha

    )

    // Lista de OTRAS apps sensibles (Redes Sociales, Galerías)
    private val SENSITIVE_PACKAGES = setOf(
        "com.mercadolibre",
        "com.whatsapp",       // WhatsApp
        "com.facebook.katana", // Facebook
        "com.instagram.android", // Instagram
        "com.google.android.apps.photos", // Google Photos
        "com.google.android.apps.docs", //drive
        "com.google.android.gm",//gmail
        "com.sec.android.gallery3d" // Galería samsuung
    )

    // --- TU CONTROL TOTAL ESTÁ AQUÍ ---
    // Unimos ambas listas para el escaneo.
    // SI QUIERES SOLO BANCARIAS, CAMIA ESTO A:
    // private val TARGET_PACKAGES = BANKING_PACKAGES
    private val TARGET_PACKAGES = BANKING_PACKAGES+SENSITIVE_PACKAGES

    fun loadInstalledApps(packageManager: PackageManager) {
        viewModelScope.launch(Dispatchers.IO) { // Tarea pesada, usar hilo IO
            val installedApps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
            val foundTargetApps = mutableListOf<InstalledAppInfo>()

            for (appInfo in installedApps) {
                // Si la app está en nuestra lista de objetivos...
                if (TARGET_PACKAGES.contains(appInfo.packageName)) {
                    // Y no es una app del sistema (opcional, pero bueno)
                    if ((appInfo.flags and ApplicationInfo.FLAG_SYSTEM) == 0) {
                        foundTargetApps.add(
                            InstalledAppInfo(
                                appName = appInfo.loadLabel(packageManager).toString(),
                                packageName = appInfo.packageName
                            )
                        )
                    }
                }
            }
            // Publicamos el resultado en el hilo principal
            withContext(Dispatchers.Main) {
                if (foundTargetApps.isEmpty()) {
                    _errorMessage.value = "No se encontraron apps bancarias (BCP, Yape, etc.) instaladas."
                }
                _foundApps.value = foundTargetApps
            }
        }
    }


    // --- Funciones de API (Backend) ---

    private suspend fun getFreshAuthHeader(): String? {
        val user = auth.currentUser
        if (user == null) {
            _errorMessage.postValue("Usuario no autenticado. Inicie sesión de nuevo.")
            return null
        }
        try {
            val idToken = user.getIdToken(true).await().token
            if (idToken == null) throw Exception("Token de ID nulo después de obtenerlo.")
            return "Bearer $idToken"
        } catch(e: Exception) {
            _errorMessage.postValue("Error al obtener token de seguridad: ${e.message}")
            return null
        }
    }

    /**
     * Paso 1: Guardar PINs
     */
    fun savePins(normalPin: String, securityPin: String) {
        viewModelScope.launch {
            val authHeader = getFreshAuthHeader() ?: return@launch
            try {
                val data = SavePinsData(normalPin, securityPin)
                val request = OnCallRequest(data = data)
                val response = api.savePins(authHeader, request)
                val result = response.body()?.result

                if (response.isSuccessful && result?.success == true) {
                    _savePinsSuccess.value = true
                } else {
                    _errorMessage.value = result?.message ?: "Error al guardar PINs (Código: ${response.code()})"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Excepción: ${e.message}"
            }
        }
    }

    /**
     * Paso 2: Guardar Apps
     */
    fun saveProtectedApps(apps: List<AppConfig>) {
        viewModelScope.launch {
            val authHeader = getFreshAuthHeader() ?: return@launch
            try {
                val data = ProtectedAppsRequest(apps = apps)
                val request = OnCallRequest(data = data)
                val response = api.saveProtectedApps(authHeader, request)
                val result = response.body()?.result

                if (response.isSuccessful && result?.success == true) {
                    _saveAppsSuccess.value = true
                } else {
                    _errorMessage.value = result?.message ?: "Error al guardar Apps"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Excepción: ${e.message}"
            }
        }
    }

    /**
     * Paso 3: Finalizar Setup
     */
    fun completeSetup(level: String = "extreme") {
        viewModelScope.launch {
            val authHeader = getFreshAuthHeader() ?: return@launch
            try {
                val data = ProtectionLevelRequest(level = level)
                val request = OnCallRequest(data = data)
                val response = api.setProtectionLevel(authHeader, request)
                val result = response.body()?.result

                if (response.isSuccessful && result?.success == true) {
                    _setupCompleteSuccess.value = true
                } else {
                    _errorMessage.value = result?.message ?: "Error al finalizar setup"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Excepción: ${e.message}"
            }
        }
    }


    fun clearErrorMessage() {
        _errorMessage.value = null
    }
}