package com.example.appdevlocalbuyandsellsystem

import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import androidx.recyclerview.widget.RecyclerView

/**
 * Adapter for the Product list.
 * Binds Product data to the item_product layout.
 */
class ProductAdapter(
    private val productList: List<Product>,
    private val onFavoriteClick: ((Product) -> Unit)? = null,
    private val onSelectionChanged: ((Int) -> Unit)? = null,
    private val onItemClick: ((Product) -> Unit)? = null
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    private val selectedItems = mutableSetOf<String>()
    var isSelectionMode = false
        set(value) {
            field = value
            if (!value) selectedItems.clear()
            notifyDataSetChanged()
        }

    fun getSelectedProducts(): List<Product> {
        return productList.filter { selectedItems.contains(it.documentId) }
    }

    class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvPrice: TextView = itemView.findViewById(R.id.tvProductPrice)
        val tvName: TextView = itemView.findViewById(R.id.tvProductName)
        val tvDesc: TextView = itemView.findViewById(R.id.tvProductDesc)
        val tvUsername: TextView = itemView.findViewById(R.id.tvProductSellerName)
        val ivFavorite: ImageView = itemView.findViewById(R.id.ivProductFavorite)
        val ivProductImage: ImageView = itemView.findViewById(R.id.ivProductImage)
        val ivUserIcon: ImageView = itemView.findViewById(R.id.ivProductUserIcon)
        val cbSelect: CheckBox = itemView.findViewById(R.id.cbSelectProduct)
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

        // Load Seller Profile Image
        FirebaseFirestore.getInstance().collection("users").document(product.sellerId).get()
            .addOnSuccessListener { doc ->
                val profileImg = doc.getString("profileImageUrl")
                if (!profileImg.isNullOrEmpty()) {
                    Glide.with(holder.itemView.context)
                        .load(profileImg)
                        .circleCrop()
                        .placeholder(R.drawable.ic_user)
                        .into(holder.ivUserIcon)
                } else {
                    holder.ivUserIcon.setImageResource(R.drawable.ic_user)
                }
            }

        // Load Product Image using Glide
        if (product.imageUrl.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(product.imageUrl)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_report_image)
                .centerCrop()
                .into(holder.ivProductImage)
        } else {
            holder.ivProductImage.setImageResource(android.R.drawable.ic_menu_gallery)
            holder.ivProductImage.setBackgroundColor(Color.parseColor("#CCCCCC"))
        }

        // Handle selection state
        holder.cbSelect.visibility = if (isSelectionMode) View.VISIBLE else View.GONE
        holder.cbSelect.setOnCheckedChangeListener(null) // Prevent recursive calls
        holder.cbSelect.isChecked = selectedItems.contains(product.documentId)

        holder.cbSelect.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                selectedItems.add(product.documentId)
            } else {
                selectedItems.remove(product.documentId)
            }
            onSelectionChanged?.invoke(selectedItems.size)
        }

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
            // Update UI immediately for snappiness
            if (product.isFavorite) {
                holder.ivFavorite.setImageResource(R.drawable.ic_heart_filled)
                holder.ivFavorite.setColorFilter(Color.parseColor("#FF0000"))
            } else {
                holder.ivFavorite.setImageResource(R.drawable.ic_heart_outline)
                holder.ivFavorite.setColorFilter(Color.parseColor("#888888"))
            }
            // Trigger Firestore update
            onFavoriteClick?.invoke(product)
        }

        // Navigate to Details Page
        holder.itemView.setOnClickListener {
            if (isSelectionMode) {
                holder.cbSelect.isChecked = !holder.cbSelect.isChecked
            } else {
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

        holder.itemView.setOnLongClickListener {
            if (!isSelectionMode) {
                isSelectionMode = true
                selectedItems.add(product.documentId)
                holder.cbSelect.isChecked = true
                onSelectionChanged?.invoke(selectedItems.size)
                true
            } else {
                false
            }
        }
    }

    override fun getItemCount(): Int = productList.size
}
