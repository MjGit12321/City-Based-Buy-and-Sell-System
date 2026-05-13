package com.example.appdevlocalbuyandsellsystem

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

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
        }

        // Delete Button Logic
        findViewById<Button>(R.id.btnDeleteProduct).setOnClickListener {
            showDeleteConfirmation()
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

    private fun showDeleteConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Delete Product")
            .setMessage("Are you sure you want to delete this product? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                // In a real app, logic to delete from database would go here
                Toast.makeText(this, "Product deleted successfully", Toast.LENGTH_SHORT).show()
                finish()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
