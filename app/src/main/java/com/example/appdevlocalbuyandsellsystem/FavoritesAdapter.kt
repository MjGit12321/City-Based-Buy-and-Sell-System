package com.example.appdevlocalbuyandsellsystem

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File
import androidx.recyclerview.widget.RecyclerView

/**
 * Adapter for the Favorites list.
 */
class FavoritesAdapter(
    private var favoriteList: MutableList<Product>,
    private val onRemoveClick: (Int) -> Unit,
    private val onItemClick: (Product) -> Unit
) : RecyclerView.Adapter<FavoritesAdapter.FavoriteViewHolder>() {

    class FavoriteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvPrice: TextView = itemView.findViewById(R.id.tvFavoritePrice)
        val tvName: TextView = itemView.findViewById(R.id.tvFavoriteName)
        val tvDesc: TextView = itemView.findViewById(R.id.tvFavoriteDesc)
        val tvUsername: TextView = itemView.findViewById(R.id.tvFavoriteUsername)
        val ivHeart: ImageView = itemView.findViewById(R.id.ivFavoriteHeart)
        val ivFavoriteImage: ImageView = itemView.findViewById(R.id.ivFavoriteImage)
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

        // Load Seller Profile Image
        FirebaseFirestore.getInstance().collection("users").document(product.sellerId).get()
            .addOnSuccessListener { doc ->
                val profileImg = doc.getString("profileImageUrl")
                if (!profileImg.isNullOrEmpty()) {
                    val context = holder.itemView.context
                    val file = if (profileImg.contains("/")) File(profileImg) else File(context.filesDir, profileImg)
                    Glide.with(context)
                        .load(file)
                        .circleCrop()
                        .placeholder(R.drawable.ic_user)
                        .into(holder.itemView.findViewById<ImageView>(R.id.ivFavoriteUserIcon))
                } else {
                    holder.itemView.findViewById<ImageView>(R.id.ivFavoriteUserIcon).setImageResource(R.drawable.ic_user)
                }
            }

        // Load Product Image using Glide
        if (product.imageUrl.isNotEmpty()) {
            val context = holder.itemView.context
            val file = if (product.imageUrl.contains("/")) File(product.imageUrl) else File(context.filesDir, product.imageUrl)
            Glide.with(context)
                .load(file)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_report_image)
                .centerCrop()
                .into(holder.ivFavoriteImage)
        } else {
            holder.ivFavoriteImage.setImageResource(android.R.drawable.ic_menu_gallery)
        }

        holder.ivHeart.setOnClickListener {
            onRemoveClick(position)
        }

        holder.itemView.setOnClickListener {
            onItemClick(product)
        }
    }

    override fun getItemCount(): Int = favoriteList.size

    fun updateList(newList: MutableList<Product>) {
        favoriteList = newList
        notifyDataSetChanged()
    }
}
