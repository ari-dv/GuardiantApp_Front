package com.guardiant.app.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.guardiant.app.R

class SettingsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Infla el layout fragment_settings.xml
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    // Aquí iría la lógica para mostrar los botones de Ajustes
}