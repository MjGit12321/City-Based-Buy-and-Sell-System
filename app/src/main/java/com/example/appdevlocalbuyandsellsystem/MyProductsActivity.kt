package com.example.appdevlocalbuyandsellsystem

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
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
    private lateinit var adapter: ProductAdapter
    private lateinit var btnDelete: ImageButton
    private lateinit var tvNoProducts: TextView

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

        btnDelete = findViewById(R.id.btnDeleteProducts)
        tvNoProducts = findViewById(R.id.tvNoProducts)

        val rvMyProducts = findViewById<RecyclerView>(R.id.rvMyProducts)
        rvMyProducts.layoutManager = LinearLayoutManager(this)

        btnDelete.setOnClickListener {
            showDeleteConfirmationDialog()
        }

        // 3. REMOVED THE HARDCODED LIST -> FETCH FROM DB
        fetchMyProductsFromFirestore(rvMyProducts)

        onBackPressedDispatcher.addCallback(this, object : androidx.activity.OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (::adapter.isInitialized && adapter.isSelectionMode) {
                    adapter.isSelectionMode = false
                    btnDelete.visibility = View.GONE
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })

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
                    val product = document.toObject(Product::class.java)
                    product.documentId = document.id // Ensure ID is set
                    productList.add(product)
                }

                if (productList.isEmpty()) {
                    tvNoProducts.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                } else {
                    tvNoProducts.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                }

                adapter = ProductAdapter(
                    productList,
                    onFavoriteClick = { product ->
                        // Handle favorite if needed
                    },
                    onItemClick = { product ->
                        val intent = Intent(this, MyProductDetailsActivity::class.java)
                        intent.putExtra("PRODUCT_DATA", product)
                        startActivity(intent)
                    },
                    onSelectionChanged = { count ->
                        btnDelete.visibility = if (count > 0) View.VISIBLE else View.GONE
                    }
                )
                recyclerView.adapter = adapter
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to load: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showDeleteConfirmationDialog() {
        val selectedProducts = adapter.getSelectedProducts()
        if (selectedProducts.isEmpty()) return

        AlertDialog.Builder(this)
            .setTitle("Delete Products")
            .setMessage("Are you sure you want to delete ${selectedProducts.size} selected products?")
            .setPositiveButton("Delete") { _, _ ->
                deleteSelectedProducts(selectedProducts)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteSelectedProducts(products: List<Product>) {
        val batch = db.batch()
        products.forEach { product ->
            val docRef = db.collection("products").document(product.documentId)
            batch.delete(docRef)
        }

        batch.commit()
            .addOnSuccessListener {
                Toast.makeText(this, "Products deleted successfully", Toast.LENGTH_SHORT).show()
                adapter.isSelectionMode = false
                btnDelete.visibility = View.GONE
                fetchMyProductsFromFirestore(findViewById(R.id.rvMyProducts))
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to delete: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
