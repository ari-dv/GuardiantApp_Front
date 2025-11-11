package com.guardiant.app.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.guardiant.app.R

// 1. Asegúrate de que herede de Fragment
class HomeFragment : Fragment() {

    // 2. Infla el layout XML correcto (fragment_home.xml)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    // Aquí iría la lógica de tu pantalla de inicio
}