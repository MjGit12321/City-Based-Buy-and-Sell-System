package com.example.appdevlocalbuyandsellsystem

import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

/**
 * Adapter for the Product list.
 * Binds Product data to the item_product layout.
 */
class ProductAdapter(
    private val productList: List<Product>,
    private val onItemClick: ((Product) -> Unit)? = null
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvPrice: TextView = itemView.findViewById(R.id.tvProductPrice)
        val tvName: TextView = itemView.findViewById(R.id.tvProductName)
        val tvDesc: TextView = itemView.findViewById(R.id.tvProductDesc)
        val tvUsername: TextView = itemView.findViewById(R.id.tvProductSellerName)
        val ivFavorite: ImageView = itemView.findViewById(R.id.ivProductFavorite)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = productList[position]

        // Display basic info
        holder.tvPrice.text = "₱${product.price}"
        holder.tvName.text = product.name
        holder.tvDesc.text = product.description
        holder.tvUsername.text = product.sellerName

        // Handle Favorite Icon
        if (product.isFavorite) {
            holder.ivFavorite.setImageResource(R.drawable.ic_heart_filled)
            holder.ivFavorite.setColorFilter(Color.parseColor("#FF0000"))
        } else {
            holder.ivFavorite.setImageResource(R.drawable.ic_heart_outline)
            holder.ivFavorite.setColorFilter(Color.parseColor("#888888"))
        }

        holder.ivFavorite.setOnClickListener {
            product.isFavorite = !product.isFavorite
            notifyItemChanged(position)
        }

        // Navigate to Details Page
        holder.itemView.setOnClickListener {
            if (onItemClick != null) {
                onItemClick.invoke(product)
            } else {
                // Fallback to default behavior if no custom lambda is provided
                val context = holder.itemView.context
                val intent = Intent(context, ProductDetailsActivity::class.java)
                intent.putExtra("PRODUCT_DATA", product)
                context.startActivity(intent)
            }
        }
    }

    override fun getItemCount(): Int = productList.size
}
