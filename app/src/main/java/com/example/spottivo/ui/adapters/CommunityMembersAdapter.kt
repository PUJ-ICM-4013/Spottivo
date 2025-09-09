package com.example.spottivo.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.spottivo.R
import com.example.spottivo.ui.models.CommunityMember
import com.google.android.material.button.MaterialButton
import de.hdodenhof.circleimageview.CircleImageView

class CommunityMembersAdapter(
    private val onItemClick: (CommunityMember) -> Unit
) : ListAdapter<CommunityMember, CommunityMembersAdapter.CommunityMemberViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommunityMemberViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_community_member, parent, false)
        return CommunityMemberViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommunityMemberViewHolder, position: Int) {
        val member = getItem(position)
        holder.bind(member)
    }

    inner class CommunityMemberViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val profileImage: CircleImageView = itemView.findViewById(R.id.iv_profile_image)
        private val memberName: TextView = itemView.findViewById(R.id.tv_user_name)
        private val userStatus: TextView = itemView.findViewById(R.id.tv_user_status)
        private val actionButton: MaterialButton = itemView.findViewById(R.id.btn_action)

        fun bind(member: CommunityMember) {
            memberName.text = member.name
            userStatus.text = if (member.isOnline) "En línea" else "Desconectado"
            
            // Set placeholder profile image
            profileImage.setImageResource(R.drawable.ic_person)
            
            // Set action button text based on context
            actionButton.text = if (member.id.startsWith("g")) "Unirse" else "Mensaje"
            
            itemView.setOnClickListener {
                onItemClick(member)
            }
            
            actionButton.setOnClickListener {
                // TODO: Handle action button click
                onItemClick(member)
            }
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<CommunityMember>() {
            override fun areItemsTheSame(oldItem: CommunityMember, newItem: CommunityMember): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: CommunityMember, newItem: CommunityMember): Boolean {
                return oldItem == newItem
            }
        }
    }
}