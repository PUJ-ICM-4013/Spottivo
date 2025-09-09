package com.example.spottivo.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.spottivo.R
import com.example.spottivo.ui.adapters.CommunityMembersAdapter
import com.example.spottivo.ui.models.CommunityMember
import com.google.android.material.tabs.TabLayout

class CommunityFragment : Fragment() {

    private lateinit var tabLayout: TabLayout
    private lateinit var membersRecyclerView: RecyclerView
    private lateinit var membersAdapter: CommunityMembersAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_community, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupViews(view)
        setupTabs()
        setupRecyclerView()
        loadCommunityMembers()
    }

    private fun setupViews(view: View) {
        tabLayout = view.findViewById(R.id.tab_layout)
        membersRecyclerView = view.findViewById(R.id.rv_community_members)
    }

    private fun setupTabs() {
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> loadCommunityMembers() // Messages tab
                    1 -> loadGroups() // Groups tab
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupRecyclerView() {
        membersAdapter = CommunityMembersAdapter { member ->
            // Handle member click
            // TODO: Navigate to member profile or start chat
        }
        
        membersRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = membersAdapter
        }
    }

    private fun loadCommunityMembers() {
        // Mock data for community members
        val members = listOf(
            CommunityMember(
                id = "1",
                name = "Carlos Rodríguez",
                lastMessage = "¿Alguien para fútbol hoy?",
                time = "10:30 AM",
                profileImageUrl = "",
                isOnline = true
            ),
            CommunityMember(
                id = "2",
                name = "María González",
                lastMessage = "Excelente partido ayer!",
                time = "9:15 AM",
                profileImageUrl = "",
                isOnline = false
            ),
            CommunityMember(
                id = "3",
                name = "Andrés López",
                lastMessage = "¿Conocen canchas de tenis?",
                time = "Ayer",
                profileImageUrl = "",
                isOnline = true
            ),
            CommunityMember(
                id = "4",
                name = "Sofia Martínez",
                lastMessage = "Grupo de running mañana",
                time = "Ayer",
                profileImageUrl = "",
                isOnline = false
            )
        )
        
        membersAdapter.submitList(members)
    }

    private fun loadGroups() {
        // Mock data for groups
        val groups = listOf(
            CommunityMember(
                id = "g1",
                name = "Fútbol Bogotá Norte",
                lastMessage = "Partido este sábado 3pm",
                time = "2:45 PM",
                profileImageUrl = "",
                isOnline = false
            ),
            CommunityMember(
                id = "g2",
                name = "Runners Chapinero",
                lastMessage = "Ruta por la Zona Rosa",
                time = "1:20 PM",
                profileImageUrl = "",
                isOnline = false
            ),
            CommunityMember(
                id = "g3",
                name = "Tenis Club",
                lastMessage = "Torneo mensual inscripciones",
                time = "11:30 AM",
                profileImageUrl = "",
                isOnline = false
            )
        )
        
        membersAdapter.submitList(groups)
    }
}