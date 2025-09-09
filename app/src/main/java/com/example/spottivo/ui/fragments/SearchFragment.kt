package com.example.spottivo.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.spottivo.R
import com.example.spottivo.ui.adapters.PopularPlacesAdapter
import com.example.spottivo.ui.models.PopularPlace

class SearchFragment : Fragment() {

    private lateinit var searchEditText: EditText
    private lateinit var popularPlacesRecyclerView: RecyclerView
    private lateinit var offersRecyclerView: RecyclerView
    private lateinit var popularPlacesAdapter: PopularPlacesAdapter
    private lateinit var offersAdapter: PopularPlacesAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupViews(view)
        setupRecyclerViews()
        loadPopularPlaces()
        loadOffers()
    }

    private fun setupViews(view: View) {
        searchEditText = view.findViewById(R.id.et_search)
        popularPlacesRecyclerView = view.findViewById(R.id.rv_popular_places)
        offersRecyclerView = view.findViewById(R.id.rv_offers)
    }

    private fun setupRecyclerViews() {
        // Setup Popular Places RecyclerView
        popularPlacesAdapter = PopularPlacesAdapter { place ->
            // Handle place click
            // TODO: Navigate to place details
        }
        
        popularPlacesRecyclerView.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = popularPlacesAdapter
        }

        // Setup Offers RecyclerView
        offersAdapter = PopularPlacesAdapter { place ->
            // Handle offer click
            // TODO: Navigate to place details
        }
        
        offersRecyclerView.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = offersAdapter
        }
    }

    private fun loadPopularPlaces() {
        val popularPlaces = listOf(
            PopularPlace(
                id = "1",
                name = "Padel - Colina",
                description = "50.000 por hora",
                distance = "2.5 km",
                imageUrl = "https://example.com/padel1.jpg"
            ),
            PopularPlace(
                id = "2",
                name = "Futbol - Colina",
                description = "50.000 por hora",
                distance = "3.1 km",
                imageUrl = "https://example.com/futbol1.jpg"
            ),
            PopularPlace(
                id = "3",
                name = "Golf - Norte",
                description = "80.000 por hora",
                distance = "4.2 km",
                imageUrl = "https://example.com/golf1.jpg"
            )
        )
        
        popularPlacesAdapter.submitList(popularPlaces)
    }

    private fun loadOffers() {
        val offers = listOf(
            PopularPlace(
                id = "4",
                name = "Tenis - Salitre",
                description = "30.000 por hora",
                distance = "1.8 km",
                imageUrl = "https://example.com/tenis1.jpg"
            ),
            PopularPlace(
                id = "5",
                name = "Bolos - Berlín",
                description = "50.000 por hora",
                distance = "5.2 km",
                imageUrl = "https://example.com/bolos1.jpg"
            ),
            PopularPlace(
                id = "6",
                name = "Squash - Zona Rosa",
                description = "45.000 por hora",
                distance = "3.7 km",
                imageUrl = "https://example.com/squash1.jpg"
            )
        )
        
        offersAdapter.submitList(offers)
    }
}