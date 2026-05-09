package com.example.appdevlocalbuyandsellsystem

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

        // Display logic
        holder.tvPrice.text = "₱${product.price}"
        holder.tvName.text = product.name
        holder.tvDesc.text = product.description
        holder.tvUsername.text = product.sellerName

        // Favorite icon logic
        if (product.isFavorite) {
            holder.ivFavorite.setImageResource(R.drawable.ic_heart_filled)
            holder.ivFavorite.setColorFilter(android.graphics.Color.parseColor("#FF0000"))
        } else {
            holder.ivFavorite.setImageResource(R.drawable.ic_heart_outline)
            holder.ivFavorite.setColorFilter(android.graphics.Color.parseColor("#888888"))
        }

        holder.ivFavorite.setOnClickListener {
            product.isFavorite = !product.isFavorite
            notifyItemChanged(position)
        }

        // CLICK LOGIC: Navigate to Detail

        // Display logic
        holder.tvPrice.text = "₱${product.price}"
        holder.tvName.text = product.name
        holder.tvDesc.text = product.description
        holder.tvUsername.text = product.sellerName

        // Favorite icon logic
        if (product.isFavorite) {
            holder.ivFavorite.setImageResource(R.drawable.ic_heart_filled)
            holder.ivFavorite.setColorFilter(android.graphics.Color.parseColor("#FF0000"))
        } else {
            holder.ivFavorite.setImageResource(R.drawable.ic_heart_outline)
            holder.ivFavorite.setColorFilter(android.graphics.Color.parseColor("#888888"))
        }

        holder.ivFavorite.setOnClickListener {
            product.isFavorite = !product.isFavorite
            notifyItemChanged(position)
        }

        // Display logic
        holder.tvPrice.text = "₱${product.price}"
        holder.tvName.text = product.name
        holder.tvDesc.text = product.description
        holder.tvUsername.text = product.sellerName

        // Favorite icon logic
        if (product.isFavorite) {
            holder.ivFavorite.setImageResource(R.drawable.ic_heart_filled)
            holder.ivFavorite.setColorFilter(android.graphics.Color.parseColor("#FF0000"))
        } else {
            holder.ivFavorite.setImageResource(R.drawable.ic_heart_outline)
            holder.ivFavorite.setColorFilter(android.graphics.Color.parseColor("#888888"))
        }

        holder.ivFavorite.setOnClickListener {
            product.isFavorite = !product.isFavorite
            notifyItemChanged(position)
        }

        // CLICK LOGIC: Navigate to Details
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = android.content.Intent(context, ProductDetailsActivity::class.java)

            // Pass individual strings for easier retrieval in Details Activity
            intent.putExtra("PRODUCT_NAME", product.name)
            intent.putExtra("PRODUCT_PRICE", product.price)
            intent.putExtra("PRODUCT_DESC", product.description)
            intent.putExtra("PRODUCT_SELLER", product.sellerName)
            intent.putExtra("PRODUCT_LOCATION", product.location)

            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = productList.size
}
