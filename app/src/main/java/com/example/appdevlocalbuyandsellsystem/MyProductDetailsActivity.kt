package com.example.appdevlocalbuyandsellsystem

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File

class MyProductDetailsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.my_product_details)

        // Adjust for system bars (status/nav bars)
        val rootLayout = findViewById<android.view.View>(android.R.id.content)
        ViewCompat.setOnApplyWindowInsetsListener(rootLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Get Product Data from Intent
        val product = intent.getSerializableExtra("PRODUCT_DATA") as? Product

        // Populate UI with dynamic data
        product?.let {
            findViewById<TextView>(R.id.tvMyProductDetailTitle).text = it.name
            findViewById<TextView>(R.id.tvMyProductDetailPrice).text = "₱${it.price}"
            findViewById<TextView>(R.id.tvMyProductDetailDesc).text = it.description
            findViewById<TextView>(R.id.tvMySellerName).text = it.sellerName

            // Load Product Image
            if (it.imageUrl.isNotEmpty()) {
                val file = if (it.imageUrl.contains("/")) File(it.imageUrl) else File(filesDir, it.imageUrl)
                Glide.with(this)
                    .load(file)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_report_image)
                    .centerInside()
                    .into(findViewById<ImageView>(R.id.ivMyProductDetailImage))
            }

            // Load Seller Profile Image
            FirebaseFirestore.getInstance().collection("users").document(it.sellerId).get()
                .addOnSuccessListener { doc ->
                    val profileImg = doc.getString("profileImageUrl")
                    if (!profileImg.isNullOrEmpty()) {
                        val file = if (profileImg.contains("/")) File(profileImg) else File(filesDir, profileImg)
                        Glide.with(this)
                            .load(file)
                            .circleCrop()
                            .placeholder(R.drawable.ic_user)
                            .into(findViewById<ImageView>(R.id.ivMySellerProfile))
                    }
                }
        }

        // Delete Button Logic
        findViewById<Button>(R.id.btnDeleteProduct).setOnClickListener {
            product?.documentId?.let { id ->
                showDeleteConfirmation(id)
            } ?: Toast.makeText(this, "Cannot delete: ID missing", Toast.LENGTH_SHORT).show()
        }

        // Footer Navigation Logic (Home highlighted)
        findViewById<LinearLayout>(R.id.navHome).setOnClickListener {
            val intent = Intent(this, MainpageActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }

        findViewById<LinearLayout>(R.id.navMessages).setOnClickListener {
            startActivity(Intent(this, InboxActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.navFavorites).setOnClickListener {
            startActivity(Intent(this, FavoritesActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.navMe).setOnClickListener {
            startActivity(Intent(this, MeActivity::class.java))
        }
    }

    private fun showDeleteConfirmation(productId: String) {
        AlertDialog.Builder(this)
            .setTitle("Delete Product")
            .setMessage("Are you sure you want to delete this product? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                if (productId.isNotEmpty()) {
                    FirebaseFirestore.getInstance().collection("products").document(productId)
                        .delete()
                        .addOnSuccessListener {
                            Toast.makeText(this, "Product deleted successfully", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Delete failed: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(this, "Error: Product ID missing", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
