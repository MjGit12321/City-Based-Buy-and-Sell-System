package com.example.appdevlocalbuyandsellsystem

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

/**
 * Adapter for the Favorites list.
 */
class FavoritesAdapter(
    private var favoriteList: MutableList<Product>,
    private val onRemoveClick: (Int) -> Unit
) : RecyclerView.Adapter<FavoritesAdapter.FavoriteViewHolder>() {

    class FavoriteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvPrice: TextView = itemView.findViewById(R.id.tvFavoritePrice)
        val tvName: TextView = itemView.findViewById(R.id.tvFavoriteName)
        val tvDesc: TextView = itemView.findViewById(R.id.tvFavoriteDesc)
        val tvUsername: TextView = itemView.findViewById(R.id.tvFavoriteUsername)
        val tvRating: TextView = itemView.findViewById(R.id.tvFavoriteRating)
        val ivHeart: ImageView = itemView.findViewById(R.id.ivFavoriteHeart)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_favorite, parent, false)
        return FavoriteViewHolder(view)
    }

    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
        val product = favoriteList[position]
        holder.tvPrice.text = "₱${product.price}"
        holder.tvName.text = product.name
        holder.tvDesc.text = product.description
        holder.tvUsername.text = product.sellerName
        holder.tvRating.text = product.rating.toString()

        holder.ivHeart.setOnClickListener {
            onRemoveClick(position)
        }
    }

    override fun getItemCount(): Int = favoriteList.size

    fun updateList(newList: MutableList<Product>) {
        favoriteList = newList
        notifyDataSetChanged()
    }
}