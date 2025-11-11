package com.guardiant.app.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.guardiant.app.R

class ActivityFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Infla el layout fragment_activity.xml
        return inflater.inflate(R.layout.fragment_activity, container, false)
    }

    // Aquí iría la lógica para llamar a getActivityFeed
}