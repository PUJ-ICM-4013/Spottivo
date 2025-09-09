package com.example.spottivo.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.spottivo.R
import com.example.spottivo.ui.models.ProfileOption

class ProfileOptionsAdapter(
    private val onItemClick: (ProfileOption) -> Unit
) : ListAdapter<ProfileOption, ProfileOptionsAdapter.ProfileOptionViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileOptionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_profile_option, parent, false)
        return ProfileOptionViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProfileOptionViewHolder, position: Int) {
        val option = getItem(position)
        holder.bind(option)
    }

    inner class ProfileOptionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val optionIcon: ImageView = itemView.findViewById(R.id.iv_option_icon)
        private val optionTitle: TextView = itemView.findViewById(R.id.tv_option_title)
        private val optionDescription: TextView = itemView.findViewById(R.id.tv_option_description)
        private val arrowIcon: ImageView = itemView.findViewById(R.id.iv_arrow)

        fun bind(option: ProfileOption) {
            optionIcon.setImageResource(option.iconRes)
            optionTitle.text = option.title
            optionDescription.text = option.description
            
            itemView.setOnClickListener {
                onItemClick(option)
            }
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<ProfileOption>() {
            override fun areItemsTheSame(oldItem: ProfileOption, newItem: ProfileOption): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: ProfileOption, newItem: ProfileOption): Boolean {
                return oldItem == newItem
            }
        }
    }
}