package com.example.appdevlocalbuyandsellsystem

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
// 1. ADD THESE FIREBASE IMPORTS
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MyProductsActivity : AppCompatActivity() {

    // 2. INITIALIZE FIREBASE
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.my_products)

        val rootLayout = findViewById<android.view.View>(android.R.id.content)
        ViewCompat.setOnApplyWindowInsetsListener(rootLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val rvMyProducts = findViewById<RecyclerView>(R.id.rvMyProducts)
        rvMyProducts.layoutManager = LinearLayoutManager(this)

        // 3. REMOVED THE HARDCODED LIST -> FETCH FROM DB
        fetchMyProductsFromFirestore(rvMyProducts)

        // Navigation Bar Logic
        findViewById<LinearLayout>(R.id.navHome).setOnClickListener {
            startActivity(Intent(this, MainpageActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.navMessages).setOnClickListener {
            startActivity(Intent(this, InboxActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.navMe).setOnClickListener {
            startActivity(Intent(this, MeActivity::class.java))
            finish()
        }

        findViewById<LinearLayout>(R.id.navFavorites).setOnClickListener {
            startActivity(Intent(this, FavoritesActivity::class.java))
        }
    }

    // 4. THE FETCH FUNCTION
    private fun fetchMyProductsFromFirestore(recyclerView: RecyclerView) {
        val currentUserId = auth.currentUser?.uid ?: return

        db.collection("products")
            .whereEqualTo("sellerId", currentUserId)
            .get()
            .addOnSuccessListener { documents ->
                val productList = mutableListOf<Product>()

                for (document in documents) {
                    // This converts the Firestore document directly into your Product class
                    val product = document.toObject(Product::class.java)
                    productList.add(product)
                }

                // Attach to adapter
                recyclerView.adapter = ProductAdapter(productList) { product ->
                    val intent = Intent(this, MyProductDetailsActivity::class.java)
                    intent.putExtra("PRODUCT_DATA", product)
                    startActivity(intent)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to load: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}