package com.example.spottivo.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.spottivo.R
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MapFragment : Fragment() {

    private lateinit var fabMyLocation: FloatingActionButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupViews(view)
        setupMapControls()
    }

    private fun setupViews(view: View) {
        fabMyLocation = view.findViewById(R.id.fab_my_location)
    }

    private fun setupMapControls() {
        fabMyLocation.setOnClickListener {
            // TODO: Implement location functionality
            // This will center the map on user's current location
        }
    }
}