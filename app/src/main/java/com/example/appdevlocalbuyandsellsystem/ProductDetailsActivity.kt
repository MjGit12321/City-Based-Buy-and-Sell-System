package com.example.appdevlocalbuyandsellsystem

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ProductDetailsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.product_details)

        // Adjust for system bars
        val rootLayout = findViewById<android.view.View>(android.R.id.content)
        ViewCompat.setOnApplyWindowInsetsListener(rootLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Get Product Data from Intent
        val product = intent.getSerializableExtra("PRODUCT_DATA") as? Product

        // Display Data if available
        product?.let {
            findViewById<TextView>(R.id.tvProductDetailTitle).text = it.name
            findViewById<TextView>(R.id.tvProductDetailPrice).text = it.price
            findViewById<TextView>(R.id.tvProductDetailDesc).text = it.description
            findViewById<TextView>(R.id.tvSellerName).text = it.username
        }

        // FAB Click Listeners
        findViewById<FloatingActionButton>(R.id.fabAddProduct).setOnClickListener {
            Toast.makeText(this, "Product added to wishlist!", Toast.LENGTH_SHORT).show()
        }

        findViewById<FloatingActionButton>(R.id.fabMessageSeller).setOnClickListener {
            val sellerName = product?.username ?: "Seller"
            val intent = Intent(this, MessageActivity::class.java)
            intent.putExtra("USER_NAME", sellerName)
            startActivity(intent)
        }

        // Navigation Bar Logic
        findViewById<LinearLayout>(R.id.navHome).setOnClickListener {
            val intent = Intent(this, MainpageActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }

        findViewById<LinearLayout>(R.id.navMessages).setOnClickListener {
            startActivity(Intent(this, InboxActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.navMe).setOnClickListener {
            startActivity(Intent(this, MeActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.navFavorites).setOnClickListener {
            startActivity(Intent(this, FavoritesActivity::class.java))
        }
    }
}
