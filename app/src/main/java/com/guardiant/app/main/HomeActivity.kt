package com.guardiant.app.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
// ¡Importaciones de Navegación!
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.guardiant.app.R
import com.guardiant.app.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // --- ¡ESTA ES LA LÓGICA DE NAVEGACIÓN REAL! ---

        // 1. Encuentra el "host" (el contenedor de fragmentos)
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment

        // 2. Encuentra el "controlador" que vive dentro del host
        val navController = navHostFragment.navController

        // 3. Conecta la barra de pestañas (BottomNavigationView) con el controlador
        // Esto maneja automáticamente los clics en las pestañas
        binding.bottomNavigationView.setupWithNavController(navController)
    }
}