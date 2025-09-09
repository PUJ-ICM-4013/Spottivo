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
import com.example.spottivo.ui.models.PopularPlace

class PopularPlacesAdapter(
    private val onItemClick: (PopularPlace) -> Unit
) : ListAdapter<PopularPlace, PopularPlacesAdapter.PopularPlaceViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PopularPlaceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_popular_place, parent, false)
        return PopularPlaceViewHolder(view)
    }

    override fun onBindViewHolder(holder: PopularPlaceViewHolder, position: Int) {
        val place = getItem(position)
        holder.bind(place)
    }

    inner class PopularPlaceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val placeImage: ImageView = itemView.findViewById(R.id.iv_place_image)
        private val placeName: TextView = itemView.findViewById(R.id.tv_place_name)
        private val placeDescription: TextView = itemView.findViewById(R.id.tv_place_description)
        private val placeDistance: TextView = itemView.findViewById(R.id.tv_place_distance)
        private val favoriteButton: ImageView = itemView.findViewById(R.id.iv_favorite)

        fun bind(place: PopularPlace) {
            placeName.text = place.name
            placeDescription.text = place.description
            placeDistance.text = place.distance
            
            // Set placeholder image
            placeImage.setImageResource(R.drawable.ic_location)
            
            // Set favorite icon
            favoriteButton.setImageResource(
                if (place.isFavorite) R.drawable.ic_favorite
                else R.drawable.ic_favorite_border
            )
            
            itemView.setOnClickListener {
                onItemClick(place)
            }
            
            favoriteButton.setOnClickListener {
                // TODO: Handle favorite toggle
            }
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<PopularPlace>() {
            override fun areItemsTheSame(oldItem: PopularPlace, newItem: PopularPlace): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: PopularPlace, newItem: PopularPlace): Boolean {
                return oldItem == newItem
            }
        }
    }
}