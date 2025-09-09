package com.example.spottivo.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.spottivo.R
import com.example.spottivo.ui.adapters.ProfileOptionsAdapter
import com.example.spottivo.ui.models.ProfileOption
import de.hdodenhof.circleimageview.CircleImageView

class ProfileFragment : Fragment() {

    private lateinit var profileImage: CircleImageView
    private lateinit var nameTextView: TextView
    private lateinit var sportsCountTextView: TextView
    private lateinit var friendsCountTextView: TextView
    private lateinit var optionsRecyclerView: RecyclerView
    private lateinit var optionsAdapter: ProfileOptionsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupViews(view)
        setupRecyclerView()
        loadUserData()
        loadProfileOptions()
    }

    private fun setupViews(view: View) {
        profileImage = view.findViewById(R.id.iv_profile_image)
        nameTextView = view.findViewById(R.id.tv_user_name)
        sportsCountTextView = view.findViewById(R.id.tv_sports_count)
        friendsCountTextView = view.findViewById(R.id.tv_friends_count)
        optionsRecyclerView = view.findViewById(R.id.rv_profile_options)
    }

    private fun setupRecyclerView() {
        optionsAdapter = ProfileOptionsAdapter { option ->
            // Handle option click
            when (option.id) {
                "edit_profile" -> {
                    // TODO: Navigate to edit profile
                }
                "settings" -> {
                    // TODO: Navigate to settings
                }
                "help" -> {
                    // TODO: Navigate to help
                }
                "logout" -> {
                    // TODO: Handle logout
                }
            }
        }
        
        optionsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = optionsAdapter
        }
    }

    private fun loadUserData() {
        // Mock user data
        nameTextView.text = "Juan Pérez"
        sportsCountTextView.text = "5"
        friendsCountTextView.text = "23"
        
        // TODO: Load actual profile image
        profileImage.setImageResource(R.drawable.ic_person)
    }

    private fun loadProfileOptions() {
        val options = listOf(
            ProfileOption(
                id = "edit_profile",
                title = "Editar Perfil",
                description = "Actualiza tu información personal",
                iconRes = R.drawable.ic_person
            ),
            ProfileOption(
                id = "settings",
                title = "Configuración",
                description = "Preferencias y privacidad",
                iconRes = R.drawable.ic_settings
            ),
            ProfileOption(
                id = "help",
                title = "Ayuda y Soporte",
                description = "Preguntas frecuentes y contacto",
                iconRes = R.drawable.ic_help
            ),
            ProfileOption(
                id = "logout",
                title = "Cerrar Sesión",
                description = "Salir de tu cuenta",
                iconRes = R.drawable.ic_logout
            )
        )
        
        optionsAdapter.submitList(options)
    }
}